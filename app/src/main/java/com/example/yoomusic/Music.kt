package com.example.yoomusic

import android.content.res.Resources
import android.media.MediaMetadataRetriever
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import java.io.File
import java.util.concurrent.TimeUnit

data class Music(
    val id:String,
    val title:String,
    val album:String,
    val artist:String,
    val duration:Long = 0,
    val path:String,
    val artUri : String,
    val dateAdded:Long = 0,
    var dateAddedToList:Long = 0
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

fun fvtChecker(id: String) : Int{
    MusicPlayer.isFav = false

    favourite.fvtItemList.forEachIndexed { index, music ->
        if (id==music.id){
            MusicPlayer.isFav = true
            return index
        }

    }
    return -1
}

class Playlist{
    var name:String = ""
    var playlist:ArrayList<Music> = ArrayList()
    var createdOn:String = ""
}
class PlaylistLists{
    var list:ArrayList<Playlist> = ArrayList()

}

class FragmentManagerHelper(private val fragmentManager: FragmentManager) {

    fun replaceFragment(containerId: Int, newFragment: Fragment) {
        fragmentManager.beginTransaction()
            .replace(containerId, newFragment)
            .commit()
    }
}

fun checkSongs(playlist: ArrayList<Music>): ArrayList<Music> {
    return playlist.filter { music ->
        val file = File(music.path)
        file.exists() // Keep only music objects for which the file exists
    } as ArrayList<Music>
}

fun setSongPosition(increment: Boolean) {
    if (!MusicPlayer.repeat) {
        if (increment) {
            if (MusicPlayer.musicListPA.size - 1 == MusicPlayer.songPosition)
                MusicPlayer.songPosition = 0
            else ++MusicPlayer.songPosition
        } else {
            if (0 == MusicPlayer.songPosition)
                MusicPlayer.songPosition = MusicPlayer.musicListPA.size - 1
            else --MusicPlayer.songPosition
        }
    }
}




