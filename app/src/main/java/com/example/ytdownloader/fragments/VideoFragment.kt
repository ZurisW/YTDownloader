package com.example.ytdownloader.fragments

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import com.example.ytdownloader.R
import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler
import com.github.hiteshsondhi88.libffmpeg.FFmpeg
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler
import java.io.File

class VideoFragment : Fragment() {
    private lateinit var videoView1: VideoView
    private lateinit var ffmpeg: FFmpeg
    private lateinit var audioManager: AudioManager

    val Any.TAG: String
        get() {
            val tag = javaClass.simpleName
            return if (tag.length <= 23) tag else tag.substring(0, 23)
        }

    // Set the output file path (nie działa bo permisje od androida 10 w góre umarły)
//    val outputPath = "/storage/emulated/0/Movies/my_video.mp4"
    val outputPath = context?.filesDir.toString() + "/my_video.mp4"
    val startMs = "00:00:03"
    val endMs = "00:00:05"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)



    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_video, container, false)

        // Initialize the VideoView and FFmpeg
        videoView1 = view.findViewById(R.id.VideoView)
        ffmpeg = FFmpeg.getInstance(activity)

        // Load the FFmpeg binary
        ffmpeg.loadBinary(object : LoadBinaryResponseHandler() {
            override fun onStart() {}
            override fun onFailure() {}
            override fun onSuccess() {}
            override fun onFinish() {}
        })

        Runtime.getRuntime().exec("chmod -R 777 " + context!!.filesDir +"/ffmpeg")

        // Set the video path
//        val videoPath = "https://www.youtube.com/watch?v=dQw4w9WgXcQ"
//        val videoPath = "android.resource://"+context!!.packageName+"/"+R.raw.video;
        val videoPath = arguments!!.getString("url")
        val uri = Uri.parse(videoPath)

        // Set the media controller
        val mediaController = MediaController(activity)

        mediaController.setMediaPlayer(videoView1)
        videoView1.setMediaController(mediaController)

//        val audioManager = requireContext().getSystemService(AudioManager::class.java)


        // Set the video URI
        videoView1.setVideoURI(uri)

        // Start the video
        videoView1.start()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val value = arguments!!.getString("url")

        view.findViewById<TextView>(R.id.textview)?.text = value

        view.findViewById<Button>(R.id.btn_pobierz).setOnClickListener {
//            if (ContextCompat.checkSelfPermission(context!!, Manifest.permission.WRITE_EXTERNAL_STORAGE)
//                != PackageManager.PERMISSION_GRANTED) {
//                Toast.makeText(
//                    this@VideoFragment.activity,
//                    "Potrzeba permisji do zapisu!",
//                    Toast.LENGTH_SHORT
//                ).show()
//            } else {
//
//            }

            Log.d(TAG, "startTrim: src: " + selectedUri.toString())
            Log.d(TAG, "startTrim: dest: $outputPath")
            Log.d(TAG, "startTrim: startMs: " + startMs)
            Log.d(TAG, "startTrim: endMs: " + endMs)
            trimVideo(selectedUri.toString(), outputPath, startMs, endMs)
        }
    }

    private fun trimVideo(inputFile: String, outputFile: String, startTime: String, duration: String) {
        val cmd = arrayOf(
            "-i",
            inputFile,
            "-ss",
            startTime,
            "-t",
            duration,
            "-c",
            "copy",
            outputFile
        )
        FFmpeg.getInstance(context).execute(cmd, object : ExecuteBinaryResponseHandler() {
            override fun onSuccess(message: String) {
                Toast.makeText(
                    this@VideoFragment.activity,
                    "Udało się strimmować video!",
                    Toast.LENGTH_SHORT
                ).show()
            }

            override fun onFailure(message: String) {
                Toast.makeText(
                    this@VideoFragment.activity,
                    "Nic się nie udało, wszystko wybuchło, jezus maria",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    fun muteAudio(inputFile: String, outputFile: String) {
        val cmd = arrayOf(
            "-i",
            inputFile,
            "-an",
            outputFile
        )
        FFmpeg.getInstance(context).execute(cmd, object : ExecuteBinaryResponseHandler() {
            override fun onSuccess(message: String) {
                // Muting succeeded, do something here
            }

            override fun onFailure(message: String) {
                // Muting failed, do something here
            }
        })
    }

}