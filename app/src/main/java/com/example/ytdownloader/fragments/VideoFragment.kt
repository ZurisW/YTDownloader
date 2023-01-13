package com.example.ytdownloader.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.*
import android.content.pm.PackageManager
import android.media.AudioManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
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
import java.util.*


class VideoFragment : Fragment() {
    private lateinit var videoView1: VideoView
    private lateinit var download_url: String

    private val Any.TAG: String
        get() {
            val tag = javaClass.simpleName
            return if (tag.length <= 23) tag else tag.substring(0, 23)
        }

    // Set the output file path (nie działa bo permisje od androida 10 w góre umarły)
//    val outputPath = "/storage/emulated/0/Movies/my_video.mp4"
//    val outputPath = context?.filesDir.toString() + "/my_video.mp4"
//    val outputPath = "${context!!.filesDir}/my_video.mp4"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_video, container, false)

        // Initialize the VideoView and FFmpeg
        videoView1 = view.findViewById(R.id.VideoView)

        // Set the video path
//        https://www.youtube.com/watch?v=EoaU8duIv90 dziobak
//        val videoPath = "https://www.youtube.com/watch?v=dQw4w9WgXcQ" rick
//        val videoPath = "android.resource://"+context!!.packageName+"/"+R.raw.video;


        var videoPath: String?
//        var downloadUrl = "https://www.youtube.com/watch?v=uJ1VbZyH_g8" kapitan

        if(arguments?.getString("check") == "1"){

//          https://www.youtube.com/watch?v=es5IFnOx1VE OD ZMIERZCHU DO ŚWITU CO ROBIMY DYM

            downloadVideo(arguments!!.getString("yturl")!!.trim())
//          downloadVideo("https://www.youtube.com/watch?v=es5IFnOx1VE")

//            videoPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "ytvid.mp4"
//            Log.d(TAG, "CMD: (DUPAGUWNO) $videoPath  A W DODATKU " + arguments?.getString("check"))
        } else {
            videoPath = arguments!!.getString("url")
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

        }

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
//            }

            val outputPath = Uri.parse(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).path
                        + File.separator + "my_video.mp4"
            )

            // TODO: jak sie kliknie pobierz to zeby prosilo o nazwe filmika
            // TODO: jednak chyba mi sie nie chce
            // TODO: wycisz zeby mozna bylo odkliknac bo nie dziala xd I OVERLAY GREY NA HOME
            // TODO: progress bar buja, przyciski wył, tera jak sie nic nie wpisze w te gówna
            // TODO: progress bar nie buja, dodac tekst z procentami pod ikonka krecenia sie
            // TODO: lepszy player do video bo tej zajezdza siusiakiem
            // TODO: edittexty to zeby od poczatku i do konca robilo i zeby start<=end
            // TODO: przepisac funkcje moze na nowsza ale tez mi sie nie chce jak dziala
            // TODO: funkcje moze zrobic z wyswietlania filmu na odtwarzaczu bo sie powtarza
            // TODO: ale jeden hujas

            val cmd = StringBuilder()
            Log.d(TAG, "CMD: (au au au) $selectedUri")

            var path = ""

            if(arguments?.getString("check") == "1") {
                val externalUri = Uri.parse("file://$selectedUri")
                path = Uri.parse(createCopyAndReturnRealPath(context!!, externalUri)).toString()
            } else {
               path = createCopyAndReturnRealPath(context!!, selectedUri!!)!!
            }

            Log.d(TAG, "CMD: RETURN PATH: $path")

            cmd.append("-y -i \"$path\" -c:v mpeg4 ")

            if(view.findViewById<EditText>(R.id.starttime).text.isNotEmpty() && view.findViewById<EditText>(R.id.starttime).text.isNotEmpty()){
                Log.d(TAG, "startTrim: SEKUNDYYYYYYYYYYYYYYYYYY")
                val pattern = "^((?:[01]\\d|2[0-3]):[0-5]\\d:[0-5]\\d)\$"
                val patternMs = "^((?:[01]\\d|2[0-3]):[0-5]\\d:[0-5]\\d\\.\\d{1,3})\$"
                val combinedPattern = "($pattern)|($patternMs)"

                val startMs = view.findViewById<EditText>(R.id.starttime).text
                val endMs = view.findViewById<EditText>(R.id.stoptime).text

                if (startMs.matches(Regex(combinedPattern)) && endMs.matches(Regex(combinedPattern))) {
                    cmd.append("-ss $startMs -to $endMs ")
                } else {
                    Toast.makeText(
                        this@VideoFragment.activity,
                        "Podaj start i end w formacie hh:mm:ss.ms",
                        Toast.LENGTH_LONG
                    ).show()
                }

            }

            if(view.findViewById<RadioButton>(R.id.mute).isChecked){
                cmd.append("-an ")
            }

            cmd.append(outputPath)

            Log.d(TAG, "startTrim CMD: $cmd")

            trimVideo(cmd.toString())
        }
    }

    private fun trimVideo(cmd: String) {
        val session = FFmpegKit.execute(cmd)
        if (ReturnCode.isSuccess(session.returnCode)) {
            Log.d(TAG, "Successful FFmpegKit command execute")
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

    private fun openDirectory() {
        val directoryUri = Uri.parse(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).path
                    + File.separator
        )
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(directoryUri, "resource/folder")
        }

        // Try to start an activity for the intent
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            // No app can handle the intent. Use the built-in "Files" app instead.
            val builtInIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(directoryUri, "resource/folder")
                setPackage("com.android.documentsui")
            }
            try {
                startActivity(builtInIntent)
            } catch (e: ActivityNotFoundException) {
                // The built-in "Files" app is not available. Show an error message.
                Toast.makeText(
                    this@VideoFragment.activity,
                    "Brak aplikacji do otwarcia folderu 'Download' z plikiem!",
                    Toast.LENGTH_LONG
                ).show()
                val homeFragment = HomeFragment()
                parentFragmentManager.beginTransaction().replace(R.id.fl_wrapper, homeFragment).commit()
            }
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
                                c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS))
                                val bytesDownloaded = c.getLong(c.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                                val bytesTotal = c.getLong(c.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                                val progress = bytesDownloaded.toFloat() / bytesTotal.toFloat()
                                val percentage = (progress * 100).toInt()
                                // Update the progress bar
                                val progressBar = view!!.findViewById<ProgressBar>(R.id.progressBar)

                                view!!.findViewById<RadioButton>(R.id.mute).isEnabled = false
                                view!!.findViewById<EditText>(R.id.starttime).isEnabled = false
                                view!!.findViewById<EditText>(R.id.stoptime).isEnabled  = false
                                view!!.findViewById<Button>(R.id.btn_pobierz).isEnabled  = false

                                view!!.findViewById<FrameLayout>(R.id.overlay).visibility = View.VISIBLE
                                progressBar.visibility = View.VISIBLE
                                progressBar.progress = percentage
                            }
                            c.close()

                            val broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
                                override fun onReceive(context: Context, intent: Intent) {
                                    if (DownloadManager.ACTION_DOWNLOAD_COMPLETE == intent.action) {
                                        view!!.findViewById<ProgressBar>(R.id.progressBar).visibility = View.GONE
                                        view!!.findViewById<FrameLayout>(R.id.overlay).visibility = View.GONE

                                        view!!.findViewById<RadioButton>(R.id.mute).isEnabled = true
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
                                            Log.d(TAG, "CMD: VIDEOPATH Z FUNKCIJ: $selectedUri")

                                            // Set the media controller
                                            val mediaController = MediaController(activity)
                                            mediaController.setMediaPlayer(videoView1)

                                            videoView1.setMediaController(mediaController)
                                            videoView1.setVideoURI(uri)
                                            videoView1.start()
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