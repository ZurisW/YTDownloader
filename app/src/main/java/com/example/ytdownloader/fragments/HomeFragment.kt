package com.example.ytdownloader.fragments


import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.ytdownloader.R
import com.example.ytdownloader.databinding.ActivityMainBinding


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

private lateinit var binding: ActivityMainBinding
var selectedUri: Uri? = null
var trimvideo: ImageView? = null
var videoView: VideoView? = null

/**
 * A simple [Fragment] subclass.
 * Use the [HomeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HomeFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)

    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment HomeFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            HomeFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    private fun openVideo() {
        val intent = Intent()
        intent.type = "video/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(intent, 100)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == 100) {
            if (data != null && data.data != null) {
                selectedUri = data.data
            }

            val text = selectedUri.toString()

            val videoFragment = VideoFragment()

            val args = Bundle()
            args.putString("url", text)
            videoFragment.arguments = args

            parentFragmentManager.beginTransaction().replace(R.id.fl_wrapper, videoFragment).commit()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        view.findViewById<Button>(R.id.btn_download).setOnClickListener {
            // Get the text to send from the edit text
            val editText = view.findViewById<EditText>(R.id.link_url)
            val text = editText?.text.toString()

//            if (text.isEmpty()) {
//               Toast.makeText(
//                    this@HomeFragment.activity,
//                    "Podaj adres filmu z YouTube",
//                    Toast.LENGTH_SHORT
//                ).show()
//
//                return@setOnClickListener
//            }

            val videoFragment = VideoFragment()

            val args = Bundle()
            args.putString("yturl", text)
            videoFragment.arguments = args

            parentFragmentManager.beginTransaction().replace(R.id.fl_wrapper, videoFragment).commit()
        }

        view.findViewById<Button>(R.id.btn_file).setOnClickListener {
            openVideo()
        }

    }
}