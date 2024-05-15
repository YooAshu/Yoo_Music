package com.example.yoomusic

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {
            Application.PREV -> {
                prevSong(context = context!!)
            }
            Application.PLAY -> {
                if (MusicPlayer.isPlaying) pauseMusic() else playMusic()
            }

            Application.NEXT -> {
                nextSong(context = context!!)
            }
        }
    }

    private fun playMusic() {
        MusicPlayer.isPlaying = true
        MusicPlayer.musicService!!.mediaPlayer!!.start()
        MusicPlayer.musicService!!.showNotification(R.drawable.pause_svgrepo_com)
        MusicPlayer.binding.ButtonPlayPause.setImageResource(R.drawable.pause_btn)
        NowPlaying.binding.playPauseNP.setImageResource(R.drawable.pause_np)

    }

    private fun pauseMusic() {
        MusicPlayer.isPlaying = false
        MusicPlayer.musicService!!.mediaPlayer!!.pause()
        MusicPlayer.musicService!!.showNotification(R.drawable.play_svgrepo_com)
        MusicPlayer.binding.ButtonPlayPause.setImageResource(R.drawable.play_btn)
        NowPlaying.binding.playPauseNP.setImageResource(R.drawable.play_np)

    }

    private fun nextSong(context: Context) {

            if (MusicPlayer.songPosition == MusicPlayer.musicListPA.size - 1) {
                MusicPlayer.songPosition = 0
            } else {
                MusicPlayer.songPosition += 1
            }

        updateMusicPlayerActivityLayout(context)
        updateNowPlayingFragment(context)
        if (MusicPlayer.isPlaying) {
            MusicPlayer.musicService!!.createMediaPlayer(playState = true)
        } else {
            MusicPlayer.musicService!!.createMediaPlayer(playState = false)
        }
    }
    private fun prevSong(context: Context) {

            if (MusicPlayer.songPosition == 0) {
                MusicPlayer.songPosition = MusicPlayer.musicListPA.size-1
            } else {
                MusicPlayer.songPosition -= 1
            }


        updateMusicPlayerActivityLayout(context)
        updateNowPlayingFragment(context)
        if (MusicPlayer.isPlaying) {
            MusicPlayer.musicService!!.createMediaPlayer(playState = true)
        } else {
            MusicPlayer.musicService!!.createMediaPlayer(playState = false)
        }
    }



    private fun updateMusicPlayerActivityLayout(context: Context){
        Glide.with(context)
            .load(MusicPlayer.musicListPA[MusicPlayer.songPosition].artUri)
            .apply(RequestOptions().placeholder(R.drawable.artboard_2).centerCrop())
            .into(MusicPlayer.binding.musicPlayerImg)
        MusicPlayer.binding.musicPlayerTitle.text = MusicPlayer.musicListPA[MusicPlayer.songPosition].title
        MusicPlayer.binding.musicPlayerAlbum.text = MusicPlayer.musicListPA[MusicPlayer.songPosition].artist
        MusicPlayer.binding.songDuration.text = formatDuration(MusicPlayer.musicListPA[MusicPlayer.songPosition].duration)

        MusicPlayer.binding.marqueeText.text = MusicPlayer.musicListPA[MusicPlayer.songPosition].title
    }
    private fun updateNowPlayingFragment(context: Context){
        Glide.with(context)
            .load(MusicPlayer.musicListPA[MusicPlayer.songPosition].artUri)
            .apply(RequestOptions().placeholder(R.drawable.artboard_2).centerCrop())
            .into(NowPlaying.binding.imageNP)

        NowPlaying.binding.nameNP.text = MusicPlayer.musicListPA[MusicPlayer.songPosition].title
    }

}