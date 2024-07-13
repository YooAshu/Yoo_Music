package com.example.yoomusic

import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.os.Binder
import android.os.Build
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
        //play music
        MusicPlayer.binding.ButtonPlayPause.setImageResource(R.drawable.pause_btn)
        NowPlaying.binding.playPauseNP.setImageResource(R.drawable.pause_np)
        MusicPlayer.isPlaying = true
        mediaPlayer?.start()
        showNotification(R.drawable.pause_svgrepo_com)
    }

    private fun pauseMusic(){
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
        MusicPlayer.binding.marqueeText.text = MusicPlayer.musicListPA[MusicPlayer.songPosition].title

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
        return START_STICKY
    }

}

