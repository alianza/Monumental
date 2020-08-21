package com.example.monumental

import android.graphics.Bitmap
import android.widget.ArrayAdapter
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
        resultsSpinnerAdapter: ArrayAdapter<CharSequence>
    ) {
        latestImage = data
        latestImageMetaData = frameMetadata
        if (processingImage == null && processingMetaData == null) {
            if (graphicOverlay != null) {
                processLatestImage(graphicOverlay, resultsSpinnerAdapter)
            }
        }
    }

    // Bitmap version
    override fun process(bitmap: Bitmap?, graphicOverlay: GraphicOverlay?, resultsSpinnerAdapter: ArrayAdapter<CharSequence>) {
        if (graphicOverlay != null) {
            detectInVisionImage(
                null, /* bitmap */
                FirebaseVisionImage.fromBitmap(bitmap!!),
                null,
                graphicOverlay,
                resultsSpinnerAdapter
            )
        }
    }

    @Synchronized
    private fun processLatestImage(graphicOverlay: GraphicOverlay, resultsSpinnerAdapter: ArrayAdapter<CharSequence>) {
        processingImage = latestImage
        processingMetaData = latestImageMetaData
        latestImage = null
        latestImageMetaData = null
        if (processingImage != null && processingMetaData != null) {
            processImage(processingImage!!, processingMetaData!!, graphicOverlay, resultsSpinnerAdapter)
        }
    }

    private fun processImage(
        data: ByteBuffer,
        frameMetadata: FrameMetadata,
        graphicOverlay: GraphicOverlay,
        resultsSpinnerAdapter: ArrayAdapter<CharSequence>
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
            graphicOverlay, resultsSpinnerAdapter
        )
    }

    private fun detectInVisionImage(
        originalCameraImage: Bitmap?,
        image: FirebaseVisionImage,
        metadata: FrameMetadata?,
        graphicOverlay: GraphicOverlay,
        resultsSpinnerAdapter: ArrayAdapter<CharSequence>
    ) {
        detectInImage(image)
            .addOnSuccessListener { results ->
                val notNullOriginalCameraImage = originalCameraImage
                        ?: Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
                val notNullMetadata = metadata ?: FrameMetadata.Builder().build()
                onSuccess(notNullOriginalCameraImage, results, notNullMetadata, graphicOverlay, resultsSpinnerAdapter)
            }
            .addOnFailureListener { e -> onFailure(e) }
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
        resultsSpinnerAdapter: ArrayAdapter<CharSequence>
    )

    protected abstract fun onFailure(e: Exception)

    abstract fun onSuccess(
        originalCameraImage: Bitmap?,
        results: List<FirebaseVisionCloudLandmark>,
        frameMetadata: FrameMetadata,
        graphicOverlay: GraphicOverlay,
        resultsSpinnerAdapter: ArrayAdapter<CharSequence>
    )
}
