@file:Suppress("DEPRECATION")

package com.example.monumental.view.common

import android.content.ContentValues.TAG
import android.content.Context
import android.hardware.Camera
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import java.io.IOException

/**
 * A basic Camera preview class
 *
 * @property mCamera Camera instance
 * @constructor
 * Camera constructor with context and no instance yet
 *
 * @param context ApplicationContext
 */
class CameraPreview(context: Context, private val mCamera: Camera?) : SurfaceView(context), SurfaceHolder.Callback {

    constructor(context: Context) : this(context, null)

    private val mHolder: SurfaceHolder = holder.apply {
        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        addCallback(this@CameraPreview)
        // deprecated setting, but required on Android versions prior to 3.0
        setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)
    }

    /**
     * When the preview surface has been created draw the Camera preview
     *
     * @param holder SurfaceHolder of the preview service
     */
    override fun surfaceCreated(holder: SurfaceHolder) {
        // The Surface has been created, now tell the camera where to draw the preview.
        mCamera?.apply {
            try {
                setPreviewDisplay(holder)
                startPreview()
            } catch (e: IOException) {
                Log.d(TAG, "Error setting camera preview: ${e.message}")
            }
        }
    }

    /**
     * Takes care of releasing the Camera instance in the activity
     *
     * @param holder SurfaceHolder
     */
    override fun surfaceDestroyed(holder: SurfaceHolder) {
//        mCamera?.release()
    }

    /**
     * When the preview surface changes
     *
     * @param holder SurfaceHolder
     * @param format  Integer
     * @param w Integer
     * @param h Integer
     */
    override fun surfaceChanged(holder: SurfaceHolder, format: Int, w: Int, h: Int) {
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.
        if (mHolder.surface == null) {
            // preview surface does not exist
            return
        }

        // stop preview before making changes
        try {
            mCamera?.stopPreview()
        } catch (e: Exception) {
            // ignore: tried to stop a non-existent preview
        }

        // set preview size and make any resize, rotate or
        // reformatting changes here

        // start preview with new settings
        mCamera?.apply {
            try {
                setPreviewDisplay(mHolder)
                startPreview()
            } catch (e: Exception) {
                Log.d(TAG, "Error starting camera preview: ${e.message}")
            }
        }
    }
}