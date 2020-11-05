@file:Suppress("DEPRECATION")

package com.example.monumental.ui.main

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
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
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.monumental.R
import com.example.monumental.cloudlandmarkrecognition.CloudLandmarkRecognitionProcessor
import com.example.monumental.common.CameraPreview
import com.example.monumental.common.GraphicOverlay
import com.example.monumental.common.VisionImageProcessor
import com.example.monumental.helpers.*
import com.example.monumental.model.Journey
import com.example.monumental.model.Landmark
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.dialog_results_view.view.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {

    private var pictureFile: File? = null
    private var camera: Camera? = null
    private var preview: CameraPreview? = null
    private var imageUri: Uri? = null
    private var picture: Camera.PictureCallback? = null

    val actionDelayVal = 250L

    lateinit var fragmentHelper: FragmentHelper
    private lateinit var cameraHelper: CameraHelper
    private lateinit var bitmapHelper: BitmapHelper
    private lateinit var currentJourney: Journey
    private lateinit var customTabHelper: CustomTabHelper
    private lateinit var imageHelper: ImageHelper
    private lateinit var mediaFileHelper: MediaFileHelper
    private lateinit var resultsAdapter: ResultsAdapter
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

    /** Catch back press event */
    override fun onBackPressed() {
        if (fragmentHelper.closeLandmarkFragment()) { return }
        if (fragmentHelper.closeJourneyFragment()) { resetViews(); return }
        super.onBackPressed()
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
                if (fragmentHelper.toggleJourneyFragment()) { // is open
                    camera?.stopPreview()
                    item.icon = ContextCompat.getDrawable(this, R.drawable.ic_baseline_explore_off_24)
                    println("stopPreview")
                } else { // is closed
                    resetViews()
                }
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
                    val imm: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
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
            takeImageButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_autorenew_black_24dp))
            tryReloadAndDetectInImage()
        }
    }

    /** Request permissions result */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) { PERMISSIONS_REQUEST_CODE -> { setupCamera() } } // When permissions granted, start the camera(preview)
    }

    /** Start everything up */
    private fun initViews() {
        instantiateClasses()
        requestPermissions()
        setupResultsRecyclerView()
        setupCamera()
        setupListeners()
    }

    /** Instantiate all classes */
    private fun instantiateClasses() {
        customTabHelper = CustomTabHelper()
        mediaFileHelper = MediaFileHelper()
        imageHelper = ImageHelper(previewPane, controlPanel)
        cameraHelper = CameraHelper(this)
        imageProcessor = CloudLandmarkRecognitionProcessor()
        fragmentHelper = FragmentHelper(this as AppCompatActivity)
        bitmapHelper = BitmapHelper()
    }

    /** Request all required permissions */
    private fun requestPermissions() {
        requestPermissions(
            arrayOf(Manifest.permission.INTERNET, Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA,), PERMISSIONS_REQUEST_CODE)
    }

    /** setup the resultsSpinner(Adapter) */
    private fun setupResultsRecyclerView() {
        resultsAdapter = ResultsAdapter(ArrayList(),
            { string: String -> onLandmarkResultClick(string) },
            { string: String -> onLandmarkResultSave(string) })
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
        viewModel.activeJourney.observe(this, { currentJourney = it!! })

        resultsButton.setOnClickListener { showDialog() }

        takeImageButton.setOnClickListener {
            tvNoResults.visibility = View.GONE
            if (checkSelfPermission(Manifest.permission.CAMERA) == PERMISSION_GRANTED) {
                if (pictureFile == null && imageUri == null) { takePicture() }
                else { resetPicture() }
            } else {
                requestPermissions()
            }
        }

        getImageButton.setOnClickListener { startChooseImageIntentForResult() }

        previewOverlay.setOnClickListener {
            if (resultsAdapter.itemCount == 1) {
                val landmark = resultsAdapter.getItem(0).toString()
                startLandmarkInfoIntent(landmark)
            } else { showDialog() }
        }
    }

    /** Callback when clicked on landmark row in ResultsRecyclerView */
    private fun onLandmarkResultClick(landmark: String) {
        val result = landmark.replace(" ", "+")
        startLandmarkInfoIntent(result)
    }

    /** Callback when clicked on landmark save button in ResultsRecyclerView */
    private fun onLandmarkResultSave(landmark: String) {
        viewModel.saveLandmark(Landmark(null, landmark, imageUri.toString(), Date(), currentJourney.id))
        Toast.makeText(this, getString(R.string.saved_landmark, landmark, currentJourney.name), Toast.LENGTH_LONG).show()
    }

    /** Resets the current taken picture */
    private fun resetPicture() {
        pictureFile = null
        imageUri = null
        takeImageButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_camera_black_24dp))
        camera?.startPreview()
        previewPane.setImageBitmap(null)
        val graphicOverlay = GraphicOverlay(this, null)
        graphicOverlay.clear()
        previewOverlay.clear()
        resultsAdapter.reset()
    }

    /** Takes a picture using the Camera pictureCallback */
    private fun takePicture() {
        progressBarHolder.visibility = View.VISIBLE
        camera?.takePicture(null, null, picture)
        takeImageButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_autorenew_black_24dp))
    }

    /** Displays the Landmark results dialog */
    private fun showDialog() {
        val dialog = AlertDialog.Builder(this)
        val view = layoutInflater.inflate(R.layout.dialog_results_view, null)
        view.rvResults.layoutManager = StaggeredGridLayoutManager(1, RecyclerView.VERTICAL)
        view.rvResults.adapter = resultsAdapter
        if (resultsAdapter.itemCount == 0) { view.dialog_results_title.text = getString(R.string.no_landmark_tip) }
        resultsAdapter.notifyDataSetChanged()
        dialog.setView(view)
        dialog.show()
    }

    /** Resets the activity and starts the camera preview */
    private fun resetViews() {
        camera?.startPreview()
        invalidateOptionsMenu()
        println("startPreview")
        supportActionBar?.title = getString(R.string.app_name)
    }

    /** Get landmark info */
    private fun startLandmarkInfoIntent(result: String) {
        customTabHelper.startIntent(result, this)
    }

    /** Choose image activity */
    private fun startChooseImageIntentForResult() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQUEST_CODE_CHOOSE_IMAGE)
    }

    /** Reload and detect in current image */
    private fun tryReloadAndDetectInImage() {
        try {
            if (imageUri == null) { progressBarHolder.visibility = View.GONE; return }

            Log.d("ImageUri", imageUri.toString())

            previewOverlay.clear() // Clear the overlay first
            val resizedBitmap: Bitmap? = bitmapHelper.getScaledBitmap(contentResolver, imageUri!!, imageHelper)

            if (imageUri!!.scheme == "content") { // if Image from device Content
                val bos = ByteArrayOutputStream() //Convert bitmap to byte array
                resizedBitmap?.compress(Bitmap.CompressFormat.JPEG, 100, bos);
                val bitmapData = bos.toByteArray()

                pictureFile = mediaFileHelper.getOutputMediaFile()
                cameraHelper.savePicture(pictureFile!!, bitmapData)
                imageUri = mediaFileHelper.getOutputMediaFileUri()
            }

            previewPane?.setImageBitmap(resizedBitmap)
            resizedBitmap?.let {
                imageProcessor.process(it, previewOverlay, resultsAdapter, progressBarHolder, tvNoResults)
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
