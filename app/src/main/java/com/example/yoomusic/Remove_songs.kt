package com.example.yoomusic

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.yoomusic.databinding.ActivityRemoveSongsBinding
import com.google.gson.GsonBuilder

class Remove_songs : AppCompatActivity() , playlistHolderAdapter.OnItemClickListener {

    private var removingFirstSong = true
    @SuppressLint("StaticFieldLeak")
    companion object{


        lateinit var tempMusicList: ArrayList<Music>
        lateinit var binding: ActivityRemoveSongsBinding
        lateinit var adapter: MusicAdapter
    }
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_remove_songs)
        binding = ActivityRemoveSongsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadMusicList()
        when(intent.getStringExtra("fromFvt")){
            "true"->{

            }
            else->{

                binding.playlistName.text = playListFragment.playlistList.list[playlist_details.playlistPosition].name
            }
        }

        binding.backBtn.setOnClickListener { finish() }

        tempMusicList = ArrayList()

        binding.remove.setOnClickListener {
            if(tempMusicList.size>=1){
                val editor = getSharedPreferences("FAVOURITES", MODE_PRIVATE).edit()

                when(intent.getStringExtra("fromFvt")){
                    "true"->{
                        tempMusicList.forEach { music ->
                            favourite.fvtItemList.remove(music)
                            favourite.musicAdapter.updateMusicList(favourite.fvtItemList,favourite.binding.totalFvtSongCount)

                            val jsonString = GsonBuilder().create().toJson(favourite.fvtItemList)
                            editor.putString("fvtSongs", jsonString)
                            editor.apply()

                        }
                    }
                    else->{
                        tempMusicList.forEach { music ->
                            playListFragment.playlistList.list[playlist_details.playlistPosition].playlist.remove(music)
                            playlist_details.binding.totalSongCount.text = "${playListFragment.playlistList.list[playlist_details.playlistPosition].playlist.size} songs"

                            val jsonStringPlaylist = GsonBuilder().create().toJson(playListFragment.playlistList)
                            editor.putString("MusicPlaylist", jsonStringPlaylist)
                            editor.apply()
                        }
                    }
                }

                tempMusicList.clear()


                finish()
            }
        }

    }

    private fun loadMusicList(){

//        musicList.addAll(HomeFragment.musicListHome)

        binding.removeSongRV.setHasFixedSize(true)
        binding.removeSongRV.setItemViewCacheSize(13)
        binding.removeSongRV.layoutManager = LinearLayoutManager(this)
        adapter = when(intent.getStringExtra("fromFvt")){
            "true"->{
                MusicAdapter(this, favourite.fvtItemList, removeSong = true , fromFvt = true, listener = this)
            }

            else->{
                MusicAdapter(this, playListFragment.playlistList.list[playlist_details.playlistPosition].playlist, removeSong = true , listener = this)
            }
        }
        binding.removeSongRV.adapter = adapter


    }

    override fun onItemClicked(position: Int) {



        if(tempMusicList.size==1 && removingFirstSong){
            binding.remove.startAnimation(AnimationUtils.loadAnimation(this,R.anim.slide_up))

            binding.remove.visibility = android.view.View.VISIBLE
            removingFirstSong = false

        }
        else if(tempMusicList.size<1){
            binding.remove.startAnimation(AnimationUtils.loadAnimation(this,R.anim.slide_down))

            binding.remove.visibility = android.view.View.GONE
            removingFirstSong = true
        }
    }
}