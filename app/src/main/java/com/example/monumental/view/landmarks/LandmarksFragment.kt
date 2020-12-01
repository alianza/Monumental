package com.example.monumental.view.landmarks

import android.app.AlertDialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.bogdwellers.pinchtozoom.ImageMatrixTouchHandler
import com.example.monumental.R
import com.example.monumental.model.entity.Journey
import com.example.monumental.model.entity.Landmark
import com.example.monumental.view.main.MainActivity
import com.example.monumental.viewModel.landmark.LandmarksViewModel
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.landmark_fragment.*
import java.io.File
import java.net.URI

open class LandmarksFragment : Fragment() {

    companion object { fun newInstance() = LandmarksFragment() }

    private var actionDelayVal: Long = 0
    private lateinit var viewModel: LandmarksViewModel

    private var landmarks = arrayListOf<Landmark>()

    private lateinit var landmarksAdapter: LandmarksAdapter
    private lateinit var journey: Journey

    private val MONTH_OFFSET = 1
    private val YEAR_OFFSET = 1900

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.landmark_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(LandmarksViewModel::class.java)
        actionDelayVal = (activity as MainActivity?)?.actionDelayVal!!

        journey = this.arguments?.getParcelable("Journey")!!
        println("Parcel Bundle $journey")

        initViews()
        setListeners()
    }

    private fun initViews() {
        landmarksAdapter = LandmarksAdapter(landmarks,
            { landmark: Landmark -> landmarkClick(landmark) },
            { landmark: Landmark -> landmarkDelete(landmark) })

        rvLandmarks.layoutManager = StaggeredGridLayoutManager(1, RecyclerView.VERTICAL)
        rvLandmarks.adapter = landmarksAdapter

        viewModel.getLandmarksByJourney(journey.id!!).observe(viewLifecycleOwner, { landmarks ->
            if (landmarks != null) {
                this.landmarks.clear()
                landmarks.forEach { landmark ->
                    this.landmarks.add(landmark)
                }
            }
            this.landmarks.sortByDescending { it.id }
            landmarksAdapter.notifyDataSetChanged()
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
            Handler(Looper.getMainLooper()).postDelayed({
                viewModel.deleteLandmark(landmark)
                dialog.dismiss()
            }, actionDelayVal)
        }

        builder.setNegativeButton("No", null)

        val alert: AlertDialog = builder.create()
        Handler(Looper.getMainLooper()).postDelayed({
            alert.show()
        }, actionDelayVal)
    }

    @Suppress("DEPRECATION")
    private fun landmarkClick(landmark: Landmark) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(context)

        builder.setTitle(landmark.name)

        builder.setNegativeButton("Close", null)

        builder.setMessage(
            getString(R.string.visited_on) + " " + getString(
                R.string.date_format,
                landmark.date?.date.toString(),
                (landmark.date?.month?.plus(MONTH_OFFSET)).toString(),
                (landmark.date?.year?.plus(YEAR_OFFSET)).toString()
            )
        )

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
        Handler(Looper.getMainLooper()).postDelayed({
            (activity as MainActivity?)?.fragmentManager?.closeLandmarkFragment()
        }, actionDelayVal)
    }
}


