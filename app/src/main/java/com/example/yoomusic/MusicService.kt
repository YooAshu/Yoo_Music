package com.example.yoomusic

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.os.Binder
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import androidx.core.app.NotificationCompat
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
                MusicPlayer.musicService!!.showNotification(R.drawable.pause_svgrepo_com)
            }
            else{
                MusicPlayer.musicService!!.showNotification(R.drawable.play_svgrepo_com)
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




}

