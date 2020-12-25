package com.example.monumental.model.data.cloudLandmarkRecognition

import android.graphics.Bitmap
import androidx.annotation.GuardedBy
import androidx.lifecycle.MutableLiveData
import com.example.monumental.model.entity.LandmarkResultList
import com.example.monumental.view.common.BitmapUtils
import com.example.monumental.view.common.FrameMetadata
import com.example.monumental.view.common.GraphicOverlay
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

    private var bitmapUtils: BitmapUtils = BitmapUtils

    /**
     * Process ByteBuffer image for Landmarks
     *
     * @param data image to process
     * @param frameMetadata meta data for frame (dimensions)
     * @param graphicOverlay overlay for displaying landmark boundaries
     * @param resultsList list to collect and return results
     */
    @Synchronized
    override fun process(
        data: ByteBuffer?,
        frameMetadata: FrameMetadata?,
        graphicOverlay: GraphicOverlay?,
        resultsList: MutableLiveData<LandmarkResultList>
    ) {
        latestImage = data
        latestImageMetaData = frameMetadata
        if (processingImage == null && processingMetaData == null && graphicOverlay != null) {
            processLatestImage(graphicOverlay, resultsList)
        }
    }

    /**
     * Process Bitmap image for Landmarks
     *
     * @param bitmap image to process
     * @param graphicOverlay overlay for displaying landmark boundaries
     * @param resultsList list to collect and return results
     */
    override fun process(
        bitmap: Bitmap?,
        graphicOverlay: GraphicOverlay?,
        resultsList: MutableLiveData<LandmarkResultList>
    ) {
        if (graphicOverlay != null) {
            detectInVisionImage(
                null, /* bitmap */
                FirebaseVisionImage.fromBitmap(bitmap!!),
                null,
                graphicOverlay,
                resultsList
            )
        }
    }

    /**
     * Process latest image for Landmarks
     *
     * @param graphicOverlay overlay for displaying landmark boundaries
     * @param resultsList list to collect and return results
     */
    @Synchronized
    private fun processLatestImage(
        graphicOverlay: GraphicOverlay,
        resultsList: MutableLiveData<LandmarkResultList>
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
                resultsList
            )
        }
    }

    /**
     * Process ByteBuffer image
     *
     * @param data image to process
     * @param frameMetadata meta data for frame (dimensions)
     * @param graphicOverlay overlay for displaying landmark boundaries
     * @param resultsList list to collect and return results
     */
    private fun processImage(
        data: ByteBuffer,
        frameMetadata: FrameMetadata,
        graphicOverlay: GraphicOverlay,
        resultsList: MutableLiveData<LandmarkResultList>
    ) {
        val metadata = FirebaseVisionImageMetadata.Builder()
            .setFormat(FirebaseVisionImageMetadata.IMAGE_FORMAT_NV21)
            .setWidth(frameMetadata.width)
            .setHeight(frameMetadata.height)
            .setRotation(frameMetadata.rotation)
            .build()

        val bitmap = bitmapUtils.getBitmap(data, frameMetadata)
        detectInVisionImage(
            bitmap,
            FirebaseVisionImage.fromByteBuffer(data, metadata),
            frameMetadata,
            graphicOverlay,
            resultsList
        )
    }

    /**
     * Process Bitmap image
     *
     * @param image image to process
     * @param originalCameraImage FirebaseVisionImage
     * @param metadata meta data for frame (dimensions)
     * @param graphicOverlay overlay for displaying landmark boundaries
     * @param resultsList list to collect and return results
     */
    private fun detectInVisionImage(
        originalCameraImage: Bitmap?,
        image: FirebaseVisionImage,
        metadata: FrameMetadata?,
        graphicOverlay: GraphicOverlay,
        resultsList: MutableLiveData<LandmarkResultList>
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
                    resultsList
                )
            }
            .addOnFailureListener { e -> onFailure(e, resultsList) }
    }

    /** Stops the underlying machine learning model and release resources.  */
    override fun stop() { } // Empty

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
        resultsList: MutableLiveData<LandmarkResultList>
    )

    protected abstract fun onFailure(e: Exception, resultsList: MutableLiveData<LandmarkResultList>)

    /**
     * Callback that executes with a successful detection result.
     *
     * @param originalCameraImage hold the original image from camera, used to draw the background
     * image.
     */
    abstract fun onSuccess(
        originalCameraImage: Bitmap?,
        results: List<FirebaseVisionCloudLandmark>,
        frameMetadata: FrameMetadata,
        graphicOverlay: GraphicOverlay,
        resultsList: MutableLiveData<LandmarkResultList>
    )
}
