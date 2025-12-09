package com.example.yoomusic

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.audiofx.AudioEffect
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.yoomusic.databinding.ActivityMusicPlayerBinding
import com.google.gson.GsonBuilder
import androidx.transition.TransitionSet
import androidx.transition.Slide
import androidx.transition.Fade
import androidx.transition.TransitionManager
import android.view.Gravity
import android.view.View
import android.widget.ScrollView
import android.view.GestureDetector
import android.view.MotionEvent



class MusicPlayer : AppCompatActivity(), ServiceConnection, MediaPlayer.OnCompletionListener {

    private var lyricsVisible = false

    companion object {
        lateinit var musicListPA: ArrayList<Music>
        var tempMusicList: ArrayList<Music> = ArrayList()
        var songPosition: Int = 0

        //        var mediaPlayer :MediaPlayer? = null
        lateinit var songId: String
        var isPlaying = false
        var shuffle = false
        var repeat = false

        var musicService: MusicService? = null

        @SuppressLint("StaticFieldLeak")
        lateinit var binding: ActivityMusicPlayerBinding
        var nowPlayingId: String = ""
        var isFav = false
        var fvtIndex: Int = -1

    }


    //    private lateinit var binding: ActivityMusicPlayerBinding
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_YooMusic)
        setContentView(R.layout.activity_music_player)
        binding = ActivityMusicPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        var intent = Intent(this, MusicService::class.java)
//        bindService(intent, this, BIND_AUTO_CREATE)
//        startService(intent)

//        binding.marqueeText.isSelected = true
//        binding.musicPlayerTitle.isSelected = true
        init()

        // --- LYRICS SWIPE SETUP ---

// start with lyrics panel hidden (moved down off screen)
        binding.lyricsPanel.post {
            binding.lyricsPanel.translationY = binding.lyricsPanel.height.toFloat()
        }

        val gestureDetector = GestureDetector(
            this,
            object : android.view.GestureDetector.SimpleOnGestureListener() {

                private val SWIPE_THRESHOLD = 80
                private val SWIPE_VELOCITY_THRESHOLD = 80

                override fun onFling(
                    e1: MotionEvent?,
                    e2: MotionEvent,
                    velocityX: Float,
                    velocityY: Float
                ): Boolean {
                    if (e1 == null) return false

                    val diffY = e1.y - e2.y
                    val SWIPE_THRESHOLD = 80
                    val SWIPE_VELOCITY_THRESHOLD = 80

                    // Swipe UP -> show lyrics
                    if (diffY > SWIPE_THRESHOLD && kotlin.math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                        showLyrics()
                        return true
                    }

                    // Swipe DOWN -> hide lyrics
                    if (diffY < -SWIPE_THRESHOLD && kotlin.math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                        hideLyrics()
                        return true
                    }

                    return false
                }

            }
        )

// swipe on album art
        binding.lyricsHintContainer.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            true
        }

// optional: swipe down on lyrics panel to close
        binding.lyricsPanel.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            true
        }


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
                musicListPA.addAll(tempMusicList)
                songPosition = findIndexOfObjectWithValue(musicListPA, songId)
            } else {
                shuffle = true
                songId = musicListPA[songPosition].id
                musicListPA.shuffle()
                songPosition = findIndexOfObjectWithValue(musicListPA, songId)
            }
            updateShuffleButton()
        }

        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {

                if (fromUser) {
                    musicService!!.mediaPlayer!!.seekTo(progress)
                    musicService!!.showNotification(if (isPlaying) R.drawable.pause_svgrepo_com else R.drawable.play_svgrepo_com)
                }

            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit

            override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit

        })

        binding.repeatBtn.setOnClickListener {
            if (!repeat) {
                repeat = true
                binding.repeatBtn.setImageResource(R.drawable.repeat_one)
            } else {
                repeat = false
                binding.repeatBtn.setImageResource(R.drawable.repeat_all)
            }
        }

//        binding.equalizerBtn.setOnClickListener {
//            try {
//                val eqIntent = Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL)
//                eqIntent.putExtra(
//                    AudioEffect.EXTRA_AUDIO_SESSION,
//                    musicService!!.mediaPlayer!!.audioSessionId
//                )
//                eqIntent.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, baseContext.packageName)
//                eqIntent.putExtra(AudioEffect.EXTRA_CONTENT_TYPE, AudioEffect.CONTENT_TYPE_MUSIC)
//                startActivityForResult(eqIntent, 13)
//            } catch (e: Exception) {
//                Toast.makeText(this, "Equalizer Not Supported", Toast.LENGTH_SHORT).show()
//            }
//
//        }
//        binding.shareBtn.setOnClickListener {
//            val shareIntent = Intent()
//            shareIntent.action = Intent.ACTION_SEND
//            shareIntent.type = "audio/*"
//            shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(musicListPA[songPosition].path))
//            startActivity(Intent.createChooser(shareIntent, "Share Music"))
//        }


        binding.fvtPlayerBtn.setOnClickListener {
            fvtIndex = fvtChecker(musicListPA[songPosition].id)
            Log.d("fvt", fvtIndex.toString())
            if (isFav) {
                isFav = false
                binding.fvtPlayerBtn.setImageResource(R.drawable.add_fvt_btn)
                favourite.fvtItemList.removeAt(fvtIndex)


//              favourite.binding.totalFvtSongCount.text = "${favourite.musicAdapter.getItemCount()} Songs"
            } else {
                isFav = true
                binding.fvtPlayerBtn.setImageResource(R.drawable.add_fav_btn_selected)
                favourite.fvtItemList.add(0, musicListPA[songPosition])
                favourite.fvtItemList[0].dateAddedToList = System.currentTimeMillis()

//              favourite.binding.totalFvtSongCount.text = "${favourite.musicAdapter.getItemCount()} Songs"

            }

            //update fvt list

//

            //to store fvt data
            val editor = getSharedPreferences("FAVOURITES", MODE_PRIVATE).edit()
            val jsonString = GsonBuilder().create().toJson(favourite.fvtItemList)
            editor.putString("fvtSongs", jsonString)
            editor.apply()
        }

        binding.addToListBtn.setOnClickListener {
            startActivity(Intent(this, AddToPlaylist::class.java))
        }

//        binding.playerBtn.setOnClickListener {
//            binding.playerBtn.background = ContextCompat.getDrawable(this, R.drawable.btn_active)
//            binding.lyricsBtn.background = ContextCompat.getDrawable(this, R.drawable.btn_inactive)
//            slideToPlayer()
//        }
//
//        binding.lyricsBtn.setOnClickListener {
//            binding.lyricsBtn.background = ContextCompat.getDrawable(this, R.drawable.btn_active)
//            binding.playerBtn.background = ContextCompat.getDrawable(this, R.drawable.btn_inactive)
//            slideToLyrics()
//        }

//        binding.lyricsContainer.setNestedScrollingEnabled(true)

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
            } else {
                musicService!!.showNotification(R.drawable.play_svgrepo_com)
            }

            binding.currentDuration.text =
                formatDuration(musicService!!.mediaPlayer!!.currentPosition.toLong())
            binding.songDuration.text = formatDuration(musicListPA[songPosition].duration)
            binding.seekBar.progress = 0
            binding.seekBar.max = musicService!!.mediaPlayer!!.duration
            nowPlayingId = musicListPA[songPosition].id
            musicService!!.mediaPlayer!!.setOnCompletionListener(this)


        } catch (e: Exception) {
            return

        }
    }

    private fun updateLayout() {
        fvtIndex = fvtChecker(musicListPA[songPosition].id)
        binding.musicPlayerTitle.text = musicListPA[songPosition].title
        binding.musicPlayerAlbum.text = musicListPA[songPosition].artist
//        binding.marqueeText.text = musicListPA[songPosition].title
        Glide.with(this)
            .load(musicListPA[songPosition].artUri)
            .apply(RequestOptions().placeholder(R.drawable.artboard_2).centerCrop())
            .into(binding.musicPlayerImg)

        if (isFav) {
            binding.fvtPlayerBtn.setImageResource(R.drawable.add_fav_btn_selected)
        } else {
            binding.fvtPlayerBtn.setImageResource(R.drawable.add_fvt_btn)
        }


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
                tempMusicList.addAll(musicListPA)

                if (shuffle) {
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
                tempMusicList.addAll(musicListPA)
                musicListPA.shuffle()
                shuffle = true
                updateLayout()

            }

            "FvtFragment" -> {
                val intent = Intent(this, MusicService::class.java)
                bindService(intent, this, BIND_AUTO_CREATE)
                startService(intent)

                musicListPA = ArrayList()
                musicListPA.addAll(favourite.fvtItemList)
                tempMusicList.addAll(musicListPA)
                musicListPA.shuffle()
                shuffle = true
                updateLayout()
            }

            "searchAdapter" -> {
                val intent = Intent(this, MusicService::class.java)
                bindService(intent, this, BIND_AUTO_CREATE)
                startService(intent)

                musicListPA = ArrayList()
                musicListPA.addAll(searchFragment.searchItemList)
                tempMusicList.addAll(musicListPA)
                if (shuffle) {
                    songId = musicListPA[songPosition].id
                    musicListPA.shuffle()
                    songPosition = findIndexOfObjectWithValue(musicListPA, songId)

                }
                updateLayout()
            }

            "FavouriteAdapter" -> {
                val intent = Intent(this, MusicService::class.java)
                bindService(intent, this, BIND_AUTO_CREATE)
                startService(intent)

                musicListPA = ArrayList()
                musicListPA.addAll(favourite.fvtItemList)
                tempMusicList.addAll(musicListPA)
                if (shuffle) {
                    songId = musicListPA[songPosition].id
                    musicListPA.shuffle()
                    songPosition = findIndexOfObjectWithValue(musicListPA, songId)

                }
                updateLayout()
            }

            "NowPlaying" -> {

//                var intent = Intent(this, MusicService::class.java)
//                bindService(intent, this, BIND_AUTO_CREATE)
//                startService(intent)
                binding.currentDuration.text =
                    formatDuration(musicService!!.mediaPlayer!!.currentPosition.toLong())
                binding.songDuration.text =
                    formatDuration(musicService!!.mediaPlayer!!.duration.toLong())
                binding.seekBar.progress = musicService!!.mediaPlayer!!.currentPosition
                binding.seekBar.max = musicService!!.mediaPlayer!!.duration
                if (isPlaying) {
                    binding.ButtonPlayPause.setImageResource(R.drawable.pause_btn)
                } else {
                    binding.ButtonPlayPause.setImageResource(R.drawable.play_btn)
                }
                updateLayout()
            }

            "PlaylistAdapter" -> {
                val intent = Intent(this, MusicService::class.java)
                bindService(intent, this, BIND_AUTO_CREATE)
                startService(intent)

                musicListPA = ArrayList()
                musicListPA.addAll(playListFragment.playlistList.list[playlist_details.playlistPosition].playlist)
                tempMusicList.addAll(musicListPA)
                if (shuffle) {
                    songId = musicListPA[songPosition].id
                    musicListPA.shuffle()
                    songPosition = findIndexOfObjectWithValue(musicListPA, songId)

                }
                updateLayout()
            }

            "playlistShuffled" -> {
                val intent = Intent(this, MusicService::class.java)
                bindService(intent, this, BIND_AUTO_CREATE)
                startService(intent)

                musicListPA = ArrayList()
                musicListPA.addAll(playListFragment.playlistList.list[playlist_details.playlistPosition].playlist)
                tempMusicList.addAll(musicListPA)
                musicListPA.shuffle()
                shuffle = true
                updateLayout()
            }
        }

        Log.d("de", musicListPA[songPosition].toString())
    }

    private fun playMusic() {

        val focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
            .setOnAudioFocusChangeListener(musicService!!.audioFocusChangeListener)
            .build()

        val result = musicService!!.audioManager.requestAudioFocus(focusRequest)

        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {

            musicService!!.mediaPlayer!!.start()
            isPlaying = true
            binding.ButtonPlayPause.setImageResource(R.drawable.pause_btn)
            musicService!!.showNotification(R.drawable.pause_svgrepo_com)
            NowPlaying.binding.playPauseNP.setImageResource(R.drawable.pause_np)

        }


    }

    private fun pauseMusic() {
        musicService!!.audioManager.abandonAudioFocus(musicService!!.audioFocusChangeListener)
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
        if (!repeat) {
            binding.repeatBtn.setImageResource(R.drawable.repeat_all)

        } else {
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
        if (!repeat) {
            if (songPosition == musicListPA.size - 1) {
                songPosition = 0
            } else {
                songPosition++
            }
        }

        createMediaPlayer(playState = true)
        try {
//            updateLayout()
            fvtIndex = fvtChecker(musicListPA[songPosition].id)
            binding.musicPlayerTitle.text = musicListPA[songPosition].title
            binding.musicPlayerAlbum.text = musicListPA[songPosition].artist
//            binding.marqueeText.text = musicListPA[songPosition].title

            Glide.with(baseContext)
                .load(musicListPA[songPosition].artUri)
                .apply(RequestOptions().placeholder(R.drawable.artboard_2).centerCrop())
                .into(binding.musicPlayerImg)

//            NowPlaying().updateNP()
            Glide.with(baseContext)
                .load(musicListPA[songPosition].artUri)
                .apply(RequestOptions().placeholder(R.drawable.artboard_2).centerCrop())
                .into(NowPlaying.binding.imageNP)

            if (isFav) {
                binding.fvtPlayerBtn.setImageResource(R.drawable.add_fav_btn_selected)
            } else {
                binding.fvtPlayerBtn.setImageResource(R.drawable.add_fvt_btn)
            }


            NowPlaying.binding.nameNP.text = musicListPA[songPosition].title


        } catch (e: Exception) {
            Log.d("Error", e.toString())
            return
        }


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
        if (resultCode == 13 || resultCode == RESULT_OK)
            return
    }

    override fun onDestroy() {
        super.onDestroy()
        //to store fvt data
//        val editor = getSharedPreferences("FAVOURITES", MODE_PRIVATE).edit()
//        val jsonString = GsonBuilder().create().toJson(favourite.fvtItemList)
//        editor.putString("fvtSongs", jsonString)
//        editor.apply()
    }

//    private fun slideToLyrics() {
//        val transition = TransitionSet().apply {
//            addTransition(Fade())
//            addTransition(Slide(Gravity.START))
//            duration = 400
//        }
//
//        TransitionManager.beginDelayedTransition(binding.root, transition)
//
//        binding.musicPlayerImg.visibility = View.GONE
//        binding.linearLayout2.visibility = View.GONE
//        // Show lyrics container (add your lyrics view here)
//    }
//
//    private fun slideToPlayer() {
//        val transition = TransitionSet().apply {
//            addTransition(Fade())
//            addTransition(Slide(Gravity.END))
//            duration = 400
//        }
//
//        TransitionManager.beginDelayedTransition(binding.root, transition)
//
//        binding.musicPlayerImg.visibility = View.VISIBLE
//        binding.linearLayout2.visibility = View.VISIBLE
//    }


    private fun showLyrics() {
        if (lyricsVisible) return
        lyricsVisible = true
        binding.lyricsPanel.animate()
            .translationY(0f)
            .setDuration(250)
            .start()

        binding.lyricsHintContainer.visibility = View.GONE
    }

    private fun hideLyrics() {
        if (!lyricsVisible) return
        lyricsVisible = false
        binding.lyricsPanel.animate()
            .translationY(binding.lyricsPanel.height.toFloat())
            .setDuration(250)
            .start()

        binding.lyricsHintContainer.visibility = View.VISIBLE
    }



}