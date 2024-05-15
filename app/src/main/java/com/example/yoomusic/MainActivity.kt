package com.example.yoomusic

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.example.yoomusic.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var binding: ActivityMainBinding
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
                    replaceFragment(HomeFragment())
                    true
                }

                R.id.search -> {
                    replaceFragment(searchFragment())
                    true
                }

                R.id.playlist -> {
//                    replaceFragment(playListFragment())
                    val intent = Intent(this,MusicPlayer::class.java)
                    startActivity(intent)
                    true
                }

                R.id.fvt -> {
                    replaceFragment(FvtFragment())
                    true
                }

                else -> false
            }
        }


//        ,,
        supportFragmentManager.beginTransaction().replace(R.id.nowPlaying, NowPlaying()).commit()



    }


    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().replace(R.id.frame_container, fragment).commit()

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
            replaceFragment(HomeFragment())

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
                replaceFragment(HomeFragment())

            } else {
                // Permissions denied, request again
                requestPermissions()
            }
        }
    }


}
