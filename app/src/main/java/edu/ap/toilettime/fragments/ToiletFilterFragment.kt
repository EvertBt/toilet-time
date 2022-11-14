package edu.ap.toilettime.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import edu.ap.toilettime.R

class ToiletFilterFragment : Fragment(R.layout.fragment_toilet_filter) {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_toilet_filter, container, false)
    }

    companion object {
        @JvmStatic
        fun newInstance() = //param1: String, param2: String
            ToiletFilterFragment().apply {
                //arguments = Bundle().apply {
                //    putString(ARG_PARAM1, param1)
                //    putString(ARG_PARAM2, param2)
                //}
            }
    }
}