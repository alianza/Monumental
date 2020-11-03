package com.example.monumental.ui.landmark

import android.app.AlertDialog
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.monumental.R
import com.example.monumental.model.Landmark
import com.example.monumental.ui.main.MainActivity
import kotlinx.android.synthetic.main.landmark_fragment.*

class LandmarkFragment : Fragment() {

    companion object {
        fun newInstance() = LandmarkFragment()
    }

    private lateinit var viewModel: LandmarkViewModel

    private var actionDelayVal: Long = 0
    private var journeyId: Int = -1

    private var landmarks = arrayListOf<Landmark>()

    private lateinit var landmarkAdapter: LandmarkAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.landmark_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(LandmarkViewModel::class.java)
        actionDelayVal = (activity as MainActivity?)?.actionDelayVal!!

        val bundle = this.arguments
        if (bundle != null) {
            journeyId = bundle.getInt("JourneyId", -1)
            println(journeyId)
        }

        initViews()
        setListeners()

//        viewModel.createLandmarkTest(landmarkId)
    }

    private fun initViews() {
        landmarkAdapter = LandmarkAdapter(landmarks,
            { landmark: Landmark -> landmarkClick(landmark) },
            { landmark: Landmark -> landmarkDelete(landmark) })

        rvLandmarks.layoutManager = StaggeredGridLayoutManager(1, RecyclerView.VERTICAL)
        rvLandmarks.adapter = landmarkAdapter

        viewModel.landmarks.observe(viewLifecycleOwner, { landmarks ->
            if (landmarks != null) {
                this.landmarks.clear()
                landmarks.forEach { landmark ->
                    this.landmarks.add(landmark)
                    println("Got: $landmark")
                }
            }
//            this.landmarks.sortByDescending { it.name }
            landmarkAdapter.notifyDataSetChanged()
            if (this.landmarks.isEmpty()) {
                tvNoLandmarks.visibility = View.VISIBLE
            } else {
                tvNoLandmarks.visibility = View.GONE
            }
        })
    }

    private fun setListeners() {
        btnClose.setOnClickListener { closeFragment() }
        tvBack.setOnClickListener { closeFragment() }
    }


    private fun landmarkDelete(landmark: Landmark) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(context)

        builder.setTitle("Remove " + landmark.name + "?")
        builder.setMessage("Are you sure?")

        builder.setPositiveButton("Yes") { dialog, _ ->
            viewModel.deleteLandmark(landmark)
            dialog.dismiss() }

        builder.setNegativeButton("No") { dialog, _ -> dialog.dismiss() }

        val alert: AlertDialog = builder.create()
        Handler().postDelayed({
            alert.show()
        }, actionDelayVal)
    }

    private fun landmarkClick(landmark: Landmark) {

    }

    private fun closeFragment() {
        btnClose.isPressed = true
        Handler().postDelayed({
            (activity as MainActivity?)?.fragmentHelper?.closeLandmarkFragment()
        }, actionDelayVal)
    }

}