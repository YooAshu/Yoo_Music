package com.example.yoomusic

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import android.window.OnBackInvokedDispatcher
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.example.yoomusic.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken

class MainActivity : AppCompatActivity() {

//    private lateinit var bottomNavigationView: BottomNavigationView
    lateinit var binding: ActivityMainBinding
    private val REQUEST_CODE_PERMISSION = 123
    private val PERMISSIONS = if (Build.VERSION.SDK_INT >= 33) {
        arrayOf(
            android.Manifest.permission.READ_MEDIA_AUDIO



        )
    } else {
        arrayOf(
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    }

    companion object{
        lateinit var bottomNavigationView: BottomNavigationView
        var permsGranted = false
        var homeCreated = false
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setTheme(R.style.Theme_YooMusic)

        setContentView(R.layout.activity_main)

        requestPermissions()


        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)

//        bottomNavigationView=findViewById<BottomNavigationView>(R.id.bottom_navigation)

        bottomNavigationView = binding.bottomNavigation


        bottomNavigationView.itemIconTintList = null



        bottomNavigationView.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.home -> {
                    replaceFragment(HomeFragment(),"home")
                    true
                }

                R.id.search -> {
                    replaceFragment(searchFragment(),"search")
                    true
                }

                R.id.playlist -> {
                    replaceFragment(playListFragment(),"playlist")
//                    val intent = Intent(this,MusicPlayer::class.java)
//                    startActivity(intent)
                    true
                }


                else -> false
            }
        }


//        ,,
        supportFragmentManager.beginTransaction().replace(R.id.nowPlaying, NowPlaying()).commit()

        // to access fvt data

        favourite.fvtItemList = ArrayList()

        val editor = getSharedPreferences("FAVOURITES", MODE_PRIVATE)
        val jsonString = editor.getString("fvtSongs", null)
        val typeToken = object : TypeToken<ArrayList<Music>>(){}.type
        if(jsonString!=null){
            val data : ArrayList<Music> = GsonBuilder().create().fromJson(jsonString,typeToken)
            favourite.fvtItemList.addAll(data)
//
        }

        //     to acess playlist data

        playListFragment.playlistList = PlaylistLists()

        val jsonStringPlaylist = editor.getString("MusicPlaylist", null)
        if(jsonStringPlaylist!=null){
            val dataPlaylist : PlaylistLists = GsonBuilder().create().fromJson(jsonStringPlaylist,PlaylistLists::class.java)
            playListFragment.playlistList = dataPlaylist
        }

        //        check songs are deleted or not

      
        favourite.fvtItemList = checkSongs(favourite.fvtItemList)


        playListFragment.playlistList.list.forEach {
            it.playlist = checkSongs(it.playlist)
        }

//        acess sorting order

        val sortingMaineditor = getSharedPreferences("SORTING", MODE_PRIVATE)
        val jsonStringSortingMain = sortingMaineditor.getString("mainList", null)
        if(jsonStringSortingMain!=null){
            val data : Int = GsonBuilder().create().fromJson(jsonStringSortingMain,Int::class.java)
            HomeFragment.sortingOrder = data
//
        }
        val jsonStringSortingFvt = sortingMaineditor.getString("fvtList", null)
        if(jsonStringSortingFvt!=null){
            val data : Int = GsonBuilder().create().fromJson(jsonStringSortingFvt,Int::class.java)
            favourite.sortingOrder = data
            Log.d("sortingOrder",favourite.sortingOrder.toString())
//
        }

        val jsonStringSortingPlaylist = sortingMaineditor.getString("playList", null)
        if( jsonStringSortingPlaylist!=null){
            val data : Int = GsonBuilder().create().fromJson(jsonStringSortingPlaylist,Int::class.java)
            playlist_details.sortingOrder = data
//
        }


    }


    private fun replaceFragment(fragment: Fragment,tag: String? = null) {
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frame_container, fragment,tag)
        fragmentTransaction.addToBackStack(tag)
        fragmentTransaction.commit()


    }




    //    for request permission
    private fun requestPermissions()  {
        val permissionsToRequest = mutableListOf<String>()
        for (permission in PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissionsToRequest.add(permission)
            }
        }

        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                permissionsToRequest.toTypedArray(),
                REQUEST_CODE_PERMISSION
            )


        } else {
            // Permissions already granted, proceed with your logic
            // For example, access contacts and camera
            replaceFragment(HomeFragment(),"home")

        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_CODE_PERMISSION) {
            var allPermissionsGranted = true
            for (result in grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false
                    break
                }
            }

            if (allPermissionsGranted) {
                // Permissions granted, proceed with your logic
                // For example, access contacts and camera
                replaceFragment(HomeFragment(),"home")

            } else {
                // Permissions denied, request again
                requestPermissions()
            }
        }
    }


    override fun getOnBackInvokedDispatcher(): OnBackInvokedDispatcher {
        return super.getOnBackInvokedDispatcher()

    }

    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {

        if (supportFragmentManager.backStackEntryCount > 1) {
            supportFragmentManager.popBackStack()
        } else {


            finish() // Exit the app if there's only one fragment left in the stack
        }
    }

    override fun onDestroy() {
        super.onDestroy()

    }

    override fun onResume() {
        super.onResume()




        //to store fvt data
//        val editor = getSharedPreferences("FAVOURITES", MODE_PRIVATE).edit()
//        val jsonString = GsonBuilder().create().toJson(favourite.fvtItemList)
//        editor.putString("fvtSongs", jsonString)
//        editor.apply()
    }


}
