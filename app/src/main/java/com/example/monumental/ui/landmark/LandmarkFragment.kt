package com.example.monumental.ui.landmark

import android.app.AlertDialog
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.bogdwellers.pinchtozoom.ImageMatrixTouchHandler
import com.example.monumental.R
import com.example.monumental.model.Journey
import com.example.monumental.model.Landmark
import com.example.monumental.ui.main.MainActivity
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.landmark_fragment.*
import java.io.File
import java.net.URI


class LandmarkFragment : Fragment() {

    companion object { fun newInstance() = LandmarkFragment() }

    private lateinit var viewModel: LandmarkViewModel

    private var actionDelayVal: Long = 0

    private var landmarks = arrayListOf<Landmark>()

    private lateinit var landmarkAdapter: LandmarkAdapter
    private lateinit var journey: Journey

    private val MONTH_OFFSET = 1
    private val YEAR_OFFSET = 1900

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
            if (landmarks != null) {
                this.landmarks.clear()
                landmarks.forEach { landmark ->
                    this.landmarks.add(landmark)
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
        swCurrentJourney.setOnCheckedChangeListener { _, isChecked ->
            toggleActiveJourney(isChecked)
        }
    }

    private fun toggleActiveJourney(checked: Boolean) {
        if (checked) {
            journey.current = swCurrentJourney.isChecked
            viewModel.setActiveJourney(journey)
            Toast.makeText(
                context,
                getString(R.string.set_current_journey, journey.name),
                Toast.LENGTH_LONG
            ).show()
        }
    }


    private fun landmarkDelete(landmark: Landmark) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(context)

        builder.setTitle("Remove " + landmark.name + "?")
        builder.setMessage("Are you sure?")

        builder.setPositiveButton("Yes") { dialog, _ ->
            Handler().postDelayed({
                viewModel.deleteLandmark(landmark)
                dialog.dismiss()
            }, actionDelayVal) }

        builder.setNegativeButton("No", null)

        val alert: AlertDialog = builder.create()
        Handler().postDelayed({
            alert.show()
        }, actionDelayVal)
    }



    private fun landmarkClick(landmark: Landmark) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(context)

        val title = TextView(context)
        title.setText(landmark.name + " " + getString(R.string.date_format,
            landmark.date?.date.toString(),
            (landmark.date?.month?.plus(MONTH_OFFSET)).toString(),
            (landmark.date?.year?.plus(YEAR_OFFSET)).toString()))
        title.setPadding(20, 20, 20, 20)
        title.textSize = 20f
        title.setTextColor(resources.getColor(R.color.design_default_color_on_secondary))
        builder.setCustomTitle(title)

        builder.setNegativeButton("Close", null)

        val dialogView = layoutInflater.inflate(R.layout.dialog_landmark_view, null)

        val imageView = dialogView.findViewById<ImageView>(R.id.ivLandmark).also {
            it.setOnTouchListener(ImageMatrixTouchHandler(view?.context))
        }

        println("Landmark " + landmark.img_uri)

        Picasso.get().load(File(URI.create(landmark.img_uri))).into(imageView)

        builder.setView(dialogView)
        val alertDialog: AlertDialog = builder.create()
        alertDialog.show()
    }

    private fun closeFragment() {
        btnClose.isPressed = true
        Handler().postDelayed({
            (activity as MainActivity?)?.fragmentHelper?.closeLandmarkFragment()
        }, actionDelayVal)
    }
}


