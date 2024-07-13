package com.example.yoomusic

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.hardware.biometrics.BiometricManager.Strings
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.yoomusic.databinding.ActivityMainBinding
import com.example.yoomusic.databinding.FragmentHomeBinding
import com.google.gson.GsonBuilder
import java.io.File
import kotlin.math.log

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [HomeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HomeFragment : Fragment(), playlistHolderAdapter.OnItemClickListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var binding: FragmentHomeBinding
    private lateinit var musicAdapter: MusicAdapter


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
        binding = FragmentHomeBinding.inflate(layoutInflater)
        return binding.root
//        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        MainActivity.bottomNavigationView.menu.getItem(0).isChecked = true

        if (!MainActivity.homeCreated) {
            // Perform some initialization here
            musicListHome = getAllAudio()
            MainActivity.homeCreated = true
        }
//        musicListHome = ArrayList()
        loadMusicList()


        binding.shuffleHome.setOnClickListener {
            val intent = Intent(requireContext(), MusicPlayer::class.java)
            intent.putExtra("index", 0)
            intent.putExtra("class", "HomeFragment")
            startActivity(intent)
        }

        binding.sortbyHome.setOnClickListener {
            showbottomDialog()
        }


    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment HomeFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            HomeFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }

        lateinit var musicListHome: ArrayList<Music>
        var sortingOrder: Int = 1
    }

    private fun loadMusicList() {
        searchFragment.search = false
        favourite.isClicked = false
//        musicListHome = getAllAudio()

        binding.homeRV.setHasFixedSize(true)
        binding.homeRV.setItemViewCacheSize(13)
        binding.homeRV.layoutManager = LinearLayoutManager(requireContext())
        musicAdapter = MusicAdapter(requireContext(), musicListHome, listener = this)
        binding.homeRV.adapter = musicAdapter
//        binding.totalSongCount.text =  musicAdapter.itemCount + "Total Songs : "
        binding.totalSongCount.text = "${musicAdapter.itemCount} Songs"


    }

    @SuppressLint("Range")
    fun getAllAudio(): ArrayList<Music> {
        val tempList = ArrayList<Music>()
//        val selection = MediaStore.Audio.Media.IS_MUSIC + "!=0"
//        val selection = "${MediaStore.Audio.Media.DATA} NOT LIKE ? AND ${MediaStore.Audio.Media.IS_MUSIC}!=0"
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.MIME_TYPE
        )

        val selection = """
    ${MediaStore.Audio.Media.DATA} NOT LIKE ? 
    AND ${MediaStore.Audio.Media.IS_MUSIC} != 0
    AND ${MediaStore.Audio.Media.TITLE} NOT LIKE 'AUD%'
    AND ${MediaStore.Audio.Media.TITLE} NOT LIKE 'PTT%'
""".trimIndent()

        val selectionArgs = arrayOf("%WhatsApp/Audio%")

        val cursor = requireContext().contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            MediaStore.Audio.Media.DATE_ADDED + " DESC",

            )

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    val titleC =
                        cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE))
                    val idC = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media._ID))
                    val albumC =
                        cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM))
                    val artistC =
                        cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST))
                    val pathC = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA))
                    val durationC =
                        cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION))

                    val albumIdC =
                        cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID))
                            .toString()
                    val uri = Uri.parse("content://media/external/audio/albumart")
                    if (uri == null) {
                        Log.d("HomeFragment", "Uri is null")
                    }
                    val artUriC = Uri.withAppendedPath(uri, albumIdC).toString()

                    val dateAddedC = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DATE_ADDED))
                    val music = Music(
                        id = idC,
                        title = titleC,
                        album = albumC,
                        artist = artistC,
                        path = pathC,
                        duration = durationC,
                        artUri = artUriC,
                        dateAdded = dateAddedC
                    )

                    val file = File(music.path)
                    if (file.exists()) {
                        tempList.add(music)
                    }


                } while (cursor.moveToNext())
                cursor.close()

            }
        }
        if(sortingOrder==0){
            tempList.sortBy { it.title.lowercase() }

        }
        else if(sortingOrder==2){
            tempList.sortBy { it.dateAdded }
        }
        return tempList
    }

    override fun onItemClicked(position: Int) {
        TODO("Not yet implemented")
    }


    @SuppressLint("NotifyDataSetChanged")
    private fun showbottomDialog(){

        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.sortby_option_dialogue)


        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.attributes?.windowAnimations = R.style.bottomDialogAnimation
        dialog.window?.setGravity(Gravity.BOTTOM)
        dialog.show()

        val editor = requireActivity().getSharedPreferences("SORTING", AppCompatActivity.MODE_PRIVATE).edit()

        dialog.findViewById<RelativeLayout>(R.id.by_name).setOnClickListener {
            if(musicListHome.isNotEmpty() && sortingOrder !=0) {

                sortingOrder = 0
                musicListHome.sortBy { it.title.lowercase() }
                musicAdapter.notifyDataSetChanged()

                val jsonString = GsonBuilder().create().toJson(0)
                editor.putString("mainList", jsonString)
                editor.apply()
            }

            dialog.dismiss()
        }
        dialog.findViewById<RelativeLayout>(R.id.by_new_first).setOnClickListener {

            if(musicListHome.isNotEmpty() && sortingOrder !=1) {
                sortingOrder = 1
                musicListHome.sortByDescending { it.dateAdded }
                musicAdapter.notifyDataSetChanged()

                val jsonString = GsonBuilder().create().toJson(1)
                editor.putString("mainList", jsonString)
                editor.apply()
            }


            dialog.dismiss()
        }
        dialog.findViewById<RelativeLayout>(R.id.by_old_first).setOnClickListener {


            if(musicListHome.isNotEmpty() && sortingOrder !=2) {
                sortingOrder = 2
                musicListHome.sortBy { it.dateAdded }
                musicAdapter.notifyDataSetChanged()

                val jsonString = GsonBuilder().create().toJson(2)
                editor.putString("mainList", jsonString)
                editor.apply()
            }

            dialog.dismiss()
        }


    }
}