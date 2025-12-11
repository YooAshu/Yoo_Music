package com.example.yoomusic

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.yoomusic.databinding.FragmentFavouriteBinding
import com.google.gson.GsonBuilder
import jp.wasabeef.glide.transformations.BlurTransformation


class favourite : Fragment(), playlistHolderAdapter.OnItemClickListener {



    companion object{
        @SuppressLint("StaticFieldLeak")
        lateinit var binding : FragmentFavouriteBinding
        var fvtItemList : ArrayList<Music> = ArrayList()
        var isClicked : Boolean = false
        @SuppressLint("StaticFieldLeak")
        lateinit var musicAdapter: MusicAdapter
        var sortingOrder : Int = 3
    }
    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_favourite, container, false)
        binding = FragmentFavouriteBinding.bind(view)

//        MainActivity.binding.bottomNavigation.selectedItemId = R.id.playlist
//        MainActivity.bottomNavigationView.selectedItemId = R.id.playlist
//        MainActivity.bottomNavigationView.menu.getItem(2).isChecked = true




//        marquu text
        binding.playlistName.isSelected = true

        loadMusicList()

        binding.shuffleFvt.setOnClickListener {

            if(fvtItemList.isNotEmpty()){

                val intent = Intent(requireContext(), MusicPlayer::class.java)
                intent.putExtra("index", 0)
                intent.putExtra("class", "FvtFragment")
                startActivity(intent)

            }
            else{
                Toast.makeText(requireContext(), "Nothing to shuffle", Toast.LENGTH_SHORT).show()
            }

        }
        binding.totalFvtSongCount.text = "${musicAdapter.getItemCount()} Songs"
        binding.threeDots.setOnClickListener {
            showbottomDialog()
        }

        binding.backBtn.setOnClickListener {
            requireActivity().onBackPressed()

        }


        return view
    }

    private fun loadMusicList(){

//       musicListSearch = HomeFragment().getAllAudio()
        isClicked  = true
        searchFragment.search = false

        //check if any song is deleted
//        fvtItemList = checkSongs(fvtItemList)

        binding.fvtRv.setHasFixedSize(true)
        binding.fvtRv.setItemViewCacheSize(13)
        binding.fvtRv.layoutManager = LinearLayoutManager(requireContext())
        musicAdapter = MusicAdapter(requireContext(), fvtItemList, listener = this)
        binding.fvtRv.adapter = musicAdapter


    }

    override fun onResume() {
        super.onResume()
        musicAdapter.updateMusicList(fvtItemList,favourite.binding.totalFvtSongCount)
        if(musicAdapter.itemCount>0){
            Glide.with(this)
                .load(fvtItemList[0].artUri)
                .apply(RequestOptions().placeholder(R.drawable.artboard_2).centerCrop())
                .into(binding.roundedImage)


            Glide.with(this).load(fvtItemList[0].artUri)
                .apply(RequestOptions().placeholder(R.drawable.artboard_2).centerCrop())
                .apply(RequestOptions.bitmapTransform(BlurTransformation(25, 3)))
                .into(binding.bannerImage)



        }
        Log.d("fvtList", sortingOrder.toString())
        if(fvtItemList.isNotEmpty()){
            when(sortingOrder){
                0 -> fvtItemList.sortBy { it.title.lowercase() }
                1 -> fvtItemList.sortByDescending { it.dateAdded }
                2-> fvtItemList.sortBy { it.dateAdded }
                else->fvtItemList.sortByDescending { it.dateAddedToList }
            }

            musicAdapter.updateMusicList(fvtItemList,null)

        }

    }

    private fun showbottomDialog(){

        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.favourite_option_dialogue)


        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.attributes?.windowAnimations = R.style.bottomDialogAnimation
        dialog.window?.setGravity(Gravity.BOTTOM)
        dialog.show()

        dialog.findViewById<RelativeLayout>(R.id.add_song).setOnClickListener {
            val intent = Intent(context,AddSongs::class.java)
            intent.putExtra("fromFvt","true")
            ContextCompat.startActivity(requireContext(),intent,null)

            dialog.dismiss()
        }
        dialog.findViewById<RelativeLayout>(R.id.remove_song).setOnClickListener {
            val intent = Intent(context,Remove_songs::class.java)
            intent.putExtra("fromFvt","true")
            ContextCompat.startActivity(requireContext(),intent,null)

            dialog.dismiss()
        }
        dialog.findViewById<RelativeLayout>(R.id.sort_by).setOnClickListener {
            dialog.dismiss()
            showSortbyDialog()
        }
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
//            Log.d("sortingOrder", sortingOrder.toString())
            if(fvtItemList.isNotEmpty() && sortingOrder!=0) {
                sortingOrder = 0
                fvtItemList.sortBy { it.title.lowercase() }
                musicAdapter.updateMusicList(fvtItemList,null)

                val jsonString = GsonBuilder().create().toJson(0)
                editor.putString("fvtList", jsonString)
                editor.apply()
            }

            dialog.dismiss()
        }
        dialog.findViewById<RelativeLayout>(R.id.by_new_first).setOnClickListener {

            if(fvtItemList.isNotEmpty() && sortingOrder!=1){
                sortingOrder = 1
                fvtItemList.sortByDescending { it.dateAdded }
                musicAdapter.updateMusicList(fvtItemList,null)

                val jsonString = GsonBuilder().create().toJson(1)
                editor.putString("fvtList", jsonString)
                editor.apply()
            }



            dialog.dismiss()
        }
        dialog.findViewById<RelativeLayout>(R.id.by_old_first).setOnClickListener {

            if(fvtItemList.isNotEmpty() && sortingOrder!=2) {
                sortingOrder = 2
                fvtItemList.sortBy { it.dateAdded }
                musicAdapter.updateMusicList(fvtItemList,null)

                val jsonString = GsonBuilder().create().toJson(2)
                editor.putString("fvtList", jsonString)
                editor.apply()
            }

            dialog.dismiss()
        }
        dialog.findViewById<RelativeLayout>(R.id.by_recently_added).setOnClickListener {
            if(fvtItemList.isNotEmpty() && sortingOrder!=3){
                sortingOrder = 3
                fvtItemList.sortByDescending { it.dateAddedToList }
                musicAdapter.updateMusicList(fvtItemList,null)

                val jsonString = GsonBuilder().create().toJson(3)
                editor.putString("fvtList", jsonString)
                editor.apply()

            }

            dialog.dismiss()
        }

    }
    override fun onItemClicked(position: Int) {
        TODO("Not yet implemented")
    }


}