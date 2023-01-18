package com.example.ytdownloader.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.*
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.Nullable
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import at.huber.youtubeExtractor.YouTubeUriExtractor
import at.huber.youtubeExtractor.YtFile
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.ReturnCode
import com.example.ytdownloader.R
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*


class VideoFragment : Fragment() {
    private lateinit var videoView1: VideoView
    private lateinit var download_url: String
    private lateinit var spinner: Spinner

    private val Any.TAG: String
        get() {
            val tag = javaClass.simpleName
            return if (tag.length <= 23) tag else tag.substring(0, 23)
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

        spinner = view.findViewById(R.id.spinner)
        val adapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.spinner_options,
            R.layout.spinner_item
        )
        adapter.setDropDownViewResource(R.layout.spinner_item)
        spinner.adapter = adapter


        var videoPath: String?

        if(arguments?.getString("check") == "1"){
            downloadVideo(arguments!!.getString("yturl")!!.trim())
        } else {
            videoPath = arguments!!.getString("url")
            val uri = Uri.parse(videoPath)

            // Set the media controller
            val mediaController = MediaController(activity)

            mediaController.setMediaPlayer(videoView1)
            videoView1.setMediaController(mediaController)

            // Set the video URI
            videoView1.setVideoURI(uri)

            // Start the video
            videoView1.start()

        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        val value = arguments!!.getString("url")
//
//        view.findViewById<TextView>(R.id.textview)?.text = value

        view.findViewById<Button>(R.id.btn_pobierz).setOnClickListener {
            val connectivityManager = context!!.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val network = connectivityManager.activeNetwork
            val capabilities = connectivityManager.getNetworkCapabilities(network)
            val isConnected = capabilities != null && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)

            if (!isConnected) {
                Toast.makeText(this@VideoFragment.activity, "Brak połączenia z internetem!", Toast.LENGTH_SHORT).show()
            }

            val outputPath = Uri.parse(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).path
                        + File.separator + "my_video"
            )

            val cmd = StringBuilder()

            var path = ""

            if(arguments?.getString("check") == "1") {
                val externalUri = Uri.parse("file://$selectedUri")
                path = Uri.parse(createCopyAndReturnRealPath(context!!, externalUri)).toString()
            } else {
               path = createCopyAndReturnRealPath(context!!, selectedUri!!)!!
            }

            cmd.append("-y -i \"$path\" ")

            if(view.findViewById<Switch>(R.id.switch3).isChecked){
                cmd.append("-c:a libmp3lame -b:a 256k ")
            } else {
                cmd.append("-c:v mpeg4 ")
            }



            if(view.findViewById<EditText>(R.id.starttime).text.isNotEmpty() && view.findViewById<EditText>(R.id.starttime).text.isNotEmpty()){

                val pattern = "^((?:[01]\\d|2[0-3]):[0-5]\\d:[0-5]\\d)\$"
                val patternMs = "^((?:[01]\\d|2[0-3]):[0-5]\\d:[0-5]\\d\\.\\d{1,3})\$"
                val combinedPattern = "($pattern)|($patternMs)"

                val startMs = view.findViewById<EditText>(R.id.starttime).text
                val endMs = view.findViewById<EditText>(R.id.stoptime).text

                val sdf = SimpleDateFormat("HH:mm:ss")
                val startTime = sdf.parse(startMs.toString())
                val endTime = sdf.parse(endMs.toString())

                if (startMs.matches(Regex(combinedPattern)) && endMs.matches(Regex(combinedPattern))) {
                    if (startTime != null) {
                        if(startTime.before(endTime)) {
                            cmd.append("-ss $startMs -to $endMs ")
                        } else {
                            Toast.makeText(
                                this@VideoFragment.activity,
                                "Start musi byc wiekszy niz stop",
                                Toast.LENGTH_LONG
                            ).show()
                            return@setOnClickListener
                        }
                    }
                } else {
                    Toast.makeText(
                        this@VideoFragment.activity,
                        "Podaj start i end w formacie hh:mm:ss.ms",
                        Toast.LENGTH_LONG
                    ).show()
                    return@setOnClickListener
                }

            }

            if(view.findViewById<Switch>(R.id.switch3).isChecked){
                // Do nothing, switch is checked
            } else {
                if(view.findViewById<Spinner>(R.id.spinner).selectedItem.toString() == "Wybierz rozdzielczosc"){
                    // Do nothing, switch is checked
                } else if(view.findViewById<Spinner>(R.id.spinner).selectedItem.toString() == "1080p") {
                    cmd.append("-vf scale=1920:1080 ")
                } else if(view.findViewById<Spinner>(R.id.spinner).selectedItem.toString() == "720p") {
                    cmd.append("-vf scale=1280:720 ")
                } else if(view.findViewById<Spinner>(R.id.spinner).selectedItem.toString() == "480p") {
                    cmd.append("-vf scale=854:480 ")
                }
            }

            if(view.findViewById<Switch>(R.id.switch3).isChecked){
                // Do nothing, switch is checked
            } else {
                if(view.findViewById<CheckBox>(R.id.mute).isChecked){
                    cmd.append("-an ")
                }
            }


            if(view.findViewById<Switch>(R.id.switch3).isChecked)
                cmd.append("$outputPath.mp3")
            else
                cmd.append("$outputPath.mp4")

            Log.d(TAG, "startTrim CMD: $cmd")

            trimVideo(cmd.toString())
        }
    }

    private fun trimVideo(cmd: String) {
        val session = FFmpegKit.execute(cmd)
        if (ReturnCode.isSuccess(session.returnCode)) {
            Log.d(TAG, "Successful FFmpegKit command execute")
            parentFragmentManager.beginTransaction().replace(R.id.fl_wrapper, HomeFragment()).commit()
            openDirectory()
        } else if (ReturnCode.isCancel(session.returnCode)) {
            Log.d(TAG, "Cancelled FFmpegKit :(")
        } else {
            Log.d(
                TAG,
                String.format(
                    "Command failed with state %s and rc %s.%s",
                    session.state,
                    session.returnCode,
                    session.failStackTrace
                )
            )
        }
    }

    @Nullable
    fun createCopyAndReturnRealPath(
        context: Context, uri: Uri
    ): String? {
        val contentResolver: ContentResolver = context.contentResolver ?: return null

        // Create file path inside app's data dir
        val filePath: String = (context.applicationInfo.dataDir + File.separator
                + System.currentTimeMillis())
        val file = File(filePath)
        try {
            val inputStream = contentResolver.openInputStream(uri) ?: return null
            val outputStream: OutputStream = FileOutputStream(file)
            val buf = ByteArray(1024)
            var len: Int
            while (inputStream.read(buf).also { len = it } > 0) outputStream.write(buf, 0, len)
            outputStream.close()
            inputStream.close()
        } catch (ignore: IOException) {
            return null
        }
        return file.absolutePath
    }

    fun openDirectory() {
        // Try to start an activity for the intent
        try {
            startActivity(Intent(DownloadManager.ACTION_VIEW_DOWNLOADS));
        } catch (e: ActivityNotFoundException) {
                Toast.makeText(
                    this@VideoFragment.activity,
                    "Brak aplikacji do otwarcia folderu 'Download' z plikiem!",
                    Toast.LENGTH_LONG
                ).show()
                val homeFragment = HomeFragment()
                parentFragmentManager.beginTransaction().replace(R.id.fl_wrapper, homeFragment).commit()
        }
    }

    private fun downloadVideo(path: String) {
        val youTubeUriExtractor =
            @SuppressLint("StaticFieldLeak")
            object : YouTubeUriExtractor(context!!) {
                override fun onUrisAvailable(
                    videoId: String,
                    videoTitle: String,
                    ytFiles: SparseArray<YtFile>
                ) {
                    if (ytFiles != null) {
                        val itag = intArrayOf(37, 22, 18)
                        for (i in itag.indices) {
                            if (ytFiles[itag[i]] != null) {
                                download_url = ytFiles[itag[i]].url
                                break
                            }
                        }
                        if (download_url != null) {
                            val request = DownloadManager.Request(Uri.parse(download_url))
                            request.setTitle(videoTitle).setDescription("YTDownloader")
                                .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE or DownloadManager.Request.NETWORK_WIFI)
                            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                                .setAllowedOverRoaming(true)
                                .setMimeType("video/mp4")
                            request.setDestinationInExternalPublicDir(
                                Environment.DIRECTORY_DOWNLOADS, "$videoTitle"
                            )
                            val downloadManager =
                                context!!.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                            val downloadId = downloadManager.enqueue(request)

                            val query = DownloadManager.Query()
                            query.setFilterById(downloadId)
                            val c = downloadManager.query(query)
                            if (c.moveToFirst()) {
                                val status = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS))

                                if (status == DownloadManager.STATUS_FAILED) {
                                    Toast.makeText(this@VideoFragment.activity, "Download canceled", Toast.LENGTH_SHORT).show()
                                    val homeFragment = HomeFragment()
                                    parentFragmentManager.beginTransaction().replace(R.id.fl_wrapper, homeFragment).commit()
                                }

//                                if (status == DownloadManager.STATUS_PENDING) {
//                                    // The download is queued, start a timer to check if it stays queued for more than 8 seconds
//                                    val timer = Timer()
//                                    timer.schedule(object : TimerTask() {
//                                        override fun run() {
//                                            // Check the status of the download again
//                                            val c2 = downloadManager.query(query)
//                                            if (c2.moveToFirst()) {
//                                                val status2 =
//                                                    c2.getInt(c2.getColumnIndex(DownloadManager.COLUMN_STATUS))
//                                                if (status2 == DownloadManager.STATUS_PENDING) {
//                                                    // The download is still queued, cancel it
//                                                    downloadManager.remove(downloadId)
//
//                                                    Toast.makeText(
//                                                        this@VideoFragment.activity,
//                                                        "Coś poszło nie tak!",
//                                                        Toast.LENGTH_SHORT
//                                                    ).show()
//                                                    timer.cancel()
//
//                                                    val homeFragment = HomeFragment()
//                                                    parentFragmentManager.beginTransaction().replace(R.id.fl_wrapper, homeFragment).commit()
//                                                }
//                                            }
//                                            c2.close()
//                                        }
//                                    }, 8000) // Check the status every 8 seconds
//                                }

                                // Update the progress bar
                                val progressBar = view!!.findViewById<ProgressBar>(R.id.progressBar)

                                view!!.findViewById<CheckBox>(R.id.mute).isEnabled = false
                                view!!.findViewById<EditText>(R.id.starttime).isEnabled = false
                                view!!.findViewById<EditText>(R.id.stoptime).isEnabled  = false
                                view!!.findViewById<Button>(R.id.btn_pobierz).isEnabled  = false

                                view!!.findViewById<FrameLayout>(R.id.overlay).visibility = View.VISIBLE
//                                view!!.findViewById<BottomNavigationView>(R.id.bottom_navigation).setBackgroundColor(R.color.greymenu)

                                progressBar.visibility = View.VISIBLE

                                view!!.findViewById<TextView>(R.id.progressBarText).visibility = View.VISIBLE
                            }
                            c.close()

                            val broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
                                override fun onReceive(context: Context, intent: Intent) {
                                    if (DownloadManager.ACTION_DOWNLOAD_COMPLETE == intent.action) {
                                        view!!.findViewById<ProgressBar>(R.id.progressBar).visibility = View.GONE
                                        view!!.findViewById<TextView>(R.id.progressBarText).visibility = View.GONE
                                        view!!.findViewById<FrameLayout>(R.id.overlay).visibility = View.GONE
//                                        view!!.findViewById<BottomNavigationView>(R.id.bottom_navigation).setBackgroundColor(R.color.purple_500)

                                        view!!.findViewById<CheckBox>(R.id.mute).isEnabled = true
                                        view!!.findViewById<EditText>(R.id.starttime).isEnabled = true
                                        view!!.findViewById<EditText>(R.id.stoptime).isEnabled  = true
                                        view!!.findViewById<Button>(R.id.btn_pobierz).isEnabled  = true

                                        val downloadManagerId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                                        if (downloadId == downloadManagerId) {
                                            val videoPath = Environment.getExternalStoragePublicDirectory(
                                                Environment.DIRECTORY_DOWNLOADS
                                            ).toString() + File.separator + videoTitle
                                            val uri = Uri.parse(videoPath)

                                            selectedUri = uri

                                            // Set the media controller
                                            val mediaController = MediaController(activity)
                                            mediaController.setMediaPlayer(videoView1)

                                            videoView1.setMediaController(mediaController)
                                            videoView1.setVideoURI(uri)
                                            videoView1.start()
                                        }
                                    } else {
                                        val downloadManagerId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                                        if (downloadId == downloadManagerId) {
                                            downloadManager.remove(downloadId)

                                            Toast.makeText(
                                                this@VideoFragment.activity,
                                                "Coś poszło nie tak!",
                                                Toast.LENGTH_SHORT
                                            ).show()

                                            val homeFragment = HomeFragment()
                                            parentFragmentManager.beginTransaction().replace(R.id.fl_wrapper, homeFragment).commit()
                                        }
                                    }
                                }
                            }
                            context!!.registerReceiver(broadcastReceiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
                        }
                    }
                }
            }
        youTubeUriExtractor.execute(path)
    }

}