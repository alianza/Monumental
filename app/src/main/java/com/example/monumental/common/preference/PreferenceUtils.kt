@file:Suppress("DEPRECATION")

package com.example.monumental.common.preference

import android.annotation.SuppressLint
import android.content.Context
import android.preference.PreferenceManager
import androidx.core.util.Preconditions
import com.example.monumental.R
import com.example.monumental.common.CameraSource
import com.example.monumental.common.CameraSource.SizePair
import com.google.android.gms.common.images.Size
import java.util.*

/** Utility class to retrieve shared preferences.  */
object PreferenceUtils {

    @SuppressLint("RestrictedApi")
    fun getCameraPreviewSizePair(context: Context, cameraId: Int): SizePair? {
        Preconditions.checkArgument(
            cameraId == CameraSource.CAMERA_FACING_BACK
                    || cameraId == CameraSource.CAMERA_FACING_FRONT
        )
        val previewSizePrefKey: String
        val pictureSizePrefKey: String
        if (cameraId == CameraSource.CAMERA_FACING_BACK) {
            previewSizePrefKey = context.getString(R.string.pref_key_rear_camera_preview_size)
            pictureSizePrefKey = context.getString(R.string.pref_key_rear_camera_picture_size)
        } else {
            previewSizePrefKey = context.getString(R.string.pref_key_front_camera_preview_size)
            pictureSizePrefKey = context.getString(R.string.pref_key_front_camera_picture_size)
        }
        return try {
            val sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(context)
            SizePair(
                Size.parseSize(
                    Objects.requireNonNull(
                        sharedPreferences.getString(previewSizePrefKey, null)
                    )
                ),
                Size.parseSize(
                    Objects.requireNonNull(
                        sharedPreferences.getString(pictureSizePrefKey, null)
                    )
                )
            )
        } catch (e: Exception) {
            null
        }
    }

}