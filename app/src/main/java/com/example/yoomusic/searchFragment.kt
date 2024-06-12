package com.example.yoomusic

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.yoomusic.databinding.FragmentHomeBinding
import com.example.yoomusic.databinding.FragmentSearchBinding

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [searchFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class searchFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var binding: FragmentSearchBinding
    private lateinit var musicAdapter: MusicAdapter

//    lateinit var musicListSearch :ArrayList<Music>
//    lateinit var searchItemList : ArrayList<Music>

    private var functionCalled = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentSearchBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        MainActivity.bottomNavigationView.menu.getItem(1).isChecked = true

        if (!functionCalled) {
            // Perform some initialization here
            loadMusicList()
            functionCalled = true
        }

        binding.searchView.setOnQueryTextListener(object : android.widget.SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean = true

            override fun onQueryTextChange(newText: String?): Boolean {

                searchItemList = ArrayList()

                if(newText!=null){
                    val userInput = newText.lowercase()
                    for (song in HomeFragment.musicListHome){
                        if(song.title.lowercase().contains(userInput)){
                            searchItemList.add(song)
                        }
                    }
                    search = true
                    musicAdapter.updateMusicList(searchItemList)

                }
                return true
            }

        })

    }


    private fun loadMusicList(){

//        musicListSearch = HomeFragment().getAllAudio()
        search = false
        favourite.isClicked = false
        searchItemList = ArrayList()

        binding.searchRV.setHasFixedSize(true)
        binding.searchRV.setItemViewCacheSize(13)
        binding.searchRV.layoutManager = LinearLayoutManager(requireContext())
        musicAdapter = MusicAdapter(requireContext(), searchItemList)
        binding.searchRV.adapter = musicAdapter


    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment searchFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        var search : Boolean = false
        lateinit var searchItemList : ArrayList<Music>
        fun newInstance(param1: String, param2: String) =
            searchFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}