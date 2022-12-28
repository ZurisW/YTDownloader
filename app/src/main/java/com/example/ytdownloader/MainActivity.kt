package com.example.ytdownloader


import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import com.example.ytdownloader.callback.FFMpegCallback
import com.example.ytdownloader.databinding.ActivityMainBinding
import com.example.ytdownloader.fragments.HomeFragment
import com.example.ytdownloader.fragments.PobraneFragment
import com.example.ytdownloader.fragments.SettingsFragment
import com.example.ytdownloader.fragments.VideoFragment
import com.github.hiteshsondhi88.libffmpeg.FFmpeg
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.io.File


class MainActivity : AppCompatActivity(), FFMpegCallback {

    private lateinit var binding: ActivityMainBinding
    private val TAG = "MainActivity"

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

        if (ContextCompat.checkSelfPermission(applicationContext, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this@MainActivity, arrayOf<String>(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE), 2222)
        } else if (ContextCompat.checkSelfPermission(applicationContext, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this@MainActivity, arrayOf<String>(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE), 2222)
        }


    }

    override fun onProgress(progress: String) {

        //Prints log of progress
        Log.i(TAG, "Running: $progress")

        onProgress(progress).run {
            onProgress(progress)
        }
    }

    override fun onSuccess(convertedFile: File, type: String) {
        Toast.makeText(this, "Done!", Toast.LENGTH_SHORT).show()

        //Show preview of outputs for after checking type of media
        when {
            type.equals(OutputType.TYPE_VIDEO) -> VideoDialog.show(supportFragmentManager, convertedFile)
            type.equals(OutputType.TYPE_AUDIO) -> AudioDialog.show(supportFragmentManager, convertedFile)
            type.equals(OutputType.TYPE_GIF) -> GIFDialog.show(supportFragmentManager, convertedFile)
        }
    }

    override fun onFailure(error: Exception) {
        error.printStackTrace()
        Toast.makeText(this, "Error: ${error.message}", Toast.LENGTH_SHORT).show()

        onProgress.run {
            onDismiss()
        }
    }

    override fun onFinish() {

        onProgress.run {
            onDismiss()
        }
    }

    override fun onNotAvailable(error: Exception) {
        Toast.makeText(this, "Error: ${error.message}", Toast.LENGTH_SHORT).show()

        onProgress.run {
            onDismiss()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 2222) {
            setUpResources()
        }
    }

    private fun stopRunningProcess() {
        FFmpeg.getInstance(this).killRunningProcesses()
    }

    private fun isRunning(): Boolean {
        return FFmpeg.getInstance(this).isFFmpegCommandRunning
    }

    private fun showInProgressToast(){
        Toast.makeText(this, "Operation already in progress! Try again in a while.", Toast.LENGTH_SHORT).show()
    }

    private fun makeCurrentFragment(fragment: Fragment) =
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fl_wrapper, fragment)
            commit()
        }
}