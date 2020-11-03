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
import com.example.monumental.model.Journey
import com.example.monumental.model.Landmark
import com.example.monumental.ui.main.MainActivity
import kotlinx.android.synthetic.main.landmark_fragment.*

class LandmarkFragment : Fragment() {

    companion object { fun newInstance() = LandmarkFragment() }

    private lateinit var viewModel: LandmarkViewModel

    private var actionDelayVal: Long = 0

    private var landmarks = arrayListOf<Landmark>()

    private lateinit var landmarkAdapter: LandmarkAdapter
    private lateinit var journey: Journey

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

        journey = this.arguments?.getParcelable("Journey")!!
        println("Parcel Bundle $journey")

        initViews()
        setListeners()
    }

    private fun initViews() {
        landmarkAdapter = LandmarkAdapter(landmarks,
            { landmark: Landmark -> landmarkClick(landmark) },
            { landmark: Landmark -> landmarkDelete(landmark) })

        rvLandmarks.layoutManager = StaggeredGridLayoutManager(1, RecyclerView.VERTICAL)
        rvLandmarks.adapter = landmarkAdapter

        viewModel.getLandmarksByJourney(journey.id!!).observe(viewLifecycleOwner, { landmarks ->
            println("Observe! " + landmarks)
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

        swCurrentJourney.isChecked = journey.current
        swCurrentJourney.isSelected = journey.current

        (activity as MainActivity?)?.supportActionBar?.title = journey.name
    }

    override fun onDestroy() {
        (activity as MainActivity?)?.supportActionBar?.title = getString(R.string.journeys)
        super.onDestroy()
    }

    private fun setListeners() {
        btnClose.setOnClickListener { closeFragment() }
        tvBack.setOnClickListener { closeFragment() }
        swCurrentJourney.setOnCheckedChangeListener { buttonView, isChecked ->
            toggleActiveJourney(isChecked)
        }
    }

    private fun toggleActiveJourney(checked: Boolean) {
        println("Checked! " + checked)
        if (checked) {
            journey.current = swCurrentJourney.isChecked
            viewModel.setActiveJourney(journey)
        }
    }


    private fun landmarkDelete(landmark: Landmark) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(context)

        builder.setTitle("Remove " + landmark.name + "?")
        builder.setMessage("Are you sure?")

        builder.setPositiveButton("Yes") { dialog, _ ->
            Handler().postDelayed({
            viewModel.deleteLandmark(landmark)
            dialog.dismiss() }, actionDelayVal) }

        builder.setNegativeButton("No", null)

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