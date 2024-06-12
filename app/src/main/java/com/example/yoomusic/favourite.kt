package com.example.yoomusic

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.yoomusic.databinding.FragmentFavouriteBinding
import com.example.yoomusic.databinding.FragmentNowPlayingBinding


class favourite : Fragment() {


    private lateinit var musicAdapter: MusicAdapter
    companion object{
        @SuppressLint("StaticFieldLeak")
        lateinit var binding : FragmentFavouriteBinding
        var fvtItemList : ArrayList<Music> = ArrayList()
        var isClicked : Boolean = false
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_favourite, container, false)
        binding = FragmentFavouriteBinding.bind(view)

//        MainActivity.binding.bottomNavigation.selectedItemId = R.id.playlist
//        MainActivity.bottomNavigationView.selectedItemId = R.id.playlist
        MainActivity.bottomNavigationView.menu.getItem(2).isChecked = true



        loadMusicList()


        return view
    }

    private fun loadMusicList(){

//       musicListSearch = HomeFragment().getAllAudio()
        isClicked  = true
        searchFragment.search = false

        binding.fvtRv.setHasFixedSize(true)
        binding.fvtRv.setItemViewCacheSize(13)
        binding.fvtRv.layoutManager = LinearLayoutManager(requireContext())
        musicAdapter = MusicAdapter(requireContext(), fvtItemList)
        binding.fvtRv.adapter = musicAdapter


    }


}