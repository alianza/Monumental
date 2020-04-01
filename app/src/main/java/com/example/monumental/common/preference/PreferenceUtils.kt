package com.example.monumental.common.preference

import android.annotation.SuppressLint
import android.content.Context
import android.preference.PreferenceManager
import androidx.annotation.StringRes
import androidx.core.util.Preconditions
import com.example.monumental.R
import com.example.monumental.common.CameraSource
import com.example.monumental.common.CameraSource.SizePair
import com.google.android.gms.common.images.Size
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions
import com.google.firebase.ml.vision.objects.FirebaseVisionObjectDetectorOptions
import java.util.*

/** Utility class to retrieve shared preferences.  */
object PreferenceUtils {
    fun saveString(
        context: Context,
        @StringRes prefKeyId: Int,
        value: String?
    ) {
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit()
            .putString(context.getString(prefKeyId), value)
            .apply()
    }

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

    fun getObjectDetectorOptionsForStillImage(
        context: Context
    ): FirebaseVisionObjectDetectorOptions {
        val sharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(context)
        val enableMultipleObjects = sharedPreferences.getBoolean(
            context.getString(
                R.string.pref_key_still_image_object_detector_enable_multiple_objects
            ),
            false
        )
        val enableClassification = sharedPreferences.getBoolean(
            context.getString(R.string.pref_key_still_image_object_detector_enable_classification),
            true
        )
        val builder =
            FirebaseVisionObjectDetectorOptions.Builder()
                .setDetectorMode(FirebaseVisionObjectDetectorOptions.SINGLE_IMAGE_MODE)
        if (enableMultipleObjects) {
            builder.enableMultipleObjects()
        }
        if (enableClassification) {
            builder.enableClassification()
        }
        return builder.build()
    }

    fun getObjectDetectorOptionsForLivePreview(
        context: Context
    ): FirebaseVisionObjectDetectorOptions {
        val sharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(context)
        val enableMultipleObjects = sharedPreferences.getBoolean(
            context.getString(
                R.string.pref_key_live_preview_object_detector_enable_multiple_objects
            ),
            false
        )
        val enableClassification = sharedPreferences.getBoolean(
            context.getString(R.string.pref_key_live_preview_object_detector_enable_classification),
            true
        )
        val builder =
            FirebaseVisionObjectDetectorOptions.Builder()
                .setDetectorMode(FirebaseVisionObjectDetectorOptions.STREAM_MODE)
        if (enableMultipleObjects) {
            builder.enableMultipleObjects()
        }
        if (enableClassification) {
            builder.enableClassification()
        }
        return builder.build()
    }

    fun getFaceDetectorOptionsForLivePreview(
        context: Context
    ): FirebaseVisionFaceDetectorOptions {
        val landmarkMode =
            getModeTypePreferenceValue(
                context,
                R.string.pref_key_live_preview_face_detection_landmark_mode,
                FirebaseVisionFaceDetectorOptions.NO_LANDMARKS
            )
        val contourMode =
            getModeTypePreferenceValue(
                context,
                R.string.pref_key_live_preview_face_detection_contour_mode,
                FirebaseVisionFaceDetectorOptions.ALL_CONTOURS
            )
        val classificationMode =
            getModeTypePreferenceValue(
                context,
                R.string.pref_key_live_preview_face_detection_classification_mode,
                FirebaseVisionFaceDetectorOptions.NO_CLASSIFICATIONS
            )
        val performanceMode =
            getModeTypePreferenceValue(
                context,
                R.string.pref_key_live_preview_face_detection_performance_mode,
                FirebaseVisionFaceDetectorOptions.FAST
            )
        val sharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(context)
        val enableFaceTracking = sharedPreferences.getBoolean(
            context.getString(R.string.pref_key_live_preview_face_detection_face_tracking), false
        )
        val minFaceSize =
            sharedPreferences.getString(
                context.getString(R.string.pref_key_live_preview_face_detection_min_face_size),
                "0.1"
            )!!.toFloat()
        val optionsBuilder =
            FirebaseVisionFaceDetectorOptions.Builder()
                .setLandmarkMode(landmarkMode)
                .setContourMode(contourMode)
                .setClassificationMode(classificationMode)
                .setPerformanceMode(performanceMode)
                .setMinFaceSize(minFaceSize)
        if (enableFaceTracking) {
            optionsBuilder.enableTracking()
        }
        return optionsBuilder.build()
    }

    fun getAutoMLRemoteModelName(context: Context): String? {
        val sharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(context)
        val modelNamePrefKey =
            context.getString(R.string.pref_key_live_preview_automl_remote_model_name)
        val defaultModelName = "mlkit_flowers"
        var remoteModelName =
            sharedPreferences.getString(modelNamePrefKey, defaultModelName)
        if (remoteModelName!!.isEmpty()) {
            remoteModelName = defaultModelName
        }
        return remoteModelName
    }

    fun getAutoMLRemoteModelChoice(context: Context): String? {
        val modelChoicePrefKey =
            context.getString(R.string.pref_key_live_preview_automl_remote_model_choices)
        val defaultModelChoice =
            context.getString(R.string.pref_entries_automl_models_local)
        val sharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(context)
        return sharedPreferences.getString(modelChoicePrefKey, defaultModelChoice)
    }

    /**
     * Mode type preference is backed by [android.preference.ListPreference] which only support
     * storing its entry value as string type, so we need to retrieve as string and then convert to
     * integer.
     */
    private fun getModeTypePreferenceValue(
        context: Context, @StringRes prefKeyResId: Int, defaultValue: Int
    ): Int {
        val sharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(context)
        val prefKey = context.getString(prefKeyResId)
        return sharedPreferences.getString(prefKey, defaultValue.toString())!!.toInt()
    }

    fun isCameraLiveViewportEnabled(context: Context): Boolean {
        val sharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(context)
        val prefKey = context.getString(R.string.pref_key_camera_live_viewport)
        return sharedPreferences.getBoolean(prefKey, false)
    }
}