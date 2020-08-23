package com.example.monumental

import android.graphics.Bitmap
import android.widget.ArrayAdapter
import android.widget.FrameLayout
import androidx.annotation.GuardedBy
import com.example.monumental.common.BitmapUtils
import com.example.monumental.common.FrameMetadata
import com.example.monumental.common.GraphicOverlay
import com.example.monumental.common.VisionImageProcessor
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
        resultsSpinnerAdapter: ArrayAdapter<CharSequence>,
        progressBarHolder: FrameLayout
    ) {
        latestImage = data
        latestImageMetaData = frameMetadata
        if (processingImage == null && processingMetaData == null) {
            if (graphicOverlay != null) {
                processLatestImage(graphicOverlay, resultsSpinnerAdapter, progressBarHolder)
            }
        }
    }

    // Bitmap version
    override fun process(
        bitmap: Bitmap?,
        graphicOverlay: GraphicOverlay?,
        resultsSpinnerAdapter: ArrayAdapter<CharSequence>,
        progressBarHolder: FrameLayout
    ) {
        if (graphicOverlay != null) {
            detectInVisionImage(
                null, /* bitmap */
                FirebaseVisionImage.fromBitmap(bitmap!!),
                null,
                graphicOverlay,
                resultsSpinnerAdapter,
                progressBarHolder
            )
        }
    }

    @Synchronized
    private fun processLatestImage(
        graphicOverlay: GraphicOverlay,
        resultsSpinnerAdapter: ArrayAdapter<CharSequence>,
        progressBarHolder: FrameLayout
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
                progressBarHolder
            )
        }
    }

    private fun processImage(
        data: ByteBuffer,
        frameMetadata: FrameMetadata,
        graphicOverlay: GraphicOverlay,
        resultsSpinnerAdapter: ArrayAdapter<CharSequence>,
        progressBarHolder: FrameLayout
    ) {
        val metadata = FirebaseVisionImageMetadata.Builder()
            .setFormat(FirebaseVisionImageMetadata.IMAGE_FORMAT_NV21)
            .setWidth(frameMetadata.width)
            .setHeight(frameMetadata.height)
            .setRotation(frameMetadata.rotation)
            .build()

        val bitmap = BitmapUtils.getBitmap(data, frameMetadata)
        detectInVisionImage(
            bitmap, FirebaseVisionImage.fromByteBuffer(data, metadata), frameMetadata,
            graphicOverlay, resultsSpinnerAdapter, progressBarHolder
        )
    }

    private fun detectInVisionImage(
        originalCameraImage: Bitmap?,
        image: FirebaseVisionImage,
        metadata: FrameMetadata?,
        graphicOverlay: GraphicOverlay,
        resultsSpinnerAdapter: ArrayAdapter<CharSequence>,
        progressBarHolder: FrameLayout
    ) {
        detectInImage(image)
            .addOnSuccessListener { results ->
                val notNullOriginalCameraImage = originalCameraImage
                        ?: Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
                val notNullMetadata = metadata ?: FrameMetadata.Builder().build()
                onSuccess(notNullOriginalCameraImage, results, notNullMetadata, graphicOverlay, resultsSpinnerAdapter, progressBarHolder)
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
        resultsSpinnerAdapter: ArrayAdapter<CharSequence>,
        progressBarHolder: FrameLayout
    )

    protected abstract fun onFailure(e: Exception, progressBarHolder: FrameLayout)

    abstract fun onSuccess(
        originalCameraImage: Bitmap?,
        results: List<FirebaseVisionCloudLandmark>,
        frameMetadata: FrameMetadata,
        graphicOverlay: GraphicOverlay,
        resultsSpinnerAdapter: ArrayAdapter<CharSequence>,
        progressBarHolder: FrameLayout
    )
}
