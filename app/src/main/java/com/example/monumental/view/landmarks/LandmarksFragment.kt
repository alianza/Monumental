@file:Suppress("DEPRECATION")

package com.example.monumental.view.landmarks

import android.annotation.SuppressLint
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
import android.view.MotionEvent
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
import com.example.monumental.view.common.BitmapUtils
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

    companion object {
        fun newInstance() = LandmarksFragment()
        private const val MONTH_OFFSET = 1
        private const val YEAR_OFFSET = 1900
        private const val CHOOSE_IMAGE_REQUEST_CODE = 1002
    }

    private var actionDelayVal: Long = 0
    private lateinit var viewModel: LandmarksViewModel

    private var landmarks = arrayListOf<Landmark>()
    private var imageUri: Uri? = null
    private var landmarkName: String? = null
    private var pictureFile: File? = null

    private lateinit var landmarksAdapter: LandmarksAdapter
    private lateinit var journey: Journey
    private lateinit var cameraHelper: CameraHelper

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

        instantiateClasses()
        initViews()
        setListeners()
    }

    /**
     * Instantiate required classes
     */
    private fun instantiateClasses() {
        cameraHelper = CameraHelper()
    }

    /**
     * Initiates the views
     * Sets up LandmarksAdapter and Observers
     */
    private fun initViews() {
        landmarksAdapter = LandmarksAdapter(landmarks,
            { landmark: Landmark -> landmarkClick(landmark) },
            { landmark: Landmark -> landmarkDelete(landmark) },
            { landmark: Landmark -> landmarkShare(landmark) })

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
            if (this.landmarks.isEmpty()) { tvNoLandmarks.visibility = View.VISIBLE }
            else { tvNoLandmarks.visibility = View.GONE }
        })

        rbCurrentJourney.isChecked = journey.current
        rbCurrentJourney.isSelected = journey.current

        (activity as MainActivity?)?.supportActionBar?.title = journey.name
    }

    /**
     * Sets onClick listeners
     */
    @SuppressLint("ClickableViewAccessibility")
    private fun setListeners() {
        btnClose.setOnClickListener { closeFragment() }

        tvBack.setOnClickListener { closeFragment() }

        tvBack.setOnTouchListener { _, motionEvent -> btnClose.isPressed = motionEvent.action == MotionEvent.ACTION_DOWN; false }

        rbCurrentJourney.setOnCheckedChangeListener { _, isChecked -> onCurrentJourneyClick(isChecked) }

        fab.setOnClickListener { onFabClick() }
    }

    /**
     * Activity result, take image and try detect landmarks
     *
     * @param requestCode Request code of Activity
     * @param resultCode ResultCode of Activity
     * @param data Data from Activity
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CHOOSE_IMAGE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            imageUri = data!!.data // In this case, imageUri is returned by the chooser, save it.
            val resizedBitmap: Bitmap? = viewModel.getBitmap(context?.contentResolver, imageUri!!)

            if (imageUri!!.scheme == "content") { // if Image from device Content
                val bitmapData = BitmapUtils.bitmapToByteArray(resizedBitmap!!)
                pictureFile = viewModel.getOutputMediaFile()
                cameraHelper.savePicture(pictureFile!!, bitmapData)
                imageUri = viewModel.getOutputMediaFileUri()
                landmarkNameDialog()
            }
        }
    }

    /**
     * Builds Landmark name dialog when new Landmark has been added
     */
    private fun landmarkNameDialog() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(context)

        builder.setTitle(getString(R.string.name_landmark))

        val dialogView = layoutInflater.inflate(R.layout.dialog_new_landmark_view, null)

        builder.setView(dialogView)

        builder.setPositiveButton("Done") { dialog, _ ->
            Handler(Looper.getMainLooper()).postDelayed({
                landmarkName = dialogView.etLandmarkName.text.toString()
                viewModel.createLandmark(Landmark(null, landmarkName!!, imageUri.toString(), Date(), journey.id))
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

    /**
     * When the FAB is clicked start Choose image activity
     */
    private fun onFabClick() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), CHOOSE_IMAGE_REQUEST_CODE)
    }

    /**
     * When current Journey button is clicked set Journey as current
     *
     * @param checked Boolean True if checkmark is checked, False otherwise
     */
    private fun onCurrentJourneyClick(checked: Boolean) {
        if (checked) {
            journey.current = rbCurrentJourney.isChecked
            viewModel.setActiveJourney(journey)
            Toast.makeText(context, getString(R.string.set_current_journey, journey.name), Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Shares a landmark
     *
     * @param landmark Landmark to share
     */
    private fun landmarkShare(landmark: Landmark) {
        val shareIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            putExtra(Intent.EXTRA_TITLE, "${getString(R.string.share)} ${landmark.name}")
            putExtra(Intent.EXTRA_TEXT, getString(R.string.share_landmark_message, landmark.name, journey.name, getString(
                R.string.date_format,
                landmark.date?.date.toString(),
                (landmark.date?.month?.plus(MONTH_OFFSET)).toString(),
                (landmark.date?.year?.plus(YEAR_OFFSET)).toString())))
            putExtra(Intent.EXTRA_STREAM, Uri.parse(landmark.imgUri))
            type = "image/jpeg"
        }
        startActivity(Intent.createChooser(shareIntent, "${getString(R.string.share)} ${landmark.name}"))
    }

    /**
     * When Landmark Delete Button is clicked
     * Builds dialog for delete confirmation
     *
     * @param landmark Landmark to delete
     */
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

    /**
     * When landmark is clicked
     * Builds Dialog for Landmark display image
     *
     * @param landmark Landmark to display
     */
    private fun landmarkClick(landmark: Landmark) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(context)

        builder.setTitle(landmark.name)

        builder.setNegativeButton(context?.getString(R.string.close), null)

        builder.setNeutralButton(getString(R.string.share)) { _, _ -> landmarkShare(landmark) }

        builder.setMessage(
            getString(R.string.visited_on) + " " + getString(
                R.string.date_format,
                landmark.date?.date.toString(),
                (landmark.date?.month?.plus(MONTH_OFFSET)).toString(),
                (landmark.date?.year?.plus(YEAR_OFFSET)).toString()
            )
        )

        val dialogView = layoutInflater.inflate(R.layout.dialog_saved_landmark_view, null)

        val imageView = dialogView.findViewById<ImageView>(R.id.ivLandmark).also {
            it.setOnTouchListener(ImageMatrixTouchHandler(view?.context))
        }

        Picasso.get().load(File(URI.create(landmark.imgUri))).into(imageView)

        builder.setView(dialogView)
        val alertDialog: AlertDialog = builder.create()
        alertDialog.show()
    }

    /**
     * When back button is clicked
     * Close LandmarksFragment using FragmentManager
     */
    private fun closeFragment() {
        Handler(Looper.getMainLooper()).postDelayed({
            (activity as MainActivity?)?.fragmentManager?.closeLandmarkFragment()
        }, actionDelayVal)
    }

    /**
     * When Fragment is destroyed, change action bar title
     */
    override fun onDestroy() {
        (activity as MainActivity?)?.supportActionBar?.title = getString(R.string.journeys)
        super.onDestroy()
    }
}


