package com.example.yoomusic

import android.app.PendingIntent
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
import android.graphics.BitmapFactory
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Binder
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.core.app.NotificationCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlin.system.exitProcess

class MusicService : Service() {

    private var myBinder = MyBinder()
    var mediaPlayer: MediaPlayer? = null

    private lateinit var runnable : Runnable

    lateinit var audioManager: AudioManager
    var focusRequest: AudioFocusRequest? = null

    private lateinit var mediaSession: MediaSessionCompat
    override fun onBind(intent: Intent?): IBinder {
        mediaSession = MediaSessionCompat(baseContext, "My Music")

        return myBinder
    }

    inner class MyBinder : Binder() {

        fun currentService(): MusicService {
            return this@MusicService
        }
    }

    override fun onCreate() {
        super.onCreate()
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        val filter = IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
        registerReceiver(noisyAudioReceiver, filter)
//        initializeAudioFocus()
    }

//    override fun onStart(intent: Intent?, startId: Int) {
//        super.onStart(intent, startId)
//        initializeAudioFocus()
//    }

    override fun onDestroy() {
        super.onDestroy()
        abandonAudioFocus()
        unregisterReceiver(noisyAudioReceiver)
        pauseMusic()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
//        stopForeground(true)
        stopForeground(STOP_FOREGROUND_REMOVE)
        MusicPlayer.musicService!!.mediaPlayer!!.release()
        MusicPlayer.musicService = null
        stopSelf()
        exitProcess(1)
    }


    fun showNotification(playPauseBtn: Int) {

        var flag = 0
        flag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        val intent = Intent(baseContext, MainActivity::class.java)
        val contentIntent = PendingIntent.getActivity(this, 0, intent, flag)

        val prevIntent = Intent(baseContext, NotificationReceiver::class.java).setAction(Application.PREV)
        val prevPendingIntent = PendingIntent.getBroadcast(baseContext, 0, prevIntent,flag)

        val playIntent = Intent(baseContext, NotificationReceiver::class.java).setAction(Application.PLAY)
        val playPendingIntent = PendingIntent.getBroadcast(baseContext, 0, playIntent,flag)

        val nextIntent = Intent(baseContext, NotificationReceiver::class.java).setAction(Application.NEXT)
        val nextPendingIntent = PendingIntent.getBroadcast(baseContext, 0, nextIntent,flag)


        val imgArt = getImageArt(MusicPlayer.musicListPA[MusicPlayer.songPosition].path)

        var largeImage = if (imgArt != null){
            BitmapFactory.decodeByteArray(imgArt,0,imgArt.size)
        }
        else{
            BitmapFactory.decodeResource(resources, R.drawable.artboard_2)
        }
        val notification = NotificationCompat.Builder(baseContext, Application.CHANNEL_ID)
            .setContentIntent(contentIntent)
            .setContentTitle(MusicPlayer.musicListPA[MusicPlayer.songPosition].title)
            .setContentText(MusicPlayer.musicListPA[MusicPlayer.songPosition].artist)
            .setSmallIcon(R.drawable.small_icon)
            .setLargeIcon(largeImage)
            .setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setMediaSession(mediaSession.sessionToken)
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .addAction(R.drawable.previous_svgrepo_com, "previous", prevPendingIntent)
            .addAction(playPauseBtn, "play", playPendingIntent)
            .addAction(R.drawable.next_svgrepo_com, "next", nextPendingIntent)
            .build()


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

            mediaSession.setMetadata(
                MediaMetadataCompat.Builder().putLong(
                    MediaMetadataCompat.METADATA_KEY_DURATION, mediaPlayer!!.duration.toLong()
                ).build()
            )

            mediaSession.setPlaybackState(getPlayBackState())
            mediaSession.setCallback(object : MediaSessionCompat.Callback() {

                //called when play button is pressed
                override fun onPlay() {
                    super.onPlay()
                    handlePlayPause()
                }

                //called when pause button is pressed
                override fun onPause() {
                    super.onPause()
                    handlePlayPause()
                }

                //called when next button is pressed
                override fun onSkipToNext() {
                    super.onSkipToNext()
                    prevNextSong(increment = true, context = baseContext)
                }

                //called when previous button is pressed
                override fun onSkipToPrevious() {
                    super.onSkipToPrevious()
                    prevNextSong(increment = false, context = baseContext)
                }

                //called when headphones buttons are pressed
                //currently only pause or play music on button click
                override fun onMediaButtonEvent(mediaButtonEvent: Intent?): Boolean {
                    handlePlayPause()
                    return super.onMediaButtonEvent(mediaButtonEvent)
                }

                //called when seekbar is changed
                override fun onSeekTo(pos: Long) {
                    super.onSeekTo(pos)
                    mediaPlayer?.seekTo(pos.toInt())

                    mediaSession.setPlaybackState(getPlayBackState())
                }
            })
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            startForeground(13, notification)
//            Application.notificationManager.notify(13, notification)
        } else {
            startForeground(13, notification, FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
//            Application.notificationManager.notify(13, notification)
        }
    }

    fun createMediaPlayer(playState: Boolean) {
        try {
            if (MusicPlayer.musicService!!.mediaPlayer == null) {
                MusicPlayer.musicService!!.mediaPlayer = MediaPlayer()
            }
            MusicPlayer.musicService!!.mediaPlayer!!.reset()
            MusicPlayer.musicService!!.mediaPlayer!!.setDataSource(MusicPlayer.musicListPA[MusicPlayer.songPosition].path)
            MusicPlayer.musicService!!.mediaPlayer!!.prepare()
            if (playState) {
                MusicPlayer.musicService!!.mediaPlayer!!.start()
                MusicPlayer.isPlaying = true
                MusicPlayer.binding.ButtonPlayPause.setImageResource(R.drawable.pause_btn)
                MusicPlayer.musicService!!.showNotification(R.drawable.pause_svgrepo_com,)
            }
            else{
                MusicPlayer.musicService!!.showNotification(R.drawable.play_svgrepo_com,)
            }


            MusicPlayer.binding.currentDuration.text = formatDuration(mediaPlayer!!.currentPosition.toLong())
            MusicPlayer.binding.seekBar.progress =0
            MusicPlayer.binding.seekBar.max = mediaPlayer!!.duration

            MusicPlayer.nowPlayingId = MusicPlayer.musicListPA[MusicPlayer.songPosition].id


        } catch (e: Exception) {
            return

        }
    }



    fun seekBarSetup() {

        runnable = Runnable {
            MusicPlayer.binding.currentDuration.text = formatDuration(mediaPlayer!!.currentPosition.toLong())
            MusicPlayer.binding.seekBar.progress = mediaPlayer!!.currentPosition
            Handler(Looper.getMainLooper()).postDelayed(runnable, 200)
        }
        Handler(Looper.getMainLooper()).postDelayed(runnable, 0)
    }

    fun getPlayBackState(): PlaybackStateCompat {
        val playbackSpeed = if (MusicPlayer.isPlaying) 1F else 0F

        return PlaybackStateCompat.Builder().setState(
            if (mediaPlayer?.isPlaying == true) PlaybackStateCompat.STATE_PLAYING else PlaybackStateCompat.STATE_PAUSED,
            mediaPlayer!!.currentPosition.toLong(), playbackSpeed)
            .setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE or PlaybackStateCompat.ACTION_SEEK_TO or PlaybackStateCompat.ACTION_SKIP_TO_NEXT or PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS)
            .build()
    }

    fun handlePlayPause() {
        if (MusicPlayer.isPlaying) pauseMusic()
        else playMusic()

        //update playback state for notification
        mediaSession.setPlaybackState(getPlayBackState())
    }
    private fun playMusic(){

        val focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
            .setOnAudioFocusChangeListener(audioFocusChangeListener)
            .build()

        val result = audioManager.requestAudioFocus(focusRequest)
        //play music

        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            // Start playback
            MusicPlayer.binding.ButtonPlayPause.setImageResource(R.drawable.pause_btn)
            NowPlaying.binding.playPauseNP.setImageResource(R.drawable.pause_np)
            MusicPlayer.isPlaying = true
            mediaPlayer?.start()
            showNotification(R.drawable.pause_svgrepo_com)
        }

    }

    private fun pauseMusic(){
        audioManager.abandonAudioFocus(audioFocusChangeListener)
        //pause music
        MusicPlayer.binding.ButtonPlayPause.setImageResource(R.drawable.play_btn)
        NowPlaying.binding.playPauseNP.setImageResource(R.drawable.play_np)
        MusicPlayer.isPlaying = false
        mediaPlayer!!.pause()
        showNotification(R.drawable.play_svgrepo_com)
    }

    private fun prevNextSong(increment: Boolean, context: Context){

        setSongPosition(increment = increment)

        if(MusicPlayer.isPlaying){

            MusicPlayer.musicService?.createMediaPlayer(true)
            playMusic()
        }
        else{
            MusicPlayer.musicService?.createMediaPlayer(false)
        }
        Glide.with(context)
            .load(MusicPlayer.musicListPA[MusicPlayer.songPosition].artUri)
            .apply(RequestOptions().placeholder(R.drawable.artboard_2).centerCrop())
            .into(MusicPlayer.binding.musicPlayerImg)

        MusicPlayer.binding.musicPlayerTitle.text = MusicPlayer.musicListPA[MusicPlayer.songPosition].title
//        MusicPlayer.binding.marqueeText.text = MusicPlayer.musicListPA[MusicPlayer.songPosition].title

        Glide.with(context)
            .load(MusicPlayer.musicListPA[MusicPlayer.songPosition].artUri)
            .apply(RequestOptions().placeholder(R.drawable.artboard_2).centerCrop())
            .into(NowPlaying.binding.imageNP)

        NowPlaying.binding.nameNP.text = MusicPlayer.musicListPA[MusicPlayer.songPosition].title



        MusicPlayer.fvtIndex = fvtChecker(MusicPlayer.musicListPA[MusicPlayer.songPosition].id)
        if(MusicPlayer.isFav) MusicPlayer.binding.fvtPlayerBtn.setImageResource(R.drawable.add_fav_btn_selected)
        else MusicPlayer.binding.fvtPlayerBtn.setImageResource(R.drawable.add_fvt_btn)

        //update playback state for notification
        mediaSession.setPlaybackState(getPlayBackState())
    }

    //for making persistent
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
//        initializeAudioFocus()
//        playMusic()
        Log.d("MusicService", "onStartCommand called")
        val focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
            .setOnAudioFocusChangeListener(audioFocusChangeListener)
            .build()

        val result = audioManager.requestAudioFocus(focusRequest)
        return START_STICKY
    }


//when other music player plays musi stop playing
     val audioFocusChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
        when (focusChange) {
            AudioManager.AUDIOFOCUS_GAIN -> {
                // You have gained audio focus, resume playback or increase volume
                MusicPlayer.binding.ButtonPlayPause.setImageResource(R.drawable.pause_btn)
                NowPlaying.binding.playPauseNP.setImageResource(R.drawable.pause_np)
                MusicPlayer.isPlaying = true
                mediaPlayer?.start()
                showNotification(R.drawable.pause_svgrepo_com)
                mediaPlayer?.setVolume(1.0f, 1.0f) // Restore volume
            }
            AudioManager.AUDIOFOCUS_LOSS -> {
                // You have lost audio focus completely, stop playback
                MusicPlayer.binding.ButtonPlayPause.setImageResource(R.drawable.play_btn)
                NowPlaying.binding.playPauseNP.setImageResource(R.drawable.play_np)
                MusicPlayer.isPlaying = false
                mediaPlayer!!.pause()
                showNotification(R.drawable.play_svgrepo_com)
//                mediaPlayer?.release() // Optionally release resources
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                // You have temporarily lost audio focus, pause playback
                MusicPlayer.binding.ButtonPlayPause.setImageResource(R.drawable.play_btn)
                NowPlaying.binding.playPauseNP.setImageResource(R.drawable.play_np)
                MusicPlayer.isPlaying = false
                mediaPlayer!!.pause()
                showNotification(R.drawable.play_svgrepo_com)
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                // You have temporarily lost audio focus, but can duck (lower volume)
                mediaPlayer?.setVolume(0.5f, 0.5f) // Reduce volume
            }
        }
    }

    fun initializeAudioFocus() {
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        // Create an AudioFocusRequest (available from API level 26)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setOnAudioFocusChangeListener(audioFocusChangeListener)
                .setAcceptsDelayedFocusGain(true)
                .setWillPauseWhenDucked(true)
                .build()

            val result = audioManager.requestAudioFocus(focusRequest!!)
            handleAudioFocusResult(result)
        } else {
            // For devices running below API level 26
            val result = audioManager.requestAudioFocus(
                audioFocusChangeListener,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
            )
            handleAudioFocusResult(result)
        }
    }

    private fun handleAudioFocusResult(result: Int) {
        when (result) {
            AudioManager.AUDIOFOCUS_REQUEST_GRANTED -> {
                // You have audio focus now
//                startPlayback()
                playMusic()
            }
            AudioManager.AUDIOFOCUS_REQUEST_FAILED -> {
                // Failed to gain audio focus, handle the failure
            }
        }
    }
    fun abandonAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            focusRequest?.let {
                audioManager.abandonAudioFocusRequest(it)
            }
        } else {
            audioManager.abandonAudioFocus(audioFocusChangeListener)
        }
    }
//when bluetooth headset removed
    private val noisyAudioReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (AudioManager.ACTION_AUDIO_BECOMING_NOISY == intent.action) {
                // Audio is becoming noisy, pause or stop playback
                MusicPlayer.binding.ButtonPlayPause.setImageResource(R.drawable.play_btn)
                NowPlaying.binding.playPauseNP.setImageResource(R.drawable.play_np)
                MusicPlayer.isPlaying = false
                mediaPlayer!!.pause()
                showNotification(R.drawable.play_svgrepo_com)
            }
        }
    }

}








