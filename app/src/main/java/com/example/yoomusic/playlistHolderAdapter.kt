package com.example.yoomusic

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.yoomusic.databinding.PlaylistFolderViewBinding

class playlistHolderAdapter(private val context: Context, private var playlistList:ArrayList<Playlist>,private val listener: OnItemClickListener): RecyclerView.Adapter<playlistHolderAdapter.MyHolder>() {

    interface OnItemClickListener {
        fun onItemClicked(position: Int)
    }
    class MyHolder(binding: PlaylistFolderViewBinding): RecyclerView.ViewHolder(binding.root) {
        val title = binding.playlistTitle
        val image = binding.playlistImage
        val root = binding.root


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        return MyHolder(PlaylistFolderViewBinding.inflate(LayoutInflater.from(context),parent,false))
    }



    override fun onBindViewHolder(holder: MyHolder, position: Int) {
        holder.title.text = playlistList[position].name
        holder.title.isSelected = true
        if(playListFragment.playlistList.list[position].playlist.size>0){
            Glide.with(context)
                .load(playListFragment.playlistList.list[position].playlist[0].artUri)
                .apply(RequestOptions().placeholder(R.drawable.artboard_2).centerCrop())
                .into(holder.image)
        }

//        if (position == itemCount - 1) {
//            val params = holder.itemView.layoutParams as RecyclerView.LayoutParams
//            params.bottomMargin = 150 // last item bottom margin
//            holder.itemView.layoutParams = params
//        }


        if (position == playlistList.size - 1) {
            val params = holder.itemView.layoutParams as RecyclerView.LayoutParams
//            convert it to dp
            val bottomMarginPx = (70 * Resources.getSystem().displayMetrics.density).toInt()
            params.bottomMargin = bottomMarginPx // last item bottom margin
            holder.itemView.layoutParams = params
        } else {
            val params = holder.itemView.layoutParams as RecyclerView.LayoutParams
            val bottomMarginPx = (5 * Resources.getSystem().displayMetrics.density).toInt()

            params.bottomMargin = bottomMarginPx // other items bottom margin
            holder.itemView.layoutParams = params
        }

//        on click of playlist folder
        holder.root.setOnClickListener {

            listener.onItemClicked(position)


        }
    }



    override fun getItemCount(): Int {
        return playlistList.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updatePlaylistList() {
        playlistList = ArrayList()
        playlistList.addAll(playListFragment.playlistList.list)
        notifyDataSetChanged()
    }





}

