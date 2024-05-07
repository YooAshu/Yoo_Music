package com.example.yoomusic

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.yoomusic.databinding.MusicItemViewBinding

class MusicAdapter(private val context:Context,private var musicList:ArrayList<Music>): RecyclerView.Adapter<MusicAdapter.MyHolder>() {
    class MyHolder(binding: MusicItemViewBinding):RecyclerView.ViewHolder(binding.root) {
        val title = binding.musicItemName
        val album = binding.musicItemAlbumName
        val image = binding.musicItemImage
        val duration = binding.musicItemDuration
        val root = binding.root


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MusicAdapter.MyHolder {
        return MyHolder(MusicItemViewBinding.inflate(LayoutInflater.from(context),parent,false))
    }

    override fun onBindViewHolder(holder: MusicAdapter.MyHolder, position: Int) {
        holder.title.text = musicList[position].title
        holder.album.text = musicList[position].album
        holder.duration.text = formatDuration(musicList[position].duration)
        Glide.with(context)
            .load(musicList[position].artUri)
            .apply(RequestOptions().placeholder(R.drawable.artboard_2).centerCrop())
            .into(holder.image)

        holder.root.setOnClickListener {


//            val manager: FragmentManager = (context as AppCompatActivity).supportFragmentManager
//            manager.beginTransaction().replace(R.id.frame_container, musicPlayerFragment()).commit()

            when{
                searchFragment.search -> sendIntent("searchAdapter",position)

                else->sendIntent("MusicAdapter",position)
            }



        }
    }

    override fun getItemCount(): Int {
        return musicList.size
    }

    fun updateMusicList(searchList: ArrayList<Music>) {
        musicList = ArrayList()
        musicList.addAll(searchList)
        notifyDataSetChanged()
    }

    private fun sendIntent(ref: String, pos: Int) {
        val intent = Intent(context,MusicPlayer::class.java)
        intent.putExtra("index",pos)
        intent.putExtra("class",ref)
        ContextCompat.startActivity(context,intent,null)
    }
}