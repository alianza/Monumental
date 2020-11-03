@file:Suppress("DEPRECATION")

package com.example.monumental.ui.main

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.graphics.Bitmap
import android.graphics.Rect
import android.hardware.Camera
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProviders
import com.example.monumental.R
import com.example.monumental.cloudlandmarkrecognition.CloudLandmarkRecognitionProcessor
import com.example.monumental.common.CameraPreview
import com.example.monumental.common.GraphicOverlay
import com.example.monumental.common.VisionImageProcessor
import com.example.monumental.helpers.*
import com.example.monumental.model.Landmark
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.IOException
import java.util.*

class MainActivity : AppCompatActivity() {

    private var pictureFile: File? = null
    private var camera: Camera? = null
    private var preview: CameraPreview? = null
    private var imageUri: Uri? = null
    private var picture: Camera.PictureCallback? = null

    // Recyclerview click offset check
    private var check = 0

    val actionDelayVal = 250L
    var currentJourneyId = 0

    lateinit var fragmentHelper: FragmentHelper
    private lateinit var customTabHelper: CustomTabHelper
    private lateinit var imageHelper: ImageHelper
    private lateinit var mediaFileHelper: MediaFileHelper
    private lateinit var cameraHelper: CameraHelper
    private lateinit var resultsSpinnerAdapter: ResultsSpinnerAdapter
    private lateinit var imageProcessor: VisionImageProcessor
    private lateinit var viewModel: MainViewModel

    /** onCreate method to set layout, theme and initiate the views */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setTheme(R.style.AppTheme)
        viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)

        initViews()
    }

    override fun onBackPressed() {
        if (!fragmentHelper.closeJourneyFragment()) {
            super.onBackPressed()
        }
    }

    /** Inflate options menu */
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.still_image_menu, menu)
        return true
    }

    /** Settings button intent */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.flash -> {
                cameraHelper.toggleFlash(item, camera!!, this)
                return true
            }
            R.id.journeys -> {
                fragmentHelper.toggleJourneyFragment()
                item.icon = if(fragmentHelper.journeyFragmentIsOpen)
                { ContextCompat.getDrawable(this, R.drawable.ic_baseline_explore_off_24) }
                else { ContextCompat.getDrawable(this, R.drawable.ic_baseline_explore_24) }
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * Handle touch events and hide keyboard when moving focus from editText
     *
     * @param event
     * @return
     */
    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val view = currentFocus
            if (view is EditText) {
                val outRect = Rect()
                view.getGlobalVisibleRect(outRect)
                if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                    view.clearFocus()
                    val imm: InputMethodManager =
                        getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0)
                }
            }
        }
        return super.dispatchTouchEvent(event)
    }

    /** Activity result, take image and try detect landmarks */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_CHOOSE_IMAGE && resultCode == Activity.RESULT_OK) {
            // In this case, imageUri is returned by the chooser, save it.
            progressBarHolder.visibility = View.VISIBLE
            tvNoResults.visibility = View.GONE
            imageUri = data!!.data
            takeImageButton.setImageDrawable(
                ContextCompat.getDrawable(
                    this,
                    R.drawable.ic_autorenew_black_24dp
                )
            )
            tryReloadAndDetectInImage()
        }
    }

    /** Request permissions result */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        // When permissions granted, start the camera(preview)
        when (requestCode) {
            PERMISSIONS_REQUEST_CODE -> {
                setupCamera()
            }
        }
    }

    /** Start everything up */
    private fun initViews() {
        customTabHelper = CustomTabHelper()
        mediaFileHelper = MediaFileHelper()
        imageHelper = ImageHelper(previewPane, controlPanel)
        cameraHelper = CameraHelper(this, imageHelper)
        imageProcessor = CloudLandmarkRecognitionProcessor()
        fragmentHelper = FragmentHelper(this as AppCompatActivity)

        requestPermissions()
        setupResultsSpinner()
        setupCamera()
        setupListeners()
    }

    /** Request all required permissions */
    private fun requestPermissions() {
        requestPermissions(
            arrayOf(
                Manifest.permission.INTERNET,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA
            ), PERMISSIONS_REQUEST_CODE
        )
    }

    /** setup the resultsSpinner(Adapter) */
    private fun setupResultsSpinner() {
        resultsSpinnerAdapter = ResultsSpinnerAdapter(this, R.layout.spinner_item)
        // Apply the adapter to the spinner
        ResultsSpinner.adapter = resultsSpinnerAdapter
    }

    /** Setup the camera */
    private fun setupCamera() {
        if (checkSelfPermission(Manifest.permission.CAMERA) == PERMISSION_GRANTED) {
            if (cameraHelper.hasCamera()) {
                camera = cameraHelper.getCameraInstance()

                preview = camera?.let { CameraPreview(this, it) }

                cameraHelper.setParameters(camera!!)

                // Set the Preview view as the content of our activity.
                preview?.also {
                    val preview: FrameLayout = findViewById(R.id.camera_preview)
                    preview.addView(it)
                }

                picture = Camera.PictureCallback { data, _ ->
                    pictureFile = mediaFileHelper.getOutputMediaFile() ?: run {
                        Log.d(TAG, ("Error creating media file, check storage permissions"))
                        return@PictureCallback
                    }

                    cameraHelper.savePicture(pictureFile!!, data)
                    imageUri = mediaFileHelper.getOutputMediaFileUri()
                    camera?.stopPreview()
                    tryReloadAndDetectInImage()
                }
            }
        }
    }

    /** Setup all event listeners */
    @SuppressLint("ClickableViewAccessibility")
    private fun setupListeners() {
        viewModel.activeJourney.observe(this, {
            currentJourneyId = it?.id!!
        })

        ResultsSpinner.setOnTouchListener { _, _ ->
            if (ResultsSpinner.adapter.count == 2) { // If only one landmark result
                val landmark = ResultsSpinner.adapter.getItem(1).toString()
                startLandmarkInfoIntent(landmark)
            }
            return@setOnTouchListener false
        }

        ResultsSpinner.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                println("Spinner item $position!")
                println("parent $parent")
                println("view $view")
                println("pos $position")
                println("id $id")
                ResultsSpinner.setSelection(0)

                if (++check > 1 && position != 0) {
//                    val container = ResultsSpinner.selectedView as ConstraintLayout?
                    val textView = view?.findViewById<TextView>(R.id.tvLandmarkResultName)
                    val btnSave = view?.findViewById<ImageView>(R.id.btnSave)
                    var result = textView?.text.toString()

                    println(btnSave)

                    viewModel.createLandmark(Landmark(null, result, imageUri.toString(), Date(), currentJourneyId))

                    result = result.replace(" ", "+")

                    println("Clicked result: $result")
                    startLandmarkInfoIntent(result)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) { println("Spinner") }
        }

        takeImageButton.setOnClickListener {
            tvNoResults.visibility = View.GONE
            // Check for required permissions
            if (checkSelfPermission(Manifest.permission.CAMERA) == PERMISSION_GRANTED) {
                if (pictureFile == null && imageUri == null) { // Take picture
                    progressBarHolder.visibility = View.VISIBLE
                    camera?.takePicture(null, null, picture)
                    takeImageButton.setImageDrawable(
                        ContextCompat.getDrawable(
                            this,
                            R.drawable.ic_autorenew_black_24dp
                        )
                    )
                } else { // Reset Picture
                    pictureFile = null
                    imageUri = null
                    takeImageButton.setImageDrawable(
                        ContextCompat.getDrawable(
                            this,
                            R.drawable.ic_camera_black_24dp
                        )
                    )
                    camera?.startPreview()
                    previewPane.setImageBitmap(null)
                    val graphicOverlay = GraphicOverlay(this, null)
                    graphicOverlay.clear()
                    previewOverlay.clear()
                    resultsSpinnerAdapter.reset()
                }
            } else {
                // No permissions granted, request them again
                requestPermissions()
            }
        }

        getImageButton.setOnClickListener { startChooseImageIntentForResult() }

        previewOverlay.setOnClickListener {
            // If only one landmark result
            if (ResultsSpinner.adapter.count == 2) {
                val landmark = ResultsSpinner.adapter.getItem(1).toString()
                startLandmarkInfoIntent(landmark)
            } else { ResultsSpinner.performClick() }
        }
    }

    /** Get landmark info */
    private fun startLandmarkInfoIntent(result: String) {
        customTabHelper.startIntent(result, this)
        ResultsSpinner.setSelection(0)
    }

    /** Choose image activity */
    private fun startChooseImageIntentForResult() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(
            Intent.createChooser(intent, "Select Picture"),
            REQUEST_CODE_CHOOSE_IMAGE
        )
    }

    /** Reload and detect in current image */
    private fun tryReloadAndDetectInImage() {
        try {
            if (imageUri == null) { progressBarHolder.visibility = View.GONE; return }

            Log.d("ImageUri", imageUri.toString())

            previewOverlay.clear() // Clear the overlay first
            val resizedBitmap: Bitmap? = cameraHelper.getBitmap(contentResolver, imageUri!!)
            previewPane?.setImageBitmap(resizedBitmap)
            resizedBitmap?.let {
                imageProcessor.process(
                    it,
                    previewOverlay,
                    resultsSpinnerAdapter,
                    progressBarHolder,
                    tvNoResults
                )
            }
        } catch (e: IOException) {
            Log.e(TAG, "Error retrieving saved image")
            progressBarHolder.visibility = View.GONE
        }
    }

    companion object {
        private const val TAG = "MainActivity"

        private const val REQUEST_CODE_CHOOSE_IMAGE = 1002
        private const val PERMISSIONS_REQUEST_CODE = 1
    }
}
