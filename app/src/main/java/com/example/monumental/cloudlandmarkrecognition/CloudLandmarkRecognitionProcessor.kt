package com.example.monumental.cloudlandmarkrecognition

import android.graphics.Bitmap
import android.util.Log
import com.example.monumental.VisionProcessorBase
import com.example.monumental.common.FrameMetadata
import com.example.monumental.common.GraphicOverlay
import com.google.android.gms.tasks.Task
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.cloud.FirebaseVisionCloudDetectorOptions
import com.google.firebase.ml.vision.cloud.landmark.FirebaseVisionCloudLandmark
import com.google.firebase.ml.vision.cloud.landmark.FirebaseVisionCloudLandmarkDetector
import com.google.firebase.ml.vision.common.FirebaseVisionImage

/** Cloud Landmark Detector Demo.  */
class CloudLandmarkRecognitionProcessor : VisionProcessorBase<List<FirebaseVisionCloudLandmark>>() {

    private val detector: FirebaseVisionCloudLandmarkDetector

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

    override fun onSuccess(
        originalCameraImage: Bitmap?,
        results: List<FirebaseVisionCloudLandmark>,
        frameMetadata: FrameMetadata,
        graphicOverlay: GraphicOverlay
    ) {
        graphicOverlay.clear()
        Log.d(TAG, "cloud landmark size: ${results.size}")
        val resultNames: MutableList<String> = ArrayList()

        for (result in results) {
            Log.d(TAG, "Landmark: ${result.landmark}")
        }

        results.forEach {
            Log.d(TAG, "cloud landmark: $it")
            val cloudLandmarkGraphic = object: CloudLandmarkGraphic(graphicOverlay, it) {}
            graphicOverlay.add(cloudLandmarkGraphic)
            resultNames.add(it.landmark)
        }
        graphicOverlay.postInvalidate()

        try {
            println("First result: ${results.first().landmark}")
        } catch (e: NoSuchElementException) {
            println("Empty landmark list $e")
        }


//        MainActivity().getInstance()?.resultsSpinnerAdapter?.clear()
//        MainActivity().getInstance()?.resultsSpinnerAdapter?.addAll(resultNames)
//        MainActivity().getInstance()?.resultsSpinnerAdapter?.notifyDataSetChanged()

//        try {
//           if (results.first().landmark !== "") {
//               MainActivity().openResultSearch(results.first().landmark)
//           }
//        } catch (e: Throwable) {
//            println("Can't open search result. $e")
//        }

//        startActivity(
//            context,
//            Intent(
//                Intent.ACTION_VIEW,
//                Uri.parse("https://www.google.com/search?q=${results.first().landmark}")
//            ).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
//            null
//        )
    }

    override fun onFailure(e: Exception) {
        Log.e(TAG, "Cloud Landmark detection failed $e")
    }

    companion object {
        private const val TAG = "CloudLmkRecProc"
    }
}
