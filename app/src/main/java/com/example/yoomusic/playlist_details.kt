package com.example.yoomusic

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.yoomusic.databinding.AddPlaylistBinding
import com.example.yoomusic.databinding.FragmentPlaylistDetailsBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.GsonBuilder
import jp.wasabeef.glide.transformations.BlurTransformation


class playlist_details : Fragment(), playlistHolderAdapter.OnItemClickListener {


    @SuppressLint("StaticFieldLeak")
    companion object{
       lateinit var binding : FragmentPlaylistDetailsBinding
       lateinit var adapter: MusicAdapter
       var playlistPosition: Int = -1
        var sortingOrder = 3
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_playlist_details, container, false)
        binding = FragmentPlaylistDetailsBinding.bind(view)



        MainActivity.bottomNavigationView.menu.getItem(2).isChecked = true


       // marquu text
        binding.playlistName.isSelected = true

        playlistPosition = arguments?.getInt("position")!!
        //        check if any song is deleted
//        playListFragment.playlistList.list[playlistPosition].playlist= checkSongs(playListFragment.playlistList.list[playlistPosition].playlist)

        loadMusicList()

        binding.shufflePlaylist.setOnClickListener {

            if(playListFragment.playlistList.list[playlistPosition].playlist.isNotEmpty()){

                val intent = Intent(requireContext(), MusicPlayer::class.java)
                intent.putExtra("index", 0)
                intent.putExtra("class", "playlistShuffled")
                startActivity(intent)

            }
            else{
                Toast.makeText(requireContext(), "Nothing to shuffle", Toast.LENGTH_SHORT).show()
            }

        }
        binding.totalSongCount.text = "${adapter.getItemCount()} Songs"

        binding.threeDots.setOnClickListener {
            showbottomDialog()
        }

        binding.backBtn.setOnClickListener {
            requireActivity().onBackPressed()
        }

        return view
    }



    private fun loadMusicList(){





        binding.playlistRv.setHasFixedSize(true)
        binding.playlistRv.setItemViewCacheSize(13)
        binding.playlistRv.layoutManager = LinearLayoutManager(requireContext())
        adapter = MusicAdapter(requireContext(), playListFragment.playlistList.list[playlistPosition].playlist, fromPlaylist = true, listener = this)
        binding.playlistRv.adapter = adapter


    }

    @SuppressLint("NotifyDataSetChanged", "CommitPrefEdits", "SetTextI18n")
    override fun onResume() {
        super.onResume()
        binding.playlistName.text = playListFragment.playlistList.list[playlistPosition].name
        if(adapter.itemCount>0){
            Glide.with(this)
                .load(playListFragment.playlistList.list[playlistPosition].playlist[0].artUri)
                .apply(RequestOptions().placeholder(R.drawable.artboard_2).centerCrop())
                .into(binding.roundedImage)


            Glide.with(this).load(playListFragment.playlistList.list[playlistPosition].playlist[0].artUri)
                .apply(RequestOptions().placeholder(R.drawable.artboard_2).centerCrop())
                .apply(RequestOptions.bitmapTransform(BlurTransformation(25, 3)))
                .into(binding.bannerImage)



        }

        if(playListFragment.playlistList.list[playlistPosition].playlist.isNotEmpty()){
            when(sortingOrder){
                0 -> playListFragment.playlistList.list[playlistPosition].playlist.sortBy { it.title.lowercase() }
                1 -> playListFragment.playlistList.list[playlistPosition].playlist.sortByDescending { it.dateAdded }
                2-> playListFragment.playlistList.list[playlistPosition].playlist.sortBy { it.dateAdded }
                else-> playListFragment.playlistList.list[playlistPosition].playlist.sortByDescending { it.dateAddedToList }
            }

        }

        adapter.notifyDataSetChanged()
        binding.totalSongCount.text = "${adapter.getItemCount()} Songs"

    }

    private fun showbottomDialog(){

        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.playlist_option_dialogue)


        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.attributes?.windowAnimations = R.style.bottomDialogAnimation
        dialog.window?.setGravity(Gravity.BOTTOM)
        dialog.show()

        dialog.findViewById<RelativeLayout>(R.id.add_song).setOnClickListener {
            startActivity(Intent(requireContext(), AddSongs::class.java))
            dialog.dismiss()
        }
        dialog.findViewById<RelativeLayout>(R.id.remove_song).setOnClickListener {
            startActivity(Intent(requireContext(), Remove_songs::class.java))
            dialog.dismiss()
        }
        dialog.findViewById<RelativeLayout>(R.id.sort_by).setOnClickListener {
            dialog.dismiss()
            showSortbyDialog()
        }
        dialog.findViewById<RelativeLayout>(R.id.edit_playlist).setOnClickListener {
            dialog.dismiss()
            customDialogue()

        }
        dialog.findViewById<RelativeLayout>(R.id.delete_playlist).setOnClickListener {
            dialog.dismiss()
            customDialogueForDelete()

        }

    }

    override fun onItemClicked(position: Int) {
        TODO("Not yet implemented")
    }


    @SuppressLint("NotifyDataSetChanged")
    private fun showSortbyDialog(){

        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.sortby_playist_option_dialogue)


        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.attributes?.windowAnimations = R.style.bottomDialogAnimation
        dialog.window?.setGravity(Gravity.BOTTOM)
        dialog.show()

        val editor = requireActivity().getSharedPreferences("SORTING", AppCompatActivity.MODE_PRIVATE).edit()

        dialog.findViewById<RelativeLayout>(R.id.by_name).setOnClickListener {
            if(playListFragment.playlistList.list[playlistPosition].playlist.isNotEmpty() && sortingOrder !=0) {
                sortingOrder = 0
                playListFragment.playlistList.list[playlistPosition].playlist.sortBy { it.title.lowercase() }
//                adapter.updateMusicList(playListFragment.playlistList.list[playlistPosition].playlist,null)

                adapter.notifyDataSetChanged()
                val jsonString = GsonBuilder().create().toJson(0)
                editor.putString("playList", jsonString)
                editor.apply()
            }

            dialog.dismiss()
        }
        dialog.findViewById<RelativeLayout>(R.id.by_new_first).setOnClickListener {

            if(playListFragment.playlistList.list[playlistPosition].playlist.isNotEmpty() && sortingOrder !=1){
                sortingOrder = 1
                playListFragment.playlistList.list[playlistPosition].playlist.sortByDescending { it.dateAdded }
//                adapter.updateMusicList(playListFragment.playlistList.list[playlistPosition].playlist,null)
                adapter.notifyDataSetChanged()
                val jsonString = GsonBuilder().create().toJson(1)
                editor.putString("playList", jsonString)
                editor.apply()
            }



            dialog.dismiss()
        }
        dialog.findViewById<RelativeLayout>(R.id.by_old_first).setOnClickListener {

            if(playListFragment.playlistList.list[playlistPosition].playlist.isNotEmpty() && sortingOrder !=2) {
                sortingOrder = 2
                playListFragment.playlistList.list[playlistPosition].playlist.sortBy { it.dateAdded }
//                adapter.updateMusicList(playListFragment.playlistList.list[playlistPosition].playlist,null)
                adapter.notifyDataSetChanged()
                val jsonString = GsonBuilder().create().toJson(2)
                editor.putString("playList", jsonString)
                editor.apply()
            }

            dialog.dismiss()
        }
        dialog.findViewById<RelativeLayout>(R.id.by_recently_added).setOnClickListener {
            if(playListFragment.playlistList.list[playlistPosition].playlist.isNotEmpty() && sortingOrder !=3){
                sortingOrder = 3
                playListFragment.playlistList.list[playlistPosition].playlist.sortByDescending { it.dateAddedToList }
//                adapter.updateMusicList(playListFragment.playlistList.list[playlistPosition].playlist,null)
                adapter.notifyDataSetChanged()
                val jsonString = GsonBuilder().create().toJson(3)
                editor.putString("playList", jsonString)
                editor.apply()

            }

            dialog.dismiss()
        }

    }

    @SuppressLint("SimpleDateFormat")
    private fun customDialogue(){
        val customDialog = LayoutInflater.from(requireContext()).inflate(R.layout.add_playlist,
            playListFragment.binding.root, false)

        val builder = MaterialAlertDialogBuilder(requireContext(),R.style.MyRounded_MaterialComponents_MaterialAlertDialog)
        val binder = AddPlaylistBinding.bind(customDialog)


        binder.playlistName.setText(playListFragment.playlistList.list[playlistPosition].name)

        builder.setView(customDialog)
            .setMessage("Rename Playlist")

            .setPositiveButton("Rename") { dialog, _ ->
                val playlistName = binder.playlistName.text.toString()
                if(playlistName.isNotEmpty()){


                    var playlistExist = false
                    for(i in playListFragment.playlistList.list){
                        if(i.name == playlistName){
                            playlistExist = true
                            break
                        }
                    }
                    if(playlistExist){
                        Toast.makeText(requireContext(),"playlist $playlistName already exist",Toast.LENGTH_SHORT).show()
                    }
                    else{
                        playListFragment.playlistList.list[playlistPosition].name = playlistName
                        binding.playlistName.text = playlistName

                        val editor = requireActivity().getSharedPreferences("FAVOURITES", Context.MODE_PRIVATE).edit()
                        val jsonStringPlaylist = GsonBuilder().create().toJson(playListFragment.playlistList)
                        editor.putString("MusicPlaylist", jsonStringPlaylist)
                        editor.apply()
//            store
                    }


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
    private fun customDialogueForDelete(){

        val builder = MaterialAlertDialogBuilder(requireContext())


        builder.setTitle(playListFragment.playlistList.list[playlistPosition].name)
            .setMessage("Do you want to delete this playlist?")

            .setPositiveButton("Yes") { dialog, _ ->
                playListFragment.playlistList.list.removeAt(playlistPosition)
                val editor = requireActivity().getSharedPreferences("FAVOURITES", Context.MODE_PRIVATE).edit()
                val jsonStringPlaylist = GsonBuilder().create().toJson(playListFragment.playlistList)
                editor.putString("MusicPlaylist", jsonStringPlaylist)
                editor.apply()
                dialog.dismiss()
                requireActivity().onBackPressed()
                
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
}