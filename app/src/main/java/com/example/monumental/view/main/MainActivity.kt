@file:Suppress("DEPRECATION")

package com.example.monumental.view.main

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.graphics.Bitmap
import android.graphics.Rect
import android.hardware.Camera
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.preference.PreferenceManager
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.monumental.R
import com.example.monumental.model.entity.Journey
import com.example.monumental.model.entity.Landmark
import com.example.monumental.model.entity.LandmarkResult
import com.example.monumental.model.entity.LandmarkResultList
import com.example.monumental.view.common.CameraPreview
import com.example.monumental.view.common.FragmentManager
import com.example.monumental.view.common.GraphicOverlay
import com.example.monumental.view.common.helpers.CameraHelper
import com.example.monumental.view.common.helpers.CustomTabHelper
import com.example.monumental.view.common.helpers.ImageHelper
import com.example.monumental.view.firstTime.FirstTimeActivity
import com.example.monumental.viewModel.main.MainViewModel
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.dialog_results_view.view.*
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
    private var currentJourney: Journey? = null

    val actionDelayVal = 250L // Delay Value for delaying user interactions so animations can finish
    private var dialog: AlertDialog? = null // Variable to reference the results dialog

    private lateinit var flashOptionsItem: MenuItem
    private lateinit var journeysOptionsItem: MenuItem
    private lateinit var landmarksList: MutableLiveData<LandmarkResultList>
    private lateinit var viewModel: MainViewModel
            lateinit var fragmentManager: FragmentManager
    private lateinit var cameraHelper: CameraHelper
    private lateinit var customTabHelper: CustomTabHelper
    private lateinit var imageHelper: ImageHelper
    private lateinit var resultsAdapter: ResultsAdapter

    /** onCreate method to set layout, theme and initiate the views */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setTheme(R.style.AppTheme)
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        initViews().runCatching { animateControlPanel() }
    }

    /** Catch back press event */
    override fun onBackPressed() {
        if (fragmentManager.closeLandmarkFragment()) { return }
        if (fragmentManager.closeJourneyFragment()) { resetViews(); return }
        super.onBackPressed()
    }

    /** Inflate options menu */
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.still_image_menu, menu)
        this.flashOptionsItem = menu?.getItem(0)!!
        this.journeysOptionsItem = menu.getItem(1)
        return true
    }

    /** Settings button intent */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.flash -> { if (checkSelfPermission(Manifest.permission.CAMERA) == PERMISSION_GRANTED) {
                        cameraHelper.toggleFlash(item, camera!!, this) } else { requestPermissions() }
                        return true }
            R.id.journeys -> { if (fragmentManager.toggleJourneyFragment()) { // is open
                        camera?.stopPreview()
                        item.icon = ContextCompat.getDrawable(this, R.drawable.ic_baseline_explore_off_24)
                    } else { resetViews() } // is closed
                             return true } }
        return super.onOptionsItemSelected(item)
    }

    /** Handle touch events and hide keyboard when moving focus from editText */
    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val view = currentFocus
            if (view is EditText) {
                val outRect = Rect()
                view.getGlobalVisibleRect(outRect)
                if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                    view.clearFocus()
                    val imm: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0) } } }
        return super.dispatchTouchEvent(event)
    }

    /** Activity result, take image and try detect landmarks */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_CHOOSE_IMAGE && resultCode == Activity.RESULT_OK) {
            progressBarHolder.visibility = View.VISIBLE
            tvNoResults.visibility = View.INVISIBLE
            imageUri = data!!.data // In this case, imageUri is returned by the chooser, save it.
            takeImageButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_autorenew_black_24dp))
            tryReloadAndDetectInImage() } else if (requestCode == REQUEST_CODE_FIRST_TIME && resultCode == Activity.RESULT_OK) {
                initViews() } // When return from First Time activity
    }

    /** Request permissions result */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) { PERMISSIONS_REQUEST_CODE -> { setupCamera() } } // When permissions granted, start the camera(preview)
    }

    /** Start everything up */
    private fun initViews() {
        if (firstTimeActivityHasStarted()) {
            instantiateClasses()
            requestPermissions()
            setupResultsRecyclerView()
//            setupCamera()
            setupListeners()
        } else { doFirstTimeActivity() }
    }

    private fun firstTimeActivityHasStarted(): Boolean { // Check if first time activity has started in the past
        val prefs = PreferenceManager.getDefaultSharedPreferences(baseContext)
        return prefs.getBoolean(getString(R.string.pref_previously_started), false)
    }

    private fun doFirstTimeActivity() {
        val intent = Intent(this, FirstTimeActivity::class.java)
        startActivityForResult(intent ,REQUEST_CODE_FIRST_TIME)
    }

    /** Instantiate all classes */
    private fun instantiateClasses() {
        customTabHelper = CustomTabHelper()
        imageHelper = ImageHelper(previewPane, controlPanel)
        cameraHelper = CameraHelper()
        fragmentManager = FragmentManager(this)
        landmarksList = MutableLiveData(LandmarkResultList(emptyArray<LandmarkResult>().toMutableList()))
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
            if (hasCamera()) {
                camera = cameraHelper.getCameraInstance()
                preview = camera?.let { CameraPreview(this, it) }
                cameraHelper.setParameters(camera!!)
                preview?.also { val preview: FrameLayout = findViewById(R.id.camera_preview); preview.addView(it) } // Set the Preview view as the content of the activity
                picture = Camera.PictureCallback { data, _ ->
                    pictureFile = viewModel.getOutputMediaFile() ?: run {
                        Log.d(TAG, ("Error creating media file, check storage permissions"))
                        return@PictureCallback }
                cameraHelper.savePicture(pictureFile!!, data)
                imageUri = viewModel.getOutputMediaFileUri()
                camera?.stopPreview()
                tryReloadAndDetectInImage() } } }
    }

    /** Setup all event listeners */
    @SuppressLint("ClickableViewAccessibility")
    private fun setupListeners() {
        landmarksList.observe(this, { landmarkResultList -> // Listener for landmark results
            if (landmarkResultList.results.isNotEmpty()) {
                resultsAdapter.clear()
                landmarkResultList.results.forEach { resultsAdapter.landmarks.add(it.name) }
                resultsAdapter.notifyDataSetChanged()
                tvNoResults.visibility = View.INVISIBLE
            } else { if (tvNoResults.visibility == View.INVISIBLE) { tvNoResults.visibility = View.VISIBLE } }
            progressBarHolder.visibility = View.GONE })

        viewModel.activeJourney.observe(this, { journey -> if (journey == null) // Listener for current active Journey
        { this.currentJourney = null } else { this.currentJourney = journey } })

        resultsButton.setOnClickListener { showResultsDialog() } // Listener for resultsButton onclick

        takeImageButton.setOnClickListener { // Listener for the takeImageButton
            tvNoResults.visibility = View.INVISIBLE
            if (checkSelfPermission(Manifest.permission.CAMERA) == PERMISSION_GRANTED) {
                if (pictureFile == null && imageUri == null) { takePicture() } else { resetPicture() }
            } else { requestPermissions() } }

        getImageButton.setOnClickListener { startChooseImageIntentForResult() } // Listener for get image from local storage button

        previewOverlay.setOnClickListener { // Listener for the entire image previews
            if (resultsAdapter.itemCount == 1) {
                val landmark = resultsAdapter.getItem(0)
                customTabHelper.startIntent(landmark, this)
            } else { showResultsDialog() } }
    }

    private fun animateControlPanel() {
        Handler(Looper.getMainLooper()).postDelayed({
            container.scaleX = 0F
            container.scaleY = 0F
            Handler(Looper.getMainLooper()).postDelayed({
                container.animate().scaleX(1F).scaleY(1F).setDuration(350)
                    .setInterpolator(AccelerateDecelerateInterpolator()).start()
            }, 500)
        }, 0)
    }

    /** Callback when clicked on landmark row in ResultsRecyclerView */
    private fun onLandmarkResultClick(landmark: String) {
        val result = landmark.replace(" ", "+") // TODO Move string replace to data
        customTabHelper.startIntent(result, this)
    }

    /** Callback when clicked on landmark save button in ResultsRecyclerView */
    private fun onLandmarkResultSave(landmark: String) {
        if (currentJourney != null) {
            viewModel.saveLandmark(Landmark(null, landmark, imageUri.toString(), Date(), currentJourney?.id))
            Toast.makeText(this, getString(R.string.saved_landmark, landmark, currentJourney?.name), Toast.LENGTH_LONG).show() }
     else { Toast.makeText(this, getString(R.string.no_current_journey), Toast.LENGTH_LONG).show()
            onOptionsItemSelected(journeysOptionsItem)
            this.dialog?.dismiss() }
    }

    /** Resets the current taken picture */
    private fun resetPicture() {
        progressBarHolder.visibility = View.INVISIBLE
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

    /** Check if this device has a camera */
    private fun hasCamera(): Boolean { return this.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY) }

    /** Displays the Landmark results dialog */
    private fun showResultsDialog() {
        val dialog = AlertDialog.Builder(this)
        val view = layoutInflater.inflate(R.layout.dialog_results_view, null)
        view.rvResults.layoutManager = StaggeredGridLayoutManager(1, RecyclerView.VERTICAL)
        view.rvResults.adapter = resultsAdapter
        if (resultsAdapter.itemCount == 0) { // If no results show empty dialog view
            view.dialog_results_title.text = getString(R.string.no_landmark_tip)
            dialog.setNeutralButton(getString(R.string.show_intro)) { it, _ ->
                doFirstTimeActivity()
                it.dismiss() } }
        resultsAdapter.notifyDataSetChanged()
        dialog.setView(view)
        this.dialog = dialog.show()
    }

    /** Resets the activity and starts the camera preview */
    private fun resetViews() {
        camera?.startPreview()
        invalidateOptionsMenu()
        supportActionBar?.title = getString(R.string.app_name)
    }

    /** Choose image activity */
    private fun startChooseImageIntentForResult() {
        tvNoResults.visibility = View.INVISIBLE
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQUEST_CODE_CHOOSE_IMAGE)
    }

    private fun isNetworkAvailable() =
    (this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager).run {
        getNetworkCapabilities(activeNetwork)?.run {
                       hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                    || hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                    || hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) } ?: false
    }

    /** Reload and detect in current image */
    private fun tryReloadAndDetectInImage() {
      try { if (imageUri == null) { progressBarHolder.visibility = View.GONE; return }
            Log.d("ImageUri", imageUri.toString())
            previewOverlay.clear() // Clear the overlay first
            val resizedBitmap: Bitmap? = viewModel.getScaledBitmap(contentResolver, imageUri!!, imageHelper)

            if (imageUri!!.scheme == "content") { // if Image from device Content
                val bitmapData = cameraHelper.bitmapToByteArray(resizedBitmap!!)
                pictureFile = viewModel.getOutputMediaFile()
                cameraHelper.savePicture(pictureFile!!, bitmapData)
                imageUri = viewModel.getOutputMediaFileUri() }

            previewPane?.setImageBitmap(resizedBitmap)
            if (isNetworkAvailable()) { // Has internet
                resizedBitmap?.let { viewModel.doDetectInBitmap(it, previewOverlay, landmarksList) }
            } else { // Has NO internet
                Toast.makeText(this, getString(R.string.no_network), Toast.LENGTH_LONG).show()
                Handler(Looper.getMainLooper()).postDelayed({
                    startActivity(Intent(Settings.ACTION_WIRELESS_SETTINGS)); resetPicture() }, 2500) }
        } catch (e: IOException) {
            Log.e(TAG, "Error retrieving saved image")
            progressBarHolder.visibility = View.GONE }
    }

    companion object {
        private const val TAG = "MainActivity"
        private const val PERMISSIONS_REQUEST_CODE = 1
        private const val REQUEST_CODE_CHOOSE_IMAGE = 1002
        private const val REQUEST_CODE_FIRST_TIME = 2
    }
}
