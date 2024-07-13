package com.example.yoomusic

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.ViewGroup
import android.view.Window
import android.view.animation.AnimationUtils
import android.widget.RelativeLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.yoomusic.databinding.ActivityAddSongsBinding
import com.google.gson.GsonBuilder

class AddSongs(private var fromFvt: Boolean = false) : AppCompatActivity(), playlistHolderAdapter.OnItemClickListener {



    lateinit var adapter: MusicAdapter
    lateinit var musicList: ArrayList<Music>
    private var addingFirstSong = true


    @SuppressLint("StaticFieldLeak")
    companion object{

        lateinit var tempMusicList: ArrayList<Music>
        lateinit var binding: ActivityAddSongsBinding

    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_songs)
        binding = ActivityAddSongsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        musicList = ArrayList()

        loadMusicList()

        binding.searchView.setOnQueryTextListener(object : android.widget.SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean = true

            override fun onQueryTextChange(newText: String?): Boolean {

               musicList = ArrayList()

                if(newText!=null){
                    val userInput = newText.lowercase()
                    for (song in HomeFragment.musicListHome){
                        if(song.title.lowercase().contains(userInput)){
                            musicList.add(song)
                        }
                    }
                    adapter.updateMusicList(musicList,null)

                }
                return true
            }

        })


        when(intent.getStringExtra("fromFvt")){

            "true"->{
//
            }

            else->{
                binding.playlistName.text = playListFragment.playlistList.list[playlist_details.playlistPosition].name
            }
        }

//        if(!fromFvt){
//
//
//        }

        binding.backBtn.setOnClickListener { finish() }

        tempMusicList = ArrayList()

        binding.add.setOnClickListener {


            if(tempMusicList.size>=1){
                val editor = getSharedPreferences("FAVOURITES", MODE_PRIVATE).edit()

                when(intent.getStringExtra("fromFvt")){
                    "true" ->{
                        for (i in tempMusicList.size - 1 downTo 0) {
                            tempMusicList[i].dateAddedToList = System.currentTimeMillis()
                            favourite.fvtItemList.add(0,tempMusicList[i])
                            favourite.musicAdapter.updateMusicList(favourite.fvtItemList,favourite.binding.totalFvtSongCount)
                        }


                        val jsonString = GsonBuilder().create().toJson(favourite.fvtItemList)
                        editor.putString("fvtSongs", jsonString)
                        editor.apply()
                    }

                    else->{
                        for (i in tempMusicList.size - 1 downTo 0) {
                            tempMusicList[i].dateAddedToList = System.currentTimeMillis()
                            playListFragment.playlistList.list[playlist_details.playlistPosition].playlist.add(0,tempMusicList[i])
//                            playlist_details.adapter.updateMusicList(playListFragment.playlistList.list[playlist_details.playlistPosition].playlist,playlist_details.binding.totalSongCount)
                        }
//                        playListFragment.playlistList.list[playlist_details.playlistPosition].playlist.addAll(
//                            tempMusicList)
                        playlist_details.binding.totalSongCount.text = "${playListFragment.playlistList.list[playlist_details.playlistPosition].playlist.size} songs"

                        val jsonStringPlaylist = GsonBuilder().create().toJson(playListFragment.playlistList)
                        editor.putString("MusicPlaylist", jsonStringPlaylist)
                        editor.apply()
                    }
                }

                tempMusicList.clear()
                finish()
            }

        }




    }

    private fun loadMusicList(){

        musicList.addAll(HomeFragment.musicListHome)

        binding.addSongRV.setHasFixedSize(true)
        binding.addSongRV.setItemViewCacheSize(13)
        binding.addSongRV.layoutManager = LinearLayoutManager(this)
        adapter = when(intent.getStringExtra("fromFvt")){
            "true" ->{
                MusicAdapter(this, musicList, addSong = true, fromFvt = true, listener = this)
            }

            else->{
                MusicAdapter(this, musicList, addSong = true, listener = this)
            }
        }

        binding.addSongRV.adapter = adapter


    }




    override fun onItemClicked(position: Int) {

        if(tempMusicList.size==1 && addingFirstSong){
            binding.add.startAnimation(AnimationUtils.loadAnimation(this,R.anim.slide_up))

            binding.add.visibility = android.view.View.VISIBLE
            addingFirstSong = false

        }
        else if(tempMusicList.size<1){
            binding.add.startAnimation(AnimationUtils.loadAnimation(this,R.anim.slide_down))

            binding.add.visibility = android.view.View.GONE
            addingFirstSong = true
        }
    }


}