package com.example.yoomusic

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.yoomusic.databinding.FragmentNowPlayingBinding
import com.example.yoomusic.databinding.FragmentPlayListBinding


class playListFragment : Fragment() {


    companion object{
        @SuppressLint("StaticFieldLeak")
        lateinit var binding : FragmentPlayListBinding
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

        return view
    }


    private fun replaceFragment(fragment: Fragment,tag: String? = null) {
        requireActivity().supportFragmentManager.beginTransaction().replace(R.id.frame_container, fragment,tag).addToBackStack(tag).commit()
        // Clear the back stack


    }


}