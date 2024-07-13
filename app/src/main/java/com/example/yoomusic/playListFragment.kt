package com.example.yoomusic

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.yoomusic.databinding.AddPlaylistBinding
import com.example.yoomusic.databinding.FragmentNowPlayingBinding
import com.example.yoomusic.databinding.FragmentPlayListBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.GsonBuilder
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


class playListFragment : Fragment() , playlistHolderAdapter.OnItemClickListener{


    @SuppressLint("StaticFieldLeak")
    companion object{
        @SuppressLint("StaticFieldLeak")
        lateinit var binding : FragmentPlayListBinding
        private lateinit var adapter: playlistHolderAdapter
        private lateinit var dialog : Dialog
        var playlistList : PlaylistLists= PlaylistLists()
        var tempPlaylist :Playlist = Playlist()
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        var view = inflater.inflate(R.layout.fragment_play_list, container, false)
        binding = FragmentPlayListBinding.bind(view)


        MainActivity.bottomNavigationView.menu.getItem(2).isChecked = true

        binding.playlistFvt.setOnClickListener {

            replaceFragment(favourite(),"playlist")
            true
        }



//        creating object of adapter of playlist holder


        binding.playlistHolder.setHasFixedSize(true)
        binding.playlistHolder.setItemViewCacheSize(13)
        binding.playlistHolder.layoutManager = LinearLayoutManager(requireContext())
        adapter = playlistHolderAdapter(requireContext(), playlistList = playlistList.list,this)
        binding.playlistHolder.adapter = adapter

//        adding custom dialogue for adding playlist
        binding.addPlaylist.setOnClickListener {
            customDialogue()

        }

        return view
    }


    private fun replaceFragment(fragment: Fragment,tag: String? = null) {
        requireActivity().supportFragmentManager.beginTransaction().replace(R.id.frame_container, fragment,tag).addToBackStack(tag).commit()
        // Clear the back stack


    }

    @SuppressLint("SimpleDateFormat")
    private fun customDialogue(){
        val customDialog = LayoutInflater.from(requireContext()).inflate(R.layout.add_playlist,
            binding.root, false)

        val builder = MaterialAlertDialogBuilder(requireContext(),R.style.MyRounded_MaterialComponents_MaterialAlertDialog)
        val binder = AddPlaylistBinding.bind(customDialog)


        builder.setView(customDialog)
            .setMessage("Enter Playlist Name")

            .setPositiveButton("Add") { dialog, _ ->
                val playlistName = binder.playlistName.text.toString()
                if(playlistName.isNotEmpty()){

                    addPlaylist(playlistName)
                }
                else{
                    Toast.makeText(requireContext(),"Enter Playlist Name",Toast.LENGTH_SHORT).show()
                }

                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
        val dialog = builder.create()
        // Apply the animation to the dialog view
        dialog.window?.attributes?.windowAnimations = R.style.addPlaylistDialogAnimation
        dialog.show()

        val messageTextView = dialog.findViewById<TextView>(android.R.id.message) // Get message TextView
        messageTextView?.setTextAppearance(R.style.MyAlertDialogMessageStyle) // Apply custom style

    }
    @SuppressLint("SimpleDateFormat")
    private fun addPlaylist(playlistName : String){

        var playlistExist = false
        for(i in playlistList.list){
            if(i.name == playlistName){
                playlistExist = true
                break
            }
        }
        if(playlistExist){
            Toast.makeText(requireContext(),"playlist $playlistName already exist",Toast.LENGTH_SHORT).show()
        }
        else{
            tempPlaylist = Playlist()
            tempPlaylist.name = playlistName
            tempPlaylist.playlist = ArrayList()
            val calendar = Calendar.getInstance().time
            val sdf = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)
            tempPlaylist.createdOn = sdf.format(calendar)
            playlistList.list.add(tempPlaylist)
            adapter.updatePlaylistList()

//            store playlist data
            val editor = requireActivity().getSharedPreferences("FAVOURITES", Context.MODE_PRIVATE).edit()
            val jsonStringPlaylist = GsonBuilder().create().toJson(playListFragment.playlistList)
            editor.putString("MusicPlaylist", jsonStringPlaylist)
            editor.apply()
        }



    }

    override fun onItemClicked(position: Int) {

        val newFragment = playlist_details()

        // Optionally, pass data to the new fragment
        val bundle = Bundle()
        bundle.putInt("position", position)
        newFragment.arguments = bundle

        parentFragmentManager.beginTransaction()
            .replace(R.id.frame_container, newFragment)
            .addToBackStack(null)
            .commit()
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onResume() {
        super.onResume()
        val editor = requireActivity().getSharedPreferences("FAVOURITES", AppCompatActivity.MODE_PRIVATE).edit()

        val jsonString = GsonBuilder().create().toJson(favourite.fvtItemList)
        editor.putString("fvtSongs", jsonString)

        val jsonStringPlaylist = GsonBuilder().create().toJson(playListFragment.playlistList)
        editor.putString("MusicPlaylist", jsonStringPlaylist)

        editor.apply()
        adapter.notifyDataSetChanged()
    }


}