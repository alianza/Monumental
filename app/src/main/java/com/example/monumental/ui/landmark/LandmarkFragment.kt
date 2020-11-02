package com.example.monumental.ui.landmark

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.monumental.MainActivity
import com.example.monumental.R
import kotlinx.android.synthetic.main.landmark_fragment.*

class LandmarkFragment : Fragment() {

    companion object {
        fun newInstance() = LandmarkFragment()
    }

    private lateinit var viewModel: LandmarkViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.landmark_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(LandmarkViewModel::class.java)

        val bundle = this.arguments
        if (bundle != null) {
            val journeyId = bundle.getInt("JourneyId", -1)
            println(journeyId)
        }

        btnClose.setOnClickListener { (activity as MainActivity?)?.fragmentHelper?.closeLandmarkFragment() }
    }

}