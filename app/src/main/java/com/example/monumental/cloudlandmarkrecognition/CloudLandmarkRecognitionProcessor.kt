package com.example.monumental.cloudlandmarkrecognition

import android.graphics.Bitmap
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import com.example.monumental.common.FrameMetadata
import com.example.monumental.common.GraphicOverlay
import com.example.monumental.common.ResultsSpinnerAdapter
import com.example.monumental.common.VisionProcessorBase
import com.google.android.gms.tasks.Task
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.cloud.FirebaseVisionCloudDetectorOptions
import com.google.firebase.ml.vision.cloud.landmark.FirebaseVisionCloudLandmark
import com.google.firebase.ml.vision.cloud.landmark.FirebaseVisionCloudLandmarkDetector
import com.google.firebase.ml.vision.common.FirebaseVisionImage

/** Cloud Landmark Detector */
class CloudLandmarkRecognitionProcessor : VisionProcessorBase<List<FirebaseVisionCloudLandmark>>() {

    private val detector: FirebaseVisionCloudLandmarkDetector

    /** Set options for Cloud Landmark Detector */
    init {
        val options = FirebaseVisionCloudDetectorOptions.Builder()
            .setMaxResults(10)
            .setModelType(FirebaseVisionCloudDetectorOptions.STABLE_MODEL)
            .build()

        detector = FirebaseVision.getInstance().getVisionCloudLandmarkDetector(options)
    }

    override fun detectInImage(image: FirebaseVisionImage): Task<List<FirebaseVisionCloudLandmark>> {
        return detector.detectInImage(image)
    }

    /** Cloud Landmark Detector onSuccess callback */
    override fun onSuccess(
        originalCameraImage: Bitmap?,
        results: List<FirebaseVisionCloudLandmark>,
        frameMetadata: FrameMetadata,
        graphicOverlay: GraphicOverlay,
        resultsSpinnerAdapter: ResultsSpinnerAdapter,
        progressBarHolder: FrameLayout,
        tvNoResults: TextView
    ) {
        // Gather distinct results
        val distinctResults = results.distinctBy { result -> result.landmark }
        graphicOverlay.clear()
        Log.d(TAG, "cloud landmark size: ${results.size}")
        val resultNames: MutableList<String> = ArrayList()

        for (distinctResult in distinctResults) {
            Log.d(TAG, "Landmark: ${distinctResult.landmark}")
        }

        // Add graphics overlay and log for each landmark result
        distinctResults.forEach {
            val cloudLandmarkGraphic = object : CloudLandmarkGraphic(graphicOverlay, it) {}
            graphicOverlay.add(cloudLandmarkGraphic)
            resultNames.add(it.landmark)
        }
        graphicOverlay.postInvalidate()

        // Print first result
        try {
            println("First result: ${results.first().landmark}")
        } catch (e: NoSuchElementException) {
            tvNoResults.visibility = View.VISIBLE
            println("Empty landmark list $e")
        }

        // Add first result
        resultNames.add(0, "More info!")

        // Clear results spinner, add results and notify adapter of changed data
        resultsSpinnerAdapter.clear()
        resultsSpinnerAdapter.addAll(resultNames)
        resultsSpinnerAdapter.notifyDataSetChanged()
        progressBarHolder.visibility = View.GONE
    }

    /** Cloud Landmark Detector onFailure callback */
    override fun onFailure(e: Exception, progressBarHolder: FrameLayout) {
        progressBarHolder.visibility = View.GONE
        Log.e(TAG, "Cloud Landmark detection failed $e")
    }

    companion object {
        private const val TAG = "CloudLmkRecProc"
    }
}