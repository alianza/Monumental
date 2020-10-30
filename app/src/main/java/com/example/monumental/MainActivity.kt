@file:Suppress("DEPRECATION")

package com.example.monumental

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.graphics.Bitmap
import android.hardware.Camera
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.monumental.cloudlandmarkrecognition.CloudLandmarkRecognitionProcessor
import com.example.monumental.common.CameraPreview
import com.example.monumental.common.GraphicOverlay
import com.example.monumental.common.ResultsSpinnerAdapter
import com.example.monumental.common.VisionImageProcessor
import com.example.monumental.helpers.CameraHelper
import com.example.monumental.helpers.CustomTabHelper
import com.example.monumental.helpers.ImageHelper
import com.example.monumental.helpers.MediaFileHelper
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private var pictureFile: File? = null
    private var camera: Camera? = null
    private var preview: CameraPreview? = null
    private var imageUri: Uri? = null
    private var picture: Camera.PictureCallback? = null

    // Recyclerview click offset check
    var check = 0

    private lateinit var customTabHelper: CustomTabHelper
    private lateinit var imageHelper: ImageHelper
    private lateinit var mediaFileHelper: MediaFileHelper
    private lateinit var cameraHelper: CameraHelper
    private lateinit var resultsSpinnerAdapter: ResultsSpinnerAdapter
    private lateinit var imageProcessor: VisionImageProcessor

    /** onCreate method to set layout, theme and initiate the views */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setTheme(R.style.AppTheme)

        initViews()
    }

    /** Inflate options menu */
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.still_image_menu, menu)
        return true
    }

    /** Settings button intent */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.flash) {
            cameraHelper.toggleFlash(item, camera!!, this)
            return true
        }

        return super.onOptionsItemSelected(item)
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

    /** Setup all event listeners */
    @SuppressLint("ClickableViewAccessibility")
    private fun setupListeners() {
        ResultsSpinner.setOnTouchListener { _, _ ->
            if (ResultsSpinner.adapter.count == 2) { // If only one landmark result
                val landmark = ResultsSpinner.adapter.getItem(1).toString()
                startLandmarkInfoIntent(landmark)
            }
            return@setOnTouchListener false
        }

        ResultsSpinner.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                ResultsSpinner.setSelection(0)
                println("Spinner item $position!")

                if (++check > 1 && position != 0) {
                    val textView = ResultsSpinner.selectedView as TextView?
                    var result = textView?.text.toString()

                    result = result.replace(" ", "+")

                    println("Clicked result: $result")
                    startLandmarkInfoIntent(result)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) { }
        }

        takeImageButton.setOnClickListener {
            tvNoResults.visibility = View.GONE
            // Check for required permissions
            if (checkSelfPermission(Manifest.permission.CAMERA) == PERMISSION_GRANTED) {
                if (pictureFile == null && imageUri == null) { // Take picture
                    progressBarHolder.visibility = View.VISIBLE
                    camera?.takePicture(null, null, picture)
                    takeImageButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_autorenew_black_24dp))
                } else { // Reset Picture
                    pictureFile = null
                    imageUri = null
                    takeImageButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_camera_black_24dp))
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
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQUEST_CODE_CHOOSE_IMAGE)
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
