package com.example.monumental.view.landmarks

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
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
import com.example.monumental.view.common.helpers.CameraHelper
import com.example.monumental.view.main.MainActivity
import com.example.monumental.viewModel.landmark.LandmarksViewModel
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.dialog_new_landmark_view.view.*
import kotlinx.android.synthetic.main.landmarks_fragment.*
import java.io.File
import java.net.URI
import java.util.*

class LandmarksFragment : Fragment() {

    companion object { fun newInstance() = LandmarksFragment() }

    private var actionDelayVal: Long = 0
    private lateinit var viewModel: LandmarksViewModel

    private var landmarks = arrayListOf<Landmark>()
    private var imageUri: Uri? = null
    private var landmarkName: String? = null
    private var pictureFile: File? = null

    private lateinit var landmarksAdapter: LandmarksAdapter
    private lateinit var journey: Journey
    private lateinit var cameraHelper: CameraHelper

    private val monthOffset = 1
    private val yearOffset = 1900
    private val requestCodeChooseImage = 1002

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.landmarks_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(LandmarksViewModel::class.java)
        actionDelayVal = (activity as MainActivity?)?.actionDelayVal!!

        journey = arguments?.getParcelable("Journey")!!

        println("Parcel Bundle $journey")

        instantiateClasses()
        initViews()
        setListeners()
    }

    private fun instantiateClasses() {
        cameraHelper = CameraHelper()
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

        rbCurrentJourney.isChecked = journey.current
        rbCurrentJourney.isSelected = journey.current

        (activity as MainActivity?)?.supportActionBar?.title = journey.name
    }

    private fun setListeners() {
        btnClose.setOnClickListener { closeFragment() }

        tvBack.setOnClickListener { closeFragment() }

        rbCurrentJourney.setOnCheckedChangeListener { _, isChecked ->
            onCurrentJourneyClick(isChecked)
        }

        fab.setOnClickListener { onFabClick() }
    }

    /** Activity result, take image and try detect landmarks */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == requestCodeChooseImage && resultCode == Activity.RESULT_OK) {
            imageUri = data!!.data // In this case, imageUri is returned by the chooser, save it.

            val resizedBitmap: Bitmap? = viewModel.getBitmap(context?.contentResolver, imageUri!!)

            if (imageUri!!.scheme == "content") { // if Image from device Content
                val bitmapData = cameraHelper.bitmapToByteArray(resizedBitmap!!)
                pictureFile = viewModel.getOutputMediaFile()
                cameraHelper.savePicture(pictureFile!!, bitmapData)
                imageUri = viewModel.getOutputMediaFileUri()
                buildLandmarkNameDialog()
            }
            println(imageUri)
        }
    }

    private fun buildLandmarkNameDialog() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(context)

        builder.setTitle(getString(R.string.name_landmark))

        val dialogView = layoutInflater.inflate(R.layout.dialog_new_landmark_view, null)

        builder.setView(dialogView)

        builder.setPositiveButton("Done") { dialog, _ ->
            Handler(Looper.getMainLooper()).postDelayed({
                landmarkName = dialogView.etLandmarkName.text.toString()
                println(landmarkName)
                viewModel.saveLandmark(Landmark(null, landmarkName!!, imageUri.toString(), Date(), journey.id))
                Toast.makeText(context, context?.getString(R.string.saved_landmark, landmarkName, journey.name), Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }, actionDelayVal)
        }

        builder.setNegativeButton("Cancel", null)

        val alert: AlertDialog = builder.create()

        Handler(Looper.getMainLooper()).postDelayed({
            alert.show()
            Handler(Looper.getMainLooper()).postDelayed({
                dialogView.etLandmarkName.requestFocus()
                val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
                imm?.showSoftInput(dialogView.etLandmarkName, InputMethodManager.SHOW_IMPLICIT)
            }, actionDelayVal)
        }, actionDelayVal)
    }

    /** Choose image activity */
    private fun onFabClick() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(
            Intent.createChooser(intent, "Select Picture"),
            requestCodeChooseImage
        )
    }

    private fun onCurrentJourneyClick(checked: Boolean) {
        if (checked) {
            journey.current = rbCurrentJourney.isChecked
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

        builder.setNegativeButton(context?.getString(R.string.close), null)

        builder.setMessage(
            getString(R.string.visited_on) + " " + getString(
                R.string.date_format,
                landmark.date?.date.toString(),
                (landmark.date?.month?.plus(monthOffset)).toString(),
                (landmark.date?.year?.plus(yearOffset)).toString()
            )
        )

        val dialogView = layoutInflater.inflate(R.layout.dialog_saved_landmark_view, null)

        val imageView = dialogView.findViewById<ImageView>(R.id.ivLandmark).also {
            it.setOnTouchListener(ImageMatrixTouchHandler(view?.context))
        }

        println("URL!: " + File(URI.create(landmark.img_uri)))

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

    override fun onDestroy() {
        (activity as MainActivity?)?.supportActionBar?.title = getString(R.string.journeys)
        super.onDestroy()
    }
}


