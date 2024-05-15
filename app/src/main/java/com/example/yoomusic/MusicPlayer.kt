package com.example.yoomusic

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.media.MediaPlayer
import android.media.audiofx.AudioEffect
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.yoomusic.databinding.ActivityMusicPlayerBinding

class MusicPlayer : AppCompatActivity(), ServiceConnection , MediaPlayer.OnCompletionListener {

    companion object {
        lateinit var musicListPA: ArrayList<Music>
        var songPosition: Int = 0

        //        var mediaPlayer :MediaPlayer? = null
        lateinit var songId:String
        var isPlaying = false
        var shuffle = false
        var repeat = false

        var musicService: MusicService? = null
        @SuppressLint("StaticFieldLeak")
        lateinit var binding: ActivityMusicPlayerBinding
        var nowPlayingId: String = ""
    }


//    private lateinit var binding: ActivityMusicPlayerBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_YooMusic)
        setContentView(R.layout.activity_music_player)
        binding = ActivityMusicPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        var intent = Intent(this, MusicService::class.java)
//        bindService(intent, this, BIND_AUTO_CREATE)
//        startService(intent)

        binding.marqueeText.isSelected = true
//        binding.musicPlayerTitle.isSelected = true
        init()

        binding.ButtonPlayPause.setOnClickListener {
            if (isPlaying) {
                pauseMusic()
            } else {
                playMusic()
            }
        }

        binding.backBtn.setOnClickListener {
            finish()
        }


        binding.ButtonPrev.setOnClickListener { playPrevMusic() }
        binding.ButtonNext.setOnClickListener { playNextMusic() }

        binding.shuffleBtn.setOnClickListener {
            if (shuffle) {
                shuffle = false
                songId = musicListPA[songPosition].id
                musicListPA = ArrayList()
                musicListPA.addAll(HomeFragment.musicListHome)
                songPosition = findIndexOfObjectWithValue(musicListPA, songId)
            } else {
                shuffle = true
                songId = musicListPA[songPosition].id
                musicListPA.shuffle()
                songPosition = findIndexOfObjectWithValue(musicListPA, songId)
            }
            updateShuffleButton()
        }

        binding.seekBar.setOnSeekBarChangeListener(object :SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {

                if (fromUser) {
                    musicService!!.mediaPlayer!!.seekTo(progress)
                }

            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit

            override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit

        })

       binding.repeatBtn.setOnClickListener {
           if(!repeat){
               repeat = true
               binding.repeatBtn.setImageResource(R.drawable.repeat_one)
           }else{
               repeat = false
               binding.repeatBtn.setImageResource(R.drawable.repeat_all)
           }
       }

       binding.equalizerBtn.setOnClickListener {
          try {
              val eqIntent = Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL)
              eqIntent.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, musicService!!.mediaPlayer!!.audioSessionId)
              eqIntent.putExtra(AudioEffect.EXTRA_PACKAGE_NAME,baseContext.packageName)
              eqIntent.putExtra(AudioEffect.EXTRA_CONTENT_TYPE,AudioEffect.CONTENT_TYPE_MUSIC)
              startActivityForResult(eqIntent,13)
          }
          catch (e:Exception){
              Toast.makeText(this,"Equalizer Not Supported",Toast.LENGTH_SHORT).show()
          }

       }
       binding.shareBtn.setOnClickListener {
           val shareIntent = Intent()
           shareIntent.action = Intent.ACTION_SEND
           shareIntent.type = "audio/*"
           shareIntent.putExtra(Intent.EXTRA_STREAM,Uri.parse(musicListPA[songPosition].path))
           startActivity(Intent.createChooser(shareIntent,"Share Music"))
       }


        updateShuffleButton()
        updateRepeatButton()
    }

    private fun createMediaPlayer(playState: Boolean) {
        try {
            if (musicService!!.mediaPlayer == null) {
                musicService!!.mediaPlayer = MediaPlayer()
            }
            musicService!!.mediaPlayer!!.reset()
            musicService!!.mediaPlayer!!.setDataSource(musicListPA[songPosition].path)
            musicService!!.mediaPlayer!!.prepare()
            if (playState) {
                musicService!!.mediaPlayer!!.start()
                isPlaying = true
                binding.ButtonPlayPause.setImageResource(R.drawable.pause_btn)
                musicService!!.showNotification(R.drawable.pause_svgrepo_com)
            }
            else{
                musicService!!.showNotification(R.drawable.play_svgrepo_com)
            }

            binding.currentDuration.text = formatDuration(musicService!!.mediaPlayer!!.currentPosition.toLong())
            binding.songDuration.text = formatDuration(musicListPA[songPosition].duration)
            binding.seekBar.progress =0
            binding.seekBar.max = musicService!!.mediaPlayer!!.duration
            nowPlayingId = musicListPA[songPosition].id
            musicService!!.mediaPlayer!!.setOnCompletionListener(this)




        } catch (e: Exception) {
            return

        }
    }

    private fun updateLayout() {
        binding.musicPlayerTitle.text = musicListPA[songPosition].title
        binding.musicPlayerAlbum.text = musicListPA[songPosition].artist
        binding.marqueeText.text = musicListPA[songPosition].title
        Glide.with(this)
            .load(musicListPA[songPosition].artUri)
            .apply(RequestOptions().placeholder(R.drawable.artboard_2).centerCrop())
            .into(binding.musicPlayerImg)



    }



//    check here for shuffle problrm
    private fun init() {
        songPosition = intent.getIntExtra("index", 0)

        when (intent.getStringExtra("class")) {
            "MusicAdapter" -> {
                val intent = Intent(this, MusicService::class.java)
                bindService(intent, this, BIND_AUTO_CREATE)
                startService(intent)

                musicListPA = ArrayList()
                musicListPA.addAll(HomeFragment.musicListHome)

                if(shuffle){
                    songId = musicListPA[songPosition].id
                    musicListPA.shuffle()
                    songPosition = findIndexOfObjectWithValue(musicListPA, songId)

                }
                updateLayout()
            }
            "HomeFragment" -> {
                val intent = Intent(this, MusicService::class.java)
                bindService(intent, this, BIND_AUTO_CREATE)
                startService(intent)

                musicListPA = ArrayList()
                musicListPA.addAll(HomeFragment.musicListHome)
                musicListPA.shuffle()
                shuffle = true
                updateLayout()

            }

            "searchAdapter"->{
                val intent = Intent(this, MusicService::class.java)
                bindService(intent, this, BIND_AUTO_CREATE)
                startService(intent)

                musicListPA = ArrayList()
                musicListPA.addAll(searchFragment.searchItemList)
                if(shuffle){
                    songId = musicListPA[songPosition].id
                    musicListPA.shuffle()
                    songPosition = findIndexOfObjectWithValue(musicListPA, songId)

                }
                updateLayout()
            }

            "NowPlaying"->{

//                var intent = Intent(this, MusicService::class.java)
//                bindService(intent, this, BIND_AUTO_CREATE)
//                startService(intent)
                binding.currentDuration.text = formatDuration(musicService!!.mediaPlayer!!.currentPosition.toLong())
                binding.songDuration.text = formatDuration(musicService!!.mediaPlayer!!.duration.toLong())
                binding.seekBar.progress = musicService!!.mediaPlayer!!.currentPosition
                binding.seekBar.max = musicService!!.mediaPlayer!!.duration
                if(isPlaying){
                    binding.ButtonPlayPause.setImageResource(R.drawable.pause_btn)
                }else{
                    binding.ButtonPlayPause.setImageResource(R.drawable.play_btn)
                }
                updateLayout()
            }
        }
    }

    private fun playMusic() {

        musicService!!.mediaPlayer!!.start()
        isPlaying = true
        binding.ButtonPlayPause.setImageResource(R.drawable.pause_btn)
        musicService!!.showNotification(R.drawable.pause_svgrepo_com)
        NowPlaying.binding.playPauseNP.setImageResource(R.drawable.pause_np)
    }

    private fun pauseMusic() {
        musicService!!.mediaPlayer!!.pause()
        isPlaying = false
        binding.ButtonPlayPause.setImageResource(R.drawable.play_btn)
        musicService!!.showNotification(R.drawable.play_svgrepo_com)
        NowPlaying.binding.playPauseNP.setImageResource(R.drawable.play_np)
    }

    private fun playNextMusic() {


            if (songPosition == musicListPA.size - 1) {
                songPosition = 0
            } else {
                songPosition++
            }


        if (isPlaying) {
            createMediaPlayer(playState = true)
        } else {
            createMediaPlayer(playState = false)
        }

        updateLayout()


    }

    private fun playPrevMusic() {


            if (songPosition == 0) {
                songPosition = musicListPA.size - 1
            } else {
                songPosition--
            }



        if (isPlaying) {
            createMediaPlayer(playState = true)
        } else {
            createMediaPlayer(playState = false)
        }
        updateLayout()

    }

    fun shufflePosition() {
        songPosition = (0 until musicListPA.size).random()

    }

    private fun updateShuffleButton() {
        if (shuffle) {
            binding.shuffleBtn.setImageResource(R.drawable.shuffle_selected)
        } else {
            binding.shuffleBtn.setImageResource(R.drawable.shuffle_btn)
        }
    }
    private fun updateRepeatButton() {
        if(!repeat){
            binding.repeatBtn.setImageResource(R.drawable.repeat_all)

        }else{
            binding.repeatBtn.setImageResource(R.drawable.repeat_one)
        }
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        val binder = service as MusicService.MyBinder
        musicService = binder.currentService()
        createMediaPlayer(playState = true)
        musicService!!.seekBarSetup()
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        musicService = null
//        exitProcess(1)

    }

    override fun onCompletion(mp: MediaPlayer?) {
        if(!repeat){
            if (songPosition == musicListPA.size - 1) {
                songPosition = 0
            } else {
                songPosition++
            }
        }

        createMediaPlayer(playState = true)
        try {
//            updateLayout()
            binding.musicPlayerTitle.text = musicListPA[songPosition].title
            binding.musicPlayerAlbum.text = musicListPA[songPosition].artist
            binding.marqueeText.text = musicListPA[songPosition].title

            Glide.with(baseContext)
                    .load(musicListPA[songPosition].artUri)
                    .apply(RequestOptions().placeholder(R.drawable.artboard_2).centerCrop())
                    .into(binding.musicPlayerImg)

//            NowPlaying().updateNP()
            Glide.with(baseContext)
                .load(musicListPA[songPosition].artUri)
                .apply(RequestOptions().placeholder(R.drawable.artboard_2).centerCrop())
                .into(NowPlaying.binding.imageNP)

            NowPlaying.binding.nameNP.text = musicListPA[songPosition].title



        }catch (e:Exception){
            Log.d("Error",e.toString())
            return}



    }


    private fun findIndexOfObjectWithValue(objects: ArrayList<Music>, id: String): Int {
        for (i in objects.indices) {
            if (objects[i].id == id) {
                return i
            }
        }

        return 0 // Value not found
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode==13 || resultCode == RESULT_OK)
            return
    }


}