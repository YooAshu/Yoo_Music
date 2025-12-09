package com.example.yoomusic

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.yoomusic.databinding.ActivityAddSongsBinding
import com.example.yoomusic.databinding.ActivityAddToPlaylistBinding
import com.google.gson.GsonBuilder

class AddToPlaylist : AppCompatActivity(), playlistHolderAdapter.OnItemClickListener {

    @SuppressLint("StaticFieldLeak")
    companion object {
        lateinit var binding: ActivityAddToPlaylistBinding
        lateinit var adapter: playlistHolderAdapter
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_to_playlist)
        binding = ActivityAddToPlaylistBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.addToPlaylistRV.setHasFixedSize(true)
        binding.addToPlaylistRV.setItemViewCacheSize(13)
        binding.addToPlaylistRV.layoutManager = LinearLayoutManager(this)
        adapter = playlistHolderAdapter(this, playlistList = playListFragment.playlistList.list,this)
        binding.addToPlaylistRV.adapter = adapter

        if (playListFragment.playlistList.list.isEmpty()){
            binding.isThereArePlayList.text = "No Playlist Found"

        }
    }

    @SuppressLint("CommitPrefEdits", "SetTextI18n")
    override fun onItemClicked(position: Int) {
        val song = MusicPlayer.musicListPA[MusicPlayer.songPosition]
        song.dateAddedToList = System.currentTimeMillis()
        var songExist = false
        playListFragment.playlistList.list[position].playlist.forEach { music->
            if(music.id==song.id){
                songExist = true
            }
        }
        if(!songExist){
            playListFragment.playlistList.list[position].playlist.add(song)
            val editor = getSharedPreferences("FAVOURITES", MODE_PRIVATE).edit()
            val jsonStringPlaylist = GsonBuilder().create().toJson(playListFragment.playlistList)
//            playlist_details.binding.totalSongCount.text = "${playListFragment.playlistList.list[playlist_details.playlistPosition].playlist.size} songs"
            editor.putString("MusicPlaylist", jsonStringPlaylist)
            editor.apply()
            Toast.makeText(this, "Song added to playlist", Toast.LENGTH_SHORT).show()
        }
        else{
            Toast.makeText(this, "Song already exist in this playlist", Toast.LENGTH_SHORT).show()
        }

        finish()
    }
}