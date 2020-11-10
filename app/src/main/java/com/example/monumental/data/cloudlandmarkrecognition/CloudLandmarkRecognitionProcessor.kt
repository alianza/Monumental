package com.example.monumental.data.cloudlandmarkrecognition

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.example.monumental.common.FrameMetadata
import com.example.monumental.common.GraphicOverlay
import com.example.monumental.common.VisionProcessorBase
import com.example.monumental.model.LandmarkResult
import com.example.monumental.model.LandmarkResultList
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
        resultsList: MutableLiveData<LandmarkResultList>
    ) {
        val resultNames: MutableList<LandmarkResult> = ArrayList()
        // Gather distinct results
        val distinctResults = results.distinctBy { result -> result.landmark }
        graphicOverlay.clear()

        for (distinctResult in distinctResults) { Log.d(TAG, "Landmark: ${distinctResult.landmark}") }

        // Add graphics overlay and log for each landmark result
        distinctResults.forEach {
            val cloudLandmarkGraphic = object : CloudLandmarkGraphic(graphicOverlay, it) {}
            graphicOverlay.add(cloudLandmarkGraphic)
            resultNames.add(LandmarkResult(it.landmark))
        }
        graphicOverlay.postInvalidate()

        if (results.isEmpty()) {
            resultsList.postValue(LandmarkResultList(emptyArray<LandmarkResult>().toMutableList()))
            println("No Landmark Results")
        }
        resultsList.postValue(LandmarkResultList(resultNames))
    }

    /** Cloud Landmark Detector onFailure callback */
    override fun onFailure(e: Exception, resultsList: MutableLiveData<LandmarkResultList>) {
        resultsList.postValue(LandmarkResultList(emptyArray<LandmarkResult>().toMutableList()))
        Log.e(TAG, "Cloud Landmark detection failed $e")
    }

    companion object {
        private const val TAG = "CloudLmkRecProc"
    }
}
