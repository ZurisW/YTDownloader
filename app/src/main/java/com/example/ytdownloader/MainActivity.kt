package com.example.ytdownloader


import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import com.example.ytdownloader.databinding.ActivityMainBinding
import com.example.ytdownloader.fragments.HomeFragment
import com.example.ytdownloader.fragments.PobraneFragment
import com.example.ytdownloader.fragments.SettingsFragment
import com.example.ytdownloader.fragments.VideoFragment
import com.google.android.material.bottomnavigation.BottomNavigationView


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val homeFragment = HomeFragment()
        val pobraneFragment = PobraneFragment()
        val settingsFragment = SettingsFragment()

        makeCurrentFragment(homeFragment)

        val bottom_navigation = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        bottom_navigation.setOnItemSelectedListener {
            when(it.itemId ) {
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


    }

    fun sendLink() {
        // Get the text to send from the edit text
        val editText = findViewById<EditText>(R.id.link_url)
        val text = editText?.text.toString()

        val videoFragment = VideoFragment()
        supportFragmentManager.beginTransaction().replace(R.id.fl_wrapper, videoFragment).commit()
    }

    private fun makeCurrentFragment(fragment: Fragment) =
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fl_wrapper, fragment)
            commit()
        }
}