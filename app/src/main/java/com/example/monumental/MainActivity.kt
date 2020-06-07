@file:Suppress("DEPRECATION")

package com.example.monumental

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.hardware.Camera
import android.hardware.Camera.CameraInfo
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.MediaStore.Images.Media.getBitmap
import android.util.Log
import android.util.Pair
import android.view.Menu
import android.view.MenuItem
import android.view.Surface
import android.view.View
import android.widget.FrameLayout
import android.widget.PopupMenu
import androidx.appcompat.app.AppCompatActivity
import com.example.monumental.cloudlandmarkrecognition.CloudLandmarkRecognitionProcessor
import com.example.monumental.common.CameraPreview
import com.example.monumental.common.VisionImageProcessor
import com.example.monumental.common.preference.SettingsActivity
import com.example.monumental.common.preference.SettingsActivity.LaunchSource
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.max

@Suppress("DEPRECATION")
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        requestPermissions(
            arrayOf(
                Manifest.permission.INTERNET,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA
            ), 1
        )

        if (hasCamera()) {
            camera = getCameraInstance()

            preview = camera?.let { CameraPreview(this, it) }

            val params: Camera.Parameters? = camera?.parameters
            params?.focusMode = Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE
            params?.setRotation(90)
//                params?.flashMode = Camera.Parameters.FLASH_MODE_ON
            camera?.parameters = params

            camera?.setDisplayOrientation(90)
            camera?.enableShutterSound(true)

            // Set the Preview view as the content of our activity.
            preview?.also {
                val preview: FrameLayout = findViewById(R.id.camera_preview)
                preview.addView(it)
            }

            val picture = Camera.PictureCallback { data, _ ->
                pictureFile = getOutputMediaFile() ?: run {
                    Log.d(TAG, ("Error creating media file, check storage permissions"))
                    return@PictureCallback
                }

                try {
                    val fos = FileOutputStream(pictureFile)
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

            takeImageButton.setOnClickListener {
                if (pictureFile == null) {
                    camera?.takePicture(null, null, picture)
                    takeImageButton.setImageDrawable(getDrawable(R.drawable.ic_autorenew_black_24dp))
                } else {
                    pictureFile = null
                    takeImageButton.setImageDrawable(getDrawable(R.drawable.ic_camera_black_24dp))
                    camera?.startPreview()
                    previewPane.setImageBitmap(null)
                }
            }
        }

        getImageButton.setOnClickListener { view ->
            // Menu for selecting either: a) take new photo b) select from existing
            val popup = PopupMenu(this, view)
            popup.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.select_images_from_local -> {
                        startChooseImageIntentForResult()
                        true
                    }
                    R.id.take_photo_using_camera -> {
                        startCameraIntentForResult()
                        true
                    }
                    else -> false
                }
            }

            val inflater = popup.menuInflater
            inflater.inflate(R.menu.camera_button_menu, popup.menu)
            popup.show()
        }

        if (previewPane == null) {
            Log.d(TAG, "Preview is null")
        }

        if (previewOverlay == null) {
            Log.d(TAG, "graphicOverlay is null")
        }

        createImageProcessor()

        isLandScape = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

        savedInstanceState?.let {
            imageUri = it.getParcelable(KEY_IMAGE_URI)
            imageMaxWidth = it.getInt(KEY_IMAGE_MAX_WIDTH)
            imageMaxHeight = it.getInt(KEY_IMAGE_MAX_HEIGHT)
            selectedSize = it.getString(KEY_SELECTED_SIZE, "")

            imageUri?.let { _ ->
                tryReloadAndDetectInImage()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume")
        createImageProcessor()
        camera?.startPreview()
        tryReloadAndDetectInImage()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.still_image_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.settings) {
            val intent = Intent(this, SettingsActivity::class.java)
            intent.putExtra(SettingsActivity.EXTRA_LAUNCH_SOURCE, LaunchSource.STILL_IMAGE)
            startActivity(intent)
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    public override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        with(outState) {
            putParcelable(KEY_IMAGE_URI, imageUri)
            putInt(KEY_IMAGE_MAX_WIDTH, imageMaxWidth)
            putInt(KEY_IMAGE_MAX_HEIGHT, imageMaxHeight)
            putString(KEY_SELECTED_SIZE, selectedSize)
        }
    }

    private fun startCameraIntentForResult() {
        // Clean up last time's image
        imageUri = null
        previewPane?.setImageBitmap(null)

        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        takePictureIntent.resolveActivity(packageManager)?.let {
            val values = ContentValues()
            values.put(MediaStore.Images.Media.TITLE, "New Picture")
            values.put(MediaStore.Images.Media.DESCRIPTION, "From Camera")
            imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        }
    }

    private fun startChooseImageIntentForResult() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQUEST_CHOOSE_IMAGE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            tryReloadAndDetectInImage()
        } else if (requestCode == REQUEST_CHOOSE_IMAGE && resultCode == Activity.RESULT_OK) {
            // In this case, imageUri is returned by the chooser, save it.
            imageUri = data!!.data
            tryReloadAndDetectInImage()
        }
    }

    private fun tryReloadAndDetectInImage() {
        try {
            if (imageUri == null) {
                return
            }

            Log.d("ImageUri", imageUri.toString())

            // Clear the overlay first
            previewOverlay?.clear()

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
                imageProcessor?.process(it, previewOverlay)
            }
        } catch (e: IOException) {
            Log.e(TAG, "Error retrieving saved image")
        }
    }

    // Returns max image width, always for portrait mode. Caller needs to swap width / height for
    // landscape mode.
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

    // Returns max image height, always for portrait mode. Caller needs to swap width / height for
    // landscape mode.
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

    // Gets the targeted width / height.
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

    private fun createImageProcessor() {
        imageProcessor = CloudLandmarkRecognitionProcessor()
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

    private fun setCameraDisplayOrientation(
        activity: Activity,
        cameraId: Int,
        camera: Camera
    ) {
        val info = CameraInfo()
        Camera.getCameraInfo(cameraId, info)
        val rotation = activity.windowManager.defaultDisplay
            .rotation
        var degrees = 0
        when (rotation) {
            Surface.ROTATION_0 -> degrees = 0
            Surface.ROTATION_90 -> degrees = 90
            Surface.ROTATION_180 -> degrees = 180
            Surface.ROTATION_270 -> degrees = 270
        }
        var result: Int
        if (info.facing == CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360
            result = (360 - result) % 360 // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360
        }
        camera.setDisplayOrientation(result)
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
            if (!exists()) {
                if (!mkdirs()) {
                    Log.d("Monumental", "failed to create directory")
                    return null
                }
            }
        }

        // Create a media file name
        val timeStamp = SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z").format(Date())
        return File("${mediaStorageDir.path}${File.separator}IMG_$timeStamp.jpg")
    }

    companion object {

        private const val TAG = "MainActivity"

        private const val SIZE_PREVIEW = "w:max" // Available on-screen width.
        private const val SIZE_1024_768 = "w:1024" // ~1024*768 in a normal ratio
        private const val SIZE_640_480 = "w:640" // ~640*480 in a normal ratio

        private const val KEY_IMAGE_URI = "com.googletest.firebase.ml.demo.KEY_IMAGE_URI"
        private const val KEY_IMAGE_MAX_WIDTH =
            "com.googletest.firebase.ml.demo.KEY_IMAGE_MAX_WIDTH"
        private const val KEY_IMAGE_MAX_HEIGHT =
            "com.googletest.firebase.ml.demo.KEY_IMAGE_MAX_HEIGHT"
        private const val KEY_SELECTED_SIZE = "com.googletest.firebase.ml.demo.KEY_SELECTED_SIZE"

        private const val REQUEST_IMAGE_CAPTURE = 1001
        private const val REQUEST_CHOOSE_IMAGE = 1002
    }

}
