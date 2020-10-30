package com.example.monumental.common

import android.graphics.Bitmap
import android.widget.FrameLayout
import android.widget.TextView
import androidx.annotation.GuardedBy
import com.google.android.gms.tasks.Task
import com.google.firebase.ml.vision.cloud.landmark.FirebaseVisionCloudLandmark
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata
import java.nio.ByteBuffer

/**
 * Abstract base class for ML Kit frame processors. Subclasses need to implement {@link
 * #onSuccess(T, FrameMetadata, GraphicOverlay)} to define what they want to with the detection
 * results and {@link #detectInImage(FirebaseVisionImage)} to specify the detector object.
 *
 * @param <T> The type of the detected feature.
 */
abstract class VisionProcessorBase<T> : VisionImageProcessor {

    // To keep the latest images and its metadata.
    @GuardedBy("this")
    private var latestImage: ByteBuffer? = null

    @GuardedBy("this")
    private var latestImageMetaData: FrameMetadata? = null

    // To keep the images and metadata in process.
    @GuardedBy("this")
    private var processingImage: ByteBuffer? = null

    @GuardedBy("this")
    private var processingMetaData: FrameMetadata? = null

    @Synchronized
    override fun process(
        data: ByteBuffer?,
        frameMetadata: FrameMetadata?,
        graphicOverlay: GraphicOverlay?,
        resultsSpinnerAdapter: ResultsSpinnerAdapter,
        progressBarHolder: FrameLayout,
        tvNoResults: TextView
    ) {
        latestImage = data
        latestImageMetaData = frameMetadata
        if (processingImage == null && processingMetaData == null) {
            if (graphicOverlay != null) {
                processLatestImage(graphicOverlay, resultsSpinnerAdapter, progressBarHolder, tvNoResults)
            }
        }
    }

    // Bitmap version
    override fun process(
        bitmap: Bitmap?,
        graphicOverlay: GraphicOverlay?,
        resultsSpinnerAdapter: ResultsSpinnerAdapter,
        progressBarHolder: FrameLayout,
        tvNoResults: TextView
    ) {
        if (graphicOverlay != null) {
            detectInVisionImage(
                null, /* bitmap */
                FirebaseVisionImage.fromBitmap(bitmap!!),
                null,
                graphicOverlay,
                resultsSpinnerAdapter,
                progressBarHolder,
                tvNoResults
            )
        }
    }

    @Synchronized
    private fun processLatestImage(
        graphicOverlay: GraphicOverlay,
        resultsSpinnerAdapter: ResultsSpinnerAdapter,
        progressBarHolder: FrameLayout,
        tvNoResults: TextView
    ) {
        processingImage = latestImage
        processingMetaData = latestImageMetaData
        latestImage = null
        latestImageMetaData = null
        if (processingImage != null && processingMetaData != null) {
            processImage(
                processingImage!!,
                processingMetaData!!,
                graphicOverlay,
                resultsSpinnerAdapter,
                progressBarHolder,
                tvNoResults
            )
        }
    }

    private fun processImage(
        data: ByteBuffer,
        frameMetadata: FrameMetadata,
        graphicOverlay: GraphicOverlay,
        resultsSpinnerAdapter: ResultsSpinnerAdapter,
        progressBarHolder: FrameLayout,
        tvNoResults: TextView
    ) {
        val metadata = FirebaseVisionImageMetadata.Builder()
            .setFormat(FirebaseVisionImageMetadata.IMAGE_FORMAT_NV21)
            .setWidth(frameMetadata.width)
            .setHeight(frameMetadata.height)
            .setRotation(frameMetadata.rotation)
            .build()

        val bitmap = BitmapUtils.getBitmap(data, frameMetadata)
        detectInVisionImage(
            bitmap,
            FirebaseVisionImage.fromByteBuffer(data, metadata),
            frameMetadata,
            graphicOverlay,
            resultsSpinnerAdapter,
            progressBarHolder,
            tvNoResults
        )
    }

    private fun detectInVisionImage(
        originalCameraImage: Bitmap?,
        image: FirebaseVisionImage,
        metadata: FrameMetadata?,
        graphicOverlay: GraphicOverlay,
        resultsSpinnerAdapter: ResultsSpinnerAdapter,
        progressBarHolder: FrameLayout,
        tvNoResults: TextView
    ) {
        detectInImage(image)
            .addOnSuccessListener { results ->
                val notNullOriginalCameraImage = originalCameraImage
                        ?: Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
                val notNullMetadata = metadata ?: FrameMetadata.Builder().build()
                onSuccess(
                    notNullOriginalCameraImage,
                    results,
                    notNullMetadata,
                    graphicOverlay,
                    resultsSpinnerAdapter,
                    progressBarHolder,
                    tvNoResults
                )
            }
            .addOnFailureListener { e -> onFailure(e, progressBarHolder) }
    }

    override fun stop() {}

    protected abstract fun detectInImage(image: FirebaseVisionImage): Task<T>

    /**
     * Callback that executes with a successful detection result.
     *
     * @param originalCameraImage hold the original image from camera, used to draw the background
     * image.
     */
    protected abstract fun onSuccess(
        originalCameraImage: Bitmap?,
        results: T,
        frameMetadata: FrameMetadata,
        graphicOverlay: GraphicOverlay,
        resultsSpinnerAdapter: ResultsSpinnerAdapter,
        progressBarHolder: FrameLayout,
        tvNoResults: TextView
    )

    protected abstract fun onFailure(e: Exception, progressBarHolder: FrameLayout)

    abstract fun onSuccess(
        originalCameraImage: Bitmap?,
        results: List<FirebaseVisionCloudLandmark>,
        frameMetadata: FrameMetadata,
        graphicOverlay: GraphicOverlay,
        resultsSpinnerAdapter: ResultsSpinnerAdapter,
        progressBarHolder: FrameLayout,
        tvNoResults: TextView
    )
}
