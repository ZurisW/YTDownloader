package com.example.ytdownloader.fragments

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.ytdownloader.R

class VideoFragment : Fragment() {
    private lateinit var videoView1: VideoView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        simpleVideoView = view?.findViewById<View>(R.id.VideoView) as VideoView
//
//        if (mediaControls == null) {
//            // creating an object of media controller class
//            mediaControls = MediaController(this.context)
//
//            // set the anchor view for the video view
//            mediaControls!!.setAnchorView(this.simpleVideoView)
//        }
//
//        // set the media controller for video view
//        simpleVideoView!!.setMediaController(mediaControls)
//
//        val value = arguments!!.getString("yturl")
//        val myUri = Uri.parse(value)
//
//        simpleVideoView!!.setVideoURI(myUri)
//
//        simpleVideoView!!.requestFocus()
//
//        // starting the video
//        simpleVideoView!!.start()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_video, container, false)

        // Initialize the VideoView
        videoView1 = view.findViewById(R.id.VideoView)

        // Set the video path
        val videoPath = "https://www.youtube.com/watch?v=dQw4w9WgXcQ"
        val uri = Uri.parse(videoPath)

        // Set the media controller
        val mediaController = MediaController(activity)
        videoView1.setMediaController(mediaController)

        // Set the video URI
        videoView1.setVideoURI(uri)

        // Start the video
        videoView1.start()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val value = arguments!!.getString("yturl")

        view.findViewById<TextView>(R.id.textview)?.text = value
    }
}