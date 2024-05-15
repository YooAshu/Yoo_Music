package com.example.yoomusic

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.yoomusic.databinding.FragmentNowPlayingBinding

class NowPlaying : Fragment() {

    companion object{
        @SuppressLint("StaticFieldLeak")
        lateinit var binding : FragmentNowPlayingBinding
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        var view = inflater.inflate(R.layout.fragment_now_playing, container, false)
        binding = FragmentNowPlayingBinding.bind(view)
        binding.nameNP.isSelected = true
        binding.root.visibility = View.INVISIBLE
        binding.playPauseNP.setOnClickListener{
            if(MusicPlayer.isPlaying) pauseMusic() else playMusic()
        }
        binding.nextNP.setOnClickListener {

            if (MusicPlayer.songPosition == MusicPlayer.musicListPA.size - 1) {
                MusicPlayer.songPosition = 0
            } else {
                MusicPlayer.songPosition += 1
            }

            Glide.with(this)
                .load(MusicPlayer.musicListPA[MusicPlayer.songPosition].artUri)
                .apply(RequestOptions().placeholder(R.drawable.artboard_2).centerCrop())
                .into(binding.imageNP)

            binding.nameNP.text = MusicPlayer.musicListPA[MusicPlayer.songPosition].title
            if (MusicPlayer.isPlaying) {
                MusicPlayer.musicService!!.createMediaPlayer(playState = true)
            } else {
                MusicPlayer.musicService!!.createMediaPlayer(playState = false)
            }
        }

        binding.root.setOnClickListener {
            val intent = Intent(requireContext(),MusicPlayer::class.java)
            intent.putExtra("index",MusicPlayer.songPosition)
            intent.putExtra("class","NowPlaying")
            ContextCompat.startActivity(requireContext(),intent,null)
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        if(MusicPlayer.musicService!=null){
            binding.root.visibility = View.VISIBLE
            Glide.with(this)
                .load(MusicPlayer.musicListPA[MusicPlayer.songPosition].artUri)
                .apply(RequestOptions().placeholder(R.drawable.artboard_2).centerCrop())
                .into(binding.imageNP)

            binding.nameNP.text = MusicPlayer.musicListPA[MusicPlayer.songPosition].title
            if(MusicPlayer.isPlaying){
                binding.playPauseNP.setImageResource(R.drawable.pause_np)
            }else{
                binding.playPauseNP.setImageResource(R.drawable.play_np)
            }




        }
    }
     fun updateNP(){
//        Glide.with(this)
//            .load(MusicPlayer.musicListPA[MusicPlayer.songPosition].artUri)
//            .apply(RequestOptions().placeholder(R.drawable.artboard_2).centerCrop())
//            .into(binding.imageNP)

        binding.nameNP.text = MusicPlayer.musicListPA[MusicPlayer.songPosition].title
    }

    private fun playMusic(){
        MusicPlayer.musicService!!.mediaPlayer!!.start()
        binding.playPauseNP.setImageResource(R.drawable.pause_np)
        MusicPlayer.musicService!!.showNotification(R.drawable.pause_svgrepo_com)
        MusicPlayer.binding.ButtonPlayPause.setImageResource(R.drawable.pause_btn)
        MusicPlayer.isPlaying = true


    }
    private fun pauseMusic(){

        MusicPlayer.musicService!!.mediaPlayer!!.pause()
        binding.playPauseNP.setImageResource(R.drawable.play_np)
        MusicPlayer.musicService!!.showNotification(R.drawable.play_svgrepo_com)
        MusicPlayer.binding.ButtonPlayPause.setImageResource(R.drawable.play_btn)
        MusicPlayer.isPlaying = false

    }

}


//<androidx.fragment.app.FragmentContainerView
//android:id="@+id/nowPlaying"
//android:name="com.example.yoomusic.NowPlaying"
//android:layout_width="match_parent"
//android:layout_height="wrap_content"
//android:layout_marginBottom="35dp"
//app:layout_constraintBottom_toBottomOf="parent"
//app:layout_constraintEnd_toEndOf="parent"
//app:layout_constraintStart_toStartOf="parent"
//tools:layout="@layout/fragment_now_playing" />