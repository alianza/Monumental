@file:Suppress("DEPRECATION")

package com.example.monumental.view.common.helpers

import android.net.Uri
import android.os.Environment
import android.util.Log
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class MediaFileHelper {

    /**
     * Creates a file Uri for saving an image
     *
     * @return Uri from File
     */
    fun getOutputMediaFileUri(): Uri {
        return Uri.fromFile(getOutputMediaFile())
    }

    /**
     * Creates a File for saving an image
     *
     * @return File
     */
    fun getOutputMediaFile(): File? {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        val mediaStorageDir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
            "Monumental"
        )
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        mediaStorageDir.apply {
            if (!exists() && !mkdirs()) {
                Log.d("Monumental", "failed to create directory")
                return null
            }
        }

        // Create a media file name from date
        val timeStamp =
            SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z", Locale.getDefault()).format(Date())
        return File("${mediaStorageDir.path}${File.separator}IMG_$timeStamp.jpg")
    }
}