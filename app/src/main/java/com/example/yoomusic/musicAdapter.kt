package com.example.yoomusic

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.yoomusic.databinding.MusicItemViewBinding

class MusicAdapter(private val context:Context,private var musicList:ArrayList<Music>,private val fromPlaylist:Boolean=false,private val addSong:Boolean = false,private val removeSong:Boolean = false,private val listener: playlistHolderAdapter.OnItemClickListener,private val fromFvt:Boolean = false): RecyclerView.Adapter<MusicAdapter.MyHolder>() {
    class MyHolder(binding: MusicItemViewBinding):RecyclerView.ViewHolder(binding.root) {
        val title = binding.musicItemName
        val album = binding.musicItemAlbumName
        val image = binding.musicItemImage
        val duration = binding.musicItemDuration
        val root = binding.root


    }

    interface OnItemClickListener {
        fun onItemClicked(position: Int)
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

        if (position == musicList.size - 1) {
            val params = holder.itemView.layoutParams as RecyclerView.LayoutParams
//            convert it to dp
            val bottomMarginPx = (135 * Resources.getSystem().displayMetrics.density).toInt()
            params.bottomMargin = bottomMarginPx // last item bottom margin
            holder.itemView.layoutParams = params
        } else {
            val params = holder.itemView.layoutParams as RecyclerView.LayoutParams
            val bottomMarginPx = (10 * Resources.getSystem().displayMetrics.density).toInt()

            params.bottomMargin = bottomMarginPx // other items bottom margin
            holder.itemView.layoutParams = params
        }

        holder.root.setOnClickListener {


//            val manager: FragmentManager = (context as AppCompatActivity).supportFragmentManager
//            manager.beginTransaction().replace(R.id.frame_container, musicPlayerFragment()).commit()

            when{
                fromPlaylist ->{
                    sendIntent("PlaylistAdapter",position)
                }
                addSong ->{

                    if(songCanBeAdded(musicList[position])){
                        holder.root.setBackgroundColor(ContextCompat.getColor(context,R.color.dark1))


                    }
                    else{
                        holder.root.setBackgroundColor(ContextCompat.getColor(context,R.color.black))

                    }
                    listener.onItemClicked(position)
                }
                removeSong ->{

                    if(isSelected(musicList[position],position)){
                        holder.root.setBackgroundColor(ContextCompat.getColor(context,R.color.dark1))
                    }
                    else
                    {
                        holder.root.setBackgroundColor(ContextCompat.getColor(context,R.color.black))
                    }
                    listener.onItemClicked(position)

                }

                else->{
                    when{
//                        musicList[position].id == MusicPlayer.nowPlayingId -> sendIntent("NowPlaying",MusicPlayer.songPosition)
                        searchFragment.search -> sendIntent("searchAdapter",position)
                        favourite.isClicked -> sendIntent("FavouriteAdapter",position)

                        else->sendIntent("MusicAdapter",position)
                    }
                }
            }







        }
    }

    override fun getItemCount(): Int {
        return musicList.size
    }

    @SuppressLint("NotifyDataSetChanged", "SetTextI18n")
    fun updateMusicList(searchList: ArrayList<Music>,view:TextView?) {
        musicList = ArrayList()
        musicList.addAll(searchList)
        view?.text = "${musicList.size} Songs"
        notifyDataSetChanged()
    }

    private fun sendIntent(ref: String, pos: Int) {
        val intent = Intent(context,MusicPlayer::class.java)
        intent.putExtra("index",pos)
        intent.putExtra("class",ref)
        ContextCompat.startActivity(context,intent,null)
    }

    private fun songCanBeAdded(song:Music) : Boolean{

        if(fromFvt){

            favourite.fvtItemList.forEachIndexed{index,music ->
                if(music.id == song.id){
                    Toast.makeText(context,"Song Already Added",Toast.LENGTH_SHORT).show()
                    return false
                }
            }

        }
        else{
            playListFragment.playlistList.list[playlist_details.playlistPosition].playlist.forEachIndexed{index,music ->
                if(music.id == song.id){
                    Toast.makeText(context,"Song Already Added",Toast.LENGTH_SHORT).show()
                    return false
                }
            }
        }

        AddSongs.tempMusicList.forEachIndexed { index,music->
            if(music.id == song.id){
                AddSongs.tempMusicList.removeAt(index)
                return false
            }
        }
//        playListFragment.playlistList.list[playlist_details.playlistPosition].playlist.add(song)
//        song.dateAddedToList = System.currentTimeMillis()
        AddSongs.tempMusicList.add(song)

        return true
    }

    private fun isSelected(song: Music,position: Int) : Boolean {

        Remove_songs.tempMusicList.forEach { music ->
            if (music.id == song.id) {
                Remove_songs.tempMusicList.remove(music)
                return false
            }

        }
        Remove_songs.tempMusicList.add(song)
        return true
    }

}