package com.example.ytdownloader


import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.MediaController
import android.widget.Toast
import android.widget.VideoView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.blackbox.ffmpeg.examples.utils.Utils
import com.example.ytdownloader.databinding.ActivityMainBinding
import com.example.ytdownloader.fragments.HomeFragment
import com.example.ytdownloader.fragments.PobraneFragment
import com.example.ytdownloader.fragments.SettingsFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.io.File


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    // declaring a null variable for VideoView
    var simpleVideoView: VideoView? = null

    // declaring a null variable for MediaController
    var mediaControls: MediaController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val homeFragment = HomeFragment()
        val pobraneFragment = PobraneFragment()
        val settingsFragment = SettingsFragment()

        makeCurrentFragment(homeFragment)

        val bottom_navigation = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        bottom_navigation.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.home -> {
                    makeCurrentFragment(homeFragment)
                    return@setOnItemSelectedListener true
                }
                R.id.pobrane -> {
                    makeCurrentFragment(pobraneFragment)
                    return@setOnItemSelectedListener true
                }
                R.id.settings -> {
                    makeCurrentFragment(settingsFragment)
                    return@setOnItemSelectedListener true
                }
                else -> false
            }
        }

//        simpleVideoView = findViewById<View>(R.id.VideoView) as VideoView
//
//        if (mediaControls == null) {
//            // creating an object of media controller class
//            mediaControls = MediaController(this)
//
//            // set the anchor view for the video view
//            mediaControls!!.setAnchorView(this.simpleVideoView)
//        }
//
//        // set the media controller for video view
//        simpleVideoView!!.setMediaController(mediaControls)
//
//        val myUri = Uri.parse("https://www.youtube.com/watch?v=xNXiuEdujAo&ab_channel=RonnieFerrari")
//
//        simpleVideoView!!.setVideoURI(myUri)
//
//        simpleVideoView!!.requestFocus()
//
//        // starting the video
//        simpleVideoView!!.start()
    }

    private fun makeCurrentFragment(fragment: Fragment) =
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fl_wrapper, fragment)
            commit()
        }
}