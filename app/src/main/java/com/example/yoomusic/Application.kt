package com.example.yoomusic

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build

class Application : Application() {
    companion object{
        const val CHANNEL_ID = "channel1"
        const val PLAY = "play"
        const val NEXT = "next"
        const val PREV = "previous"
        lateinit var notificationManager:NotificationManager
    }
    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){

            val notificationChannel = NotificationChannel(CHANNEL_ID,"now playing song",NotificationManager.IMPORTANCE_HIGH)
            notificationChannel.description = "to show song in notification"
            notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(notificationChannel)

        }
    }
}