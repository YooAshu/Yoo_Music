package com.example.yoomusic

import android.media.MediaMetadataRetriever
import java.util.concurrent.TimeUnit

data class Music(
    val id:String,
    val title:String,
    val album:String,
    val artist:String,
    val duration:Long = 0,
    val path:String,
    val artUri : String
)

fun formatDuration(duration: Long):String{

    var minutes = TimeUnit.MINUTES.convert(duration,TimeUnit.MILLISECONDS)
    var seconds = (TimeUnit.SECONDS.convert(duration,TimeUnit.MILLISECONDS)
            - minutes*TimeUnit.SECONDS.convert(1,TimeUnit.MINUTES))


    return String.format("%02d:%02d",minutes,seconds)
}

fun getImageArt(path:String): ByteArray? {
    val retriver = MediaMetadataRetriever()
    retriver.setDataSource(path)
    return retriver.embeddedPicture
}