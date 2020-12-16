@file:Suppress("DEPRECATION")

package com.example.monumental.view.common.helpers

import android.content.Context
import android.graphics.Bitmap
import android.hardware.Camera
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.monumental.R
import java.io.*

class CameraHelper {

    /**
     * Set parameters to Camera
     *
     * @param cameraInstance Camera instance to set parameters for
     */
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

    /**
     * Saves a File to the device storage
     *
     * @param pictureFile File to write
     * @param data Data to write
     */
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

    /**
     * Convert bitmap to byte array
     *
     * @param bitmap Bitmap to convert
     * @return ByteArray Converted ByteArray
     */
    fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
        val bos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos)
        return bos.toByteArray()
    }

    /**
     * A safe way to get an instance of the Camera object
     *
     * @return Camera object
     */
    fun getCameraInstance(): Camera? {
        return try {
            Camera.open() // attempt to get a Camera instance
        } catch (e: Exception) {
            // Camera is not available (in use or does not exist)
            null // returns null if camera is unavailable
        }
    }

    /**
     * Toggles the camera flash
     *
     * @param item MenuItem to change drawable after toggle
     * @param cameraInstance Camera instance to toggle flash for
     * @param context ApplicationContext
     */
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