package com.example.monumental.cloudtextrecognition

import android.graphics.Bitmap
import android.util.Log
import com.example.monumental.VisionProcessorBase
import com.example.monumental.common.FrameMetadata
import com.example.monumental.common.GraphicOverlay
import com.google.android.gms.tasks.Task
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.document.FirebaseVisionDocumentText
import com.google.firebase.ml.vision.document.FirebaseVisionDocumentTextRecognizer

/** Processor for the cloud document text detector demo.  */
class CloudDocumentTextRecognitionProcessor : VisionProcessorBase<FirebaseVisionDocumentText>() {

    private val detector: FirebaseVisionDocumentTextRecognizer =
            FirebaseVision.getInstance().cloudDocumentTextRecognizer

    override fun detectInImage(image: FirebaseVisionImage): Task<FirebaseVisionDocumentText> {
        return detector.processImage(image)
    }

    override fun onSuccess(
        originalCameraImage: Bitmap?,
        results: FirebaseVisionDocumentText,
        frameMetadata: FrameMetadata,
        graphicOverlay: GraphicOverlay
    ) {
        graphicOverlay.clear()
        Log.d(TAG, "detected text is: ${results.text}")
        val blocks = results.blocks
        for (i in blocks.indices) {
            val paragraphs = blocks[i].paragraphs
            for (j in paragraphs.indices) {
                val words = paragraphs[j].words
                for (l in words.indices) {
                    val symbols = words[l].symbols
                    for (m in symbols.indices) {
                        val cloudDocumentTextGraphic = object: CloudDocumentTextGraphic(
                            graphicOverlay,
                            symbols[m]
                        ) {}
                        graphicOverlay.add(cloudDocumentTextGraphic)
                        graphicOverlay.postInvalidate()
                    }
                }
            }
        }
    }

    override fun onFailure(e: Exception) {
        Log.w(TAG, "Cloud Document Text detection failed.$e")
    }

    companion object {

        private const val TAG = "CloudDocTextRecProc"
    }
}
