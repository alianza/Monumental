// Copyright 2018 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
@file:Suppress("DEPRECATION")

package com.example.monumental.view.common

import android.annotation.SuppressLint
import android.hardware.Camera
import android.hardware.Camera.CameraInfo
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.example.monumental.model.data.cloudlandmarkrecognition.VisionImageProcessor
import com.example.monumental.model.entity.LandmarkResultList
import com.google.android.gms.common.images.Size
import java.nio.ByteBuffer

/**
 * Manages the camera and allows UI updates on top of it (e.g. overlaying extra Graphics or
 * displaying extra information). This receives preview frames from the camera at a specified rate,
 * sending those frames to child classes' detectors / classifiers as fast as it is able to process.
 */
open class CameraSource(
    private val graphicOverlay: GraphicOverlay,
    private val landmarkResultList: MutableLiveData<LandmarkResultList>
) {

    private var camera: Camera? = null

    /**
     * Returns the selected camera; one of [.CAMERA_FACING_BACK] or [ ][.CAMERA_FACING_FRONT].
     */
    var cameraFacing =
        CAMERA_FACING_BACK
        private set

    /**
     * Rotation of the device, and thus the associated preview images captured from the device. See
     * Frame.Metadata#getRotation().
     */
    private var rotation = 0

    /** Returns the preview size that is currently in use by the underlying camera.  */
    var previewSize: Size? = null
        private set

    private val processingRunnable: FrameProcessingRunnable
    private val processorLock = Any()

    // @GuardedBy("processorLock")
    private var frameProcessor: VisionImageProcessor? = null

    // ==============================================================================================
    // Frame processing
    // ==============================================================================================

    /**
     * This runnable controls access to the underlying receiver, calling it to process frames when
     * available from the camera. This is designed to run detection on frames as fast as possible
     * (i.e., without unnecessary context switching or waiting on the next frame).
     *
     *
     * While detection is running on a frame, new frames may be received from the camera. As these
     * frames come in, the most recent frame is held onto as pending. As soon as detection and its
     * associated processing is done for the previous frame, detection on the mostly recently received
     * frame will immediately start on the same thread.
     */
    private inner class FrameProcessingRunnable : Runnable {
        // This lock guards all of the member variables below.
        private val lock = Object()
        private var active = true

        // These pending variables hold the state associated with the new frame awaiting processing.
        private var pendingFrameData: ByteBuffer? = null

        /**
         * As long as the processing thread is active, this executes detection on frames continuously.
         * The next pending frame is either immediately available or hasn't been received yet. Once it
         * is available, we transfer the frame info to local variables and run detection on that frame.
         * It immediately loops back for the next frame without pausing.
         *
         *
         * If detection takes longer than the time in between new frames from the camera, this will
         * mean that this loop will run without ever waiting on a frame, avoiding any context switching
         * or frame acquisition time latency.
         *
         *
         * If you find that this is using more CPU than you'd like, you should probably decrease the
         * FPS setting above to allow for some idle time in between frames.
         */
        @SuppressLint("InlinedApi")
        override fun run() {
            var data: ByteBuffer?
            while (true) {
                synchronized(lock) {
                    while (active && (pendingFrameData == null)) {
                        try {
                            // Wait for the next frame to be received from the camera, since we
                            // don't have it yet.
                            lock.wait()
                        } catch (e: InterruptedException) {
                            Log.d(
                                TAG,
                                "Frame processing loop terminated.",
                                e
                            )
                            return
                        }
                    }
                    if (!active) {
                        // Exit the loop once this camera source is stopped or released.  We check
                        // this here, immediately after the wait() above, to handle the case where
                        // setActive(false) had been called, triggering the termination of this
                        // loop.
                        return
                    }

                    // Hold onto the frame data locally, so that we can use this for detection
                    // below.  We need to clear pendingFrameData to ensure that this buffer isn't
                    // recycled back to the camera before we are done using that data.
                    data = pendingFrameData
                    pendingFrameData = null
                }

                // The code below needs to run outside of synchronization, because this will allow
                // the camera to add pending frame(s) while we are running detection on the current
                // frame.
                try {
                    synchronized(processorLock) {
                        Log.d(
                            TAG,
                            "Process an image"
                        )
                        frameProcessor!!.process(
                            data,
                            FrameMetadata.Builder()
                                .setWidth(previewSize!!.width)
                                .setHeight(previewSize!!.height)
                                .setRotation(rotation)
                                .setCameraFacing(cameraFacing)
                                .build(),
                            graphicOverlay,
                            landmarkResultList
                        )
                    }
                } catch (t: Exception) {
                    Log.e(
                        TAG,
                        "Exception thrown from receiver.",
                        t
                    )
                } finally {
                    camera!!.addCallbackBuffer(data!!.array())
                }
            }
        }
    }

    companion object {
        const val CAMERA_FACING_BACK = CameraInfo.CAMERA_FACING_BACK

        private const val TAG = "CameraSource"
    }

    init {
        graphicOverlay.clear()
        processingRunnable = FrameProcessingRunnable()
    }
}
