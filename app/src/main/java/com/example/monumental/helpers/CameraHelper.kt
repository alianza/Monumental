@file:Suppress("DEPRECATION")

package com.example.monumental.helpers

import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Camera
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.monumental.R
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

class CameraHelper(private val context: Context) {

    fun setParameters (cameraInstance: Camera) {
        val params: Camera.Parameters? = cameraInstance.parameters
        // Check for focus mode support
        if (cameraInstance.parameters?.supportedFocusModes!!.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            params?.focusMode = Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE
        }
        params?.setRotation(90)
        cameraInstance.parameters = params

        cameraInstance.setDisplayOrientation(90)
        cameraInstance.enableShutterSound(true)
    }

    fun savePicture(pictureFile: File, data: ByteArray) {
        try {
            val fos = FileOutputStream(pictureFile)
            fos.write(data)
            fos.close()
            println("Wrote file" + pictureFile.path)
        } catch (e: FileNotFoundException) {
            Log.d(TAG, "File not found: ${e.message}")
        } catch (e: IOException) {
            Log.d(TAG, "Error accessing file: ${e.message}")
        }
    }

    /** Check if this device has a camera */
    fun hasCamera(): Boolean {
        return context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)
    }

    /** A safe way to get an instance of the Camera object. */
    fun getCameraInstance(): Camera? {
        return try {
            Camera.open() // attempt to get a Camera instance
        } catch (e: Exception) {
            // Camera is not available (in use or does not exist)
            null // returns null if camera is unavailable
        }
    }

    /** Toggles the camera flash */
    fun toggleFlash(item: MenuItem, cameraInstance: Camera, context: Context) {
        val params: Camera.Parameters? = cameraInstance.parameters

        // Check for flash support
        if (params!!.supportedFlashModes != null) {
            if (params.flashMode == Camera.Parameters.FLASH_MODE_ON) {
                params.flashMode = Camera.Parameters.FLASH_MODE_OFF
                item.icon =
                    ContextCompat.getDrawable(context, R.drawable.ic_baseline_flash_on_24)
                Toast.makeText(context, context.getString(R.string.flash_off), Toast.LENGTH_SHORT).show()
            } else {
                params.flashMode = Camera.Parameters.FLASH_MODE_ON
                item.icon =
                    ContextCompat.getDrawable(context, R.drawable.ic_baseline_flash_off_24)
                Toast.makeText(context, context.getString(R.string.flash_on), Toast.LENGTH_SHORT).show()
            }
            cameraInstance.parameters = params
        } else {
            Toast.makeText(context, context.getString(R.string.flash_not_supported), Toast.LENGTH_SHORT).show()
            item.icon =
                ContextCompat.getDrawable(context, R.drawable.ic_baseline_flash_off_24)
        }
    }

    companion object {
        private const val TAG = "CameraHelper"
    }
}