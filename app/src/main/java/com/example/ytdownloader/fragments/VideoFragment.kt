package com.example.ytdownloader.fragments

import android.content.ActivityNotFoundException
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.Nullable
import androidx.fragment.app.Fragment
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
    private lateinit var ffmpeg: FFmpegKit
    private lateinit var audioManager: AudioManager

    val Any.TAG: String
        get() {
            val tag = javaClass.simpleName
            return if (tag.length <= 23) tag else tag.substring(0, 23)
        }

    // Set the output file path (nie działa bo permisje od androida 10 w góre umarły)
//    val outputPath = "/storage/emulated/0/Movies/my_video.mp4"
//    val outputPath = context?.filesDir.toString() + "/my_video.mp4"
//    val outputPath = "${context!!.filesDir}/my_video.mp4"
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

//          val outputPath = "${context!!.filesDir}/my_video.mp4"
            val outputPath = Uri.parse(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).path
                        + File.separator + "my_video.mp4"
            )

            // TODO: zrobilem cmd, trzeba zrobic dobrze starttime i endtime (brak startu to od poczatku video
            // TODO: endtime to do konca), na sekundy i w formacie 00:00:03 itd, naura

            val cmd = StringBuilder()
            val path = createCopyAndReturnRealPath(context!!, selectedUri!!)!!

            cmd.append("-y -i $path -c:v mpeg4 ")

            if(view.findViewById<EditText>(R.id.starttime).text.isNotEmpty() && view.findViewById<EditText>(R.id.starttime).text.isNotEmpty()){
                Log.d(TAG, "startTrim: SEKUNDYYYYYYYYYYYYYYYYYY")
                cmd.append("-ss $startMs -to $endMs ")
            }

            if(view.findViewById<RadioButton>(R.id.mute).isChecked){
                Log.d(TAG, "startTrim: CHECKED CAŁE TEEEEEE")
                cmd.append("-an ")
            }

            cmd.append(outputPath)

            Log.d(TAG, "startTrim: src: " + selectedUri.toString())
            Log.d(TAG, "startTrim: dest: $outputPath")
            Log.d(TAG, "startTrim: startMs: $startMs")
            Log.d(TAG, "startTrim: endMs: $endMs")

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

}