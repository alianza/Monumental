@file:Suppress("DEPRECATION")

package com.example.monumental

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.hardware.Camera
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore.Images.Media.getBitmap
import android.util.Log
import android.util.Pair
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import androidx.appcompat.app.AppCompatActivity
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
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.max

class MainActivity : AppCompatActivity() {

    private var selectedSize: String = SIZE_PREVIEW
    private var isLandScape: Boolean = false
    private var imageUri: Uri? = null
    // Max width (portrait mode)
    private var imageMaxWidth = 0
    // Max height (portrait mode)
    private var imageMaxHeight = 0
    private var imageProcessor: VisionImageProcessor? = null
    private var pictureFile: File? = null
    private var camera: Camera? = null
    private var preview: CameraPreview? = null
    var check = 0

    private lateinit var picture: Camera.PictureCallback
    private lateinit var resultsSpinnerAdapter: ArrayAdapter<CharSequence>

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
            takeImageButton.setImageDrawable(ContextCompat.getDrawable(applicationContext ,R.drawable.ic_autorenew_black_24dp))
            tryReloadAndDetectInImage()
        }
    }

    /** Request permissions result */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        // When permissions granted, start the camera(preview)
        when(requestCode) {
            1 -> {
                setupCamera()
            }
        }
    }

    /** Start everything up */
    private fun initViews() {
        requestPermissions()
        setupResultsSpinner()
        setupCamera()
        setupListeners()
        createImageProcessor()
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
            params?.focusMode = Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE
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
                pictureFile = getOutputMediaFile() ?: run {
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

                imageUri = getOutputMediaFileUri()
                camera?.stopPreview()
                tryReloadAndDetectInImage()
            }
        }

        isLandScape = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
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
                    takeImageButton.setImageDrawable(ContextCompat.getDrawable(applicationContext, R.drawable.ic_autorenew_black_24dp))
                } else {
                    pictureFile = null
                    imageUri = null
                    takeImageButton.setImageDrawable(ContextCompat.getDrawable(applicationContext, R.drawable.ic_camera_black_24dp))
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
        startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse(getString(R.string.info_url, result))
            )
        )
        ResultsSpinner.setSelection(0)
    }

    /** Create the image processor */
    private fun createImageProcessor() {
        imageProcessor = CloudLandmarkRecognitionProcessor()
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
        if (params!!.flashMode == Camera.Parameters.FLASH_MODE_ON) {
            params.flashMode = Camera.Parameters.FLASH_MODE_OFF
            item.icon = ContextCompat.getDrawable(applicationContext, R.drawable.ic_baseline_flash_on_24)
            Toast.makeText(applicationContext, "Flash off", Toast.LENGTH_SHORT).show()
        } else {
            params.flashMode = Camera.Parameters.FLASH_MODE_ON
            item.icon = ContextCompat.getDrawable(applicationContext, R.drawable.ic_baseline_flash_off_24)
            Toast.makeText(applicationContext, "Flash on", Toast.LENGTH_SHORT).show()
        }
        camera?.parameters = params
        println("Toggled flash to: " + params.flashMode)
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
            val targetedSize = getTargetedWidthHeight()

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

    /** Returns max image width, always for portrait mode. Caller needs to swap width / height for
    landscape mode. */
    private fun getImageMaxWidth(): Int {
        if (imageMaxWidth == 0) {
            // Calculate the max width in portrait mode. This is done lazily since we need to wait for
            // a UI layout pass to get the right values. So delay it to first time image rendering time.
            imageMaxWidth = if (isLandScape) {
                (previewPane.parent as View).height - controlPanel.height
            } else {
                (previewPane.parent as View).width
            }
        }

        return imageMaxWidth
    }

    /** Returns max image height, always for portrait mode. Caller needs to swap width / height for
    landscape mode. */
    private fun getImageMaxHeight(): Int {
        if (imageMaxHeight == 0) {
            // Calculate the max width in portrait mode. This is done lazily since we need to wait for
            // a UI layout pass to get the right values. So delay it to first time image rendering time.
            imageMaxHeight = if (isLandScape) {
                (previewPane.parent as View).width
            } else {
                (previewPane.parent as View).height - controlPanel.height
            }
        }

        return imageMaxHeight
    }

    /** Gets the targeted width / height. */
    private fun getTargetedWidthHeight(): Pair<Int, Int> {
        val targetWidth: Int
        val targetHeight: Int

        when (selectedSize) {
            SIZE_PREVIEW -> {
                val maxWidthForPortraitMode = getImageMaxWidth()
                val maxHeightForPortraitMode = getImageMaxHeight()
                targetWidth = if (isLandScape) maxHeightForPortraitMode else maxWidthForPortraitMode
                targetHeight =
                    if (isLandScape) maxWidthForPortraitMode else maxHeightForPortraitMode
            }
            SIZE_640_480 -> {
                targetWidth = if (isLandScape) 640 else 480
                targetHeight = if (isLandScape) 480 else 640
            }
            SIZE_1024_768 -> {
                targetWidth = if (isLandScape) 1024 else 768
                targetHeight = if (isLandScape) 768 else 1024
            }
            else -> throw IllegalStateException("Unknown size")
        }

        return Pair(targetWidth, targetHeight)
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

    /** Create a file Uri for saving an image */
    private fun getOutputMediaFileUri(): Uri {
        return Uri.fromFile(getOutputMediaFile())
    }

    /** Create a File for saving an image */
    private fun getOutputMediaFile(): File? {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        val mediaStorageDir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
            "Monumental"
        )
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        mediaStorageDir.apply {
            if (!exists() && !mkdirs()) {
                Log.d("Monumental", "failed to create directory")
                return null
            }
        }

        // Create a media file name
        val timeStamp = SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z", Locale.getDefault()).format(Date())
        return File("${mediaStorageDir.path}${File.separator}IMG_$timeStamp.jpg")
    }

    companion object {
        private const val TAG = "MainActivity"

        private const val SIZE_PREVIEW = "w:max" // Available on-screen width.
        private const val SIZE_1024_768 = "w:1024" // ~1024*768 in a normal ratio
        private const val SIZE_640_480 = "w:640" // ~640*480 in a normal ratio

        private const val REQUEST_CHOOSE_IMAGE = 1002
    }
}
