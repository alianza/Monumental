@file:Suppress("DEPRECATION")

package com.example.monumental

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.hardware.Camera
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore.Images.Media.getBitmap
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import com.example.monumental.cloudlandmarkrecognition.CloudLandmarkRecognitionProcessor
import com.example.monumental.common.CameraPreview
import com.example.monumental.common.GraphicOverlay
import com.example.monumental.common.VisionImageProcessor
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import kotlin.math.max

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
            toggleFlash(item)
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    /** Activity result, take image and try detect landmarks */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CHOOSE_IMAGE && resultCode == Activity.RESULT_OK) {
            // In this case, imageUri is returned by the chooser, save it.
            progressBarHolder.visibility = View.VISIBLE
            tvNoResults.visibility = View.GONE
            imageUri = data!!.data
            takeImageButton.setImageDrawable(
                ContextCompat.getDrawable(
                    applicationContext,
                    R.drawable.ic_autorenew_black_24dp
                )
            )
            tryReloadAndDetectInImage()
        }
    }

    /** Request permissions result */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        // When permissions granted, start the camera(preview)
        when (requestCode) {
            1 -> {
                setupCamera()
            }
        }
    }

    /** Start everything up */
    private fun initViews() {

        customTabHelper = CustomTabHelper()
        mediaFileHelper = MediaFileHelper()
        imageHelper = ImageHelper(previewPane, controlPanel)
        imageProcessor = CloudLandmarkRecognitionProcessor()

        requestPermissions()
        setupResultsSpinner()
        setupCamera()
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
            ), 1
        )

    }

    /** setup the resultsSpinner(Adapter) */
    private fun setupResultsSpinner() {
        resultsSpinnerAdapter = ResultsSpinnerAdapter(applicationContext, R.layout.spinner_item)

        // Specify the layout to use when the list of choices appears
        resultsSpinnerAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        // Apply the adapter to the spinner
        ResultsSpinner.adapter = resultsSpinnerAdapter

        resultsSpinnerAdapter.addAll(mutableListOf(getString(R.string.more_info)))

        resultsSpinnerAdapter.notifyDataSetChanged()
    }

    /** Setup the camera */
    private fun setupCamera() {
        if (hasCamera()) {
            camera = getCameraInstance()

            preview = camera?.let { CameraPreview(this, it) }

            val params: Camera.Parameters? = camera?.parameters
            // Check for focus mode support
            if (camera?.parameters?.supportedFocusModes!!.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                params?.focusMode = Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE
            }
            params?.setRotation(90)
            camera?.parameters = params

            camera?.setDisplayOrientation(90)
            camera?.enableShutterSound(true)

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

                try {
                    val fos = FileOutputStream(pictureFile as File)
                    fos.write(data)
                    fos.close()
                } catch (e: FileNotFoundException) {
                    Log.d(TAG, "File not found: ${e.message}")
                } catch (e: IOException) {
                    Log.d(TAG, "Error accessing file: ${e.message}")
                }

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
            // If only one landmark result
            if (ResultsSpinner.adapter.count == 2) {
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

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        takeImageButton.setOnClickListener {
            tvNoResults.visibility = View.GONE
            // Check for required permissions
            if (checkSelfPermission(Manifest.permission.CAMERA) == PERMISSION_GRANTED) {
                if (pictureFile == null && imageUri == null) {
                    progressBarHolder.visibility = View.VISIBLE
                    camera?.takePicture(null, null, picture)
                    takeImageButton.setImageDrawable(
                        ContextCompat.getDrawable(
                            applicationContext,
                            R.drawable.ic_autorenew_black_24dp
                        )
                    )
                } else {
                    pictureFile = null
                    imageUri = null
                    takeImageButton.setImageDrawable(
                        ContextCompat.getDrawable(
                            applicationContext,
                            R.drawable.ic_camera_black_24dp
                        )
                    )
                    camera?.startPreview()
                    previewPane.setImageBitmap(null)
                    val graphicOverlay = GraphicOverlay(this, null)
                    graphicOverlay.clear()
                    previewOverlay.clear()
                    resultsSpinnerAdapter.clear()
                    resultsSpinnerAdapter.addAll(mutableListOf(getString(R.string.more_info)))
                    resultsSpinnerAdapter.notifyDataSetChanged()
                }
            } else {
                // No permissions granted, request them again
                requestPermissions()
            }
        }

        getImageButton.setOnClickListener {
            startChooseImageIntentForResult()
        }

        previewOverlay.setOnClickListener {
            // If only one landmark result
            if (ResultsSpinner.adapter.count == 2) {
                val landmark = ResultsSpinner.adapter.getItem(1).toString()
                startLandmarkInfoIntent(landmark)
            } else {
                ResultsSpinner.performClick()
            }
        }
    }

    private fun startLandmarkInfoIntent(result: String) {
        val builder = CustomTabsIntent.Builder()

        // modify toolbar color
        builder.setToolbarColor(ContextCompat.getColor(this@MainActivity, R.color.colorPrimary))

        // add share button to overflow menu
        builder.addDefaultShareMenuItem()

        // modify back button icon
        builder.setCloseButtonIcon(
            BitmapFactory.decodeResource(
                resources,
                R.drawable.baseline_arrow_back_black_24dp
            )
        )

        // show website title
        builder.setShowTitle(true)

        // animation for enter and exit of tab
        builder.setStartAnimations(this, R.anim.anim_slide_in_left, R.anim.anim_slide_out_left)
        builder.setExitAnimations(this, R.anim.anim_slide_in_right, R.anim.anim_slide_out_right)

        val customTabsIntent = builder.build()

        // check if chrome is available
        val packageName =
            customTabHelper.getPackageNameToUse(this, getString(R.string.info_url, result))

        if (packageName == null) {
            // If chrome not available open in web view
            val intentOpenUri = Intent(this, WebViewActivity::class.java)
            intentOpenUri.putExtra(WebViewActivity.URL, getString(R.string.info_url, result))
            intentOpenUri.putExtra(WebViewActivity.NAME, result)
            startActivity(intentOpenUri)
            overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_left)
        } else {
            // Open chrome custom tab
            customTabsIntent.intent.setPackage(packageName)
            customTabsIntent.launchUrl(this, Uri.parse(getString(R.string.info_url, result)))
        }
        ResultsSpinner.setSelection(0)
    }

    /** Choose image activity */
    private fun startChooseImageIntentForResult() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQUEST_CHOOSE_IMAGE)
    }

    /** Toggles the camera flash */
    private fun toggleFlash(item: MenuItem) {
        val params: Camera.Parameters? = camera?.parameters

        // Check for flash support
        if (params!!.supportedFlashModes != null) {
            if (params.flashMode == Camera.Parameters.FLASH_MODE_ON) {
                params.flashMode = Camera.Parameters.FLASH_MODE_OFF
                item.icon =
                    ContextCompat.getDrawable(applicationContext, R.drawable.ic_baseline_flash_on_24)
                Toast.makeText(applicationContext, getString(R.string.flash_off), Toast.LENGTH_SHORT).show()
            } else {
                params.flashMode = Camera.Parameters.FLASH_MODE_ON
                item.icon =
                    ContextCompat.getDrawable(applicationContext, R.drawable.ic_baseline_flash_off_24)
                Toast.makeText(applicationContext, getString(R.string.flash_on), Toast.LENGTH_SHORT).show()
            }
            camera?.parameters = params
        } else {
            Toast.makeText(applicationContext, getString(R.string.flash_not_supported), Toast.LENGTH_SHORT).show()
            item.icon =
                ContextCompat.getDrawable(applicationContext, R.drawable.ic_baseline_flash_off_24)
        }
    }

    /** Reload and detect in current image */
    private fun tryReloadAndDetectInImage() {
        try {
            if (imageUri == null) {
                progressBarHolder.visibility = View.GONE
                return
            }

            Log.d("ImageUri", imageUri.toString())

            // Clear the overlay first
            previewOverlay.clear()

            val imageBitmap = if (Build.VERSION.SDK_INT < 29) {
                getBitmap(contentResolver, imageUri)
            } else {
                val source = ImageDecoder.createSource(contentResolver, imageUri!!)
                ImageDecoder.decodeBitmap(source)
            }

            // Get the dimensions of the View
            val targetedSize = imageHelper.getTargetedWidthHeight()

            val targetWidth = targetedSize.first
            val maxHeight = targetedSize.second

            // Determine how much to scale down the image
            val scaleFactor = max(
                imageBitmap.width.toFloat() / targetWidth.toFloat(),
                imageBitmap.height.toFloat() / maxHeight.toFloat()
            )

            val resizedBitmap = Bitmap.createScaledBitmap(
                imageBitmap,
                (imageBitmap.width / scaleFactor).toInt(),
                (imageBitmap.height / scaleFactor).toInt(),
                true
            )

            previewPane?.setImageBitmap(resizedBitmap)
            resizedBitmap?.let {
                imageProcessor?.process(
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

    /** Check if this device has a camera */
    private fun hasCamera(): Boolean {
        return applicationContext.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)
    }

    /** A safe way to get an instance of the Camera object. */
    private fun getCameraInstance(): Camera? {
        return try {
            Camera.open() // attempt to get a Camera instance
        } catch (e: Exception) {
            // Camera is not available (in use or does not exist)
            null // returns null if camera is unavailable
        }
    }

    companion object {
        private const val TAG = "MainActivity"

        private const val REQUEST_CHOOSE_IMAGE = 1002
    }
}
