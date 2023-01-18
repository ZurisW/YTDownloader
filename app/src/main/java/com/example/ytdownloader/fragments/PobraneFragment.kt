package com.example.ytdownloader.fragments

import android.app.DownloadManager
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.example.ytdownloader.R


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [PobraneFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class PobraneFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        try {
            startActivity(Intent(DownloadManager.ACTION_VIEW_DOWNLOADS));
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(
                this@PobraneFragment.activity,
                "Brak aplikacji do otwarcia folderu 'Download' z plikiem!",
                Toast.LENGTH_LONG
            ).show()
        }

        val homeFragment = HomeFragment()
        parentFragmentManager.beginTransaction().replace(R.id.fl_wrapper, homeFragment).commit()


        return inflater.inflate(R.layout.fragment_pobrane, container, false)
    }
}