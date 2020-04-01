package com.example.monumental.common.preference

import android.hardware.Camera
import android.os.Bundle
import android.preference.EditTextPreference
import android.preference.ListPreference
import android.preference.Preference.OnPreferenceChangeListener
import android.preference.PreferenceCategory
import android.preference.PreferenceFragment
import android.widget.Toast
import androidx.annotation.StringRes
import com.example.monumental.R
import com.example.monumental.common.CameraSource
import com.example.monumental.common.CameraSource.Companion.CAMERA_FACING_BACK
import com.example.monumental.common.CameraSource.Companion.CAMERA_FACING_FRONT
import com.example.monumental.common.CameraSource.Companion.generateValidPreviewSizeList
import com.example.monumental.common.CameraSource.Companion.selectSizePair
import com.example.monumental.common.preference.PreferenceUtils.saveString
import java.util.*

/** Configures live preview demo settings.  */
class LivePreviewPreferenceFragment : PreferenceFragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.preference_live_preview)
        setUpCameraPreferences()
        setUpFaceDetectionPreferences()
        setUpListPreference(R.string.pref_key_live_preview_automl_remote_model_choices)
    }

    private fun setUpCameraPreferences() {
        setUpCameraPreviewSizePreference(
            R.string.pref_key_rear_camera_preview_size,
            R.string.pref_key_rear_camera_picture_size,
            CAMERA_FACING_BACK
        )
        setUpCameraPreviewSizePreference(
            R.string.pref_key_front_camera_preview_size,
            R.string.pref_key_front_camera_picture_size,
            CAMERA_FACING_FRONT
        )
    }

    private fun setUpCameraPreviewSizePreference(
        @StringRes previewSizePrefKeyId: Int, @StringRes pictureSizePrefKeyId: Int, cameraId: Int
    ) {
        val previewSizePreference =
            findPreference(getString(previewSizePrefKeyId)) as ListPreference
        var camera: Camera? = null
        try {
            camera = Camera.open(cameraId)
            val previewSizeList =
                generateValidPreviewSizeList(
                    camera
                )
            val previewSizeStringValues =
                arrayOfNulls<String>(previewSizeList.size)
            val previewToPictureSizeStringMap: MutableMap<String, String> =
                HashMap()
            for (i in previewSizeList.indices) {
                val sizePair = previewSizeList[i]
                previewSizeStringValues[i] = sizePair.preview.toString()
                if (sizePair.picture != null) {
                    previewToPictureSizeStringMap[sizePair.preview.toString()] =
                        sizePair.picture.toString()
                }
            }
            previewSizePreference.entries = previewSizeStringValues
            previewSizePreference.entryValues = previewSizeStringValues
            if (previewSizePreference.entry == null) {
                // First time of opening the Settings page.
                val sizePair =
                    selectSizePair(
                        camera,
                        CameraSource.DEFAULT_REQUESTED_CAMERA_PREVIEW_WIDTH,
                        CameraSource.DEFAULT_REQUESTED_CAMERA_PREVIEW_HEIGHT
                    )
                val previewSizeString = sizePair!!.preview.toString()
                previewSizePreference.value = previewSizeString
                previewSizePreference.summary = previewSizeString
                saveString(
                    activity,
                    pictureSizePrefKeyId,
                    if (sizePair.picture != null) sizePair.picture.toString() else null
                )
            } else {
                previewSizePreference.summary = previewSizePreference.entry
            }
            previewSizePreference.onPreferenceChangeListener =
                OnPreferenceChangeListener { preference, newValue ->
                    val newPreviewSizeStringValue = newValue as String
                    previewSizePreference.summary = newPreviewSizeStringValue
                    saveString(
                        this@LivePreviewPreferenceFragment.activity,
                        pictureSizePrefKeyId,
                        previewToPictureSizeStringMap[newPreviewSizeStringValue]
                    )
                    true
                }
        } catch (e: Exception) {
            // If there's no camera for the given camera id, hide the corresponding preference.
            (findPreference(getString(R.string.pref_category_key_camera)) as PreferenceCategory)
                .removePreference(previewSizePreference)
        } finally {
            camera?.release()
        }
    }

    private fun setUpFaceDetectionPreferences() {
        setUpListPreference(R.string.pref_key_live_preview_face_detection_landmark_mode)
        setUpListPreference(R.string.pref_key_live_preview_face_detection_contour_mode)
        setUpListPreference(R.string.pref_key_live_preview_face_detection_classification_mode)
        setUpListPreference(R.string.pref_key_live_preview_face_detection_performance_mode)
        val minFaceSizePreference =
            findPreference(getString(R.string.pref_key_live_preview_face_detection_min_face_size)) as EditTextPreference
        minFaceSizePreference.summary = minFaceSizePreference.text
        minFaceSizePreference.onPreferenceChangeListener =
            OnPreferenceChangeListener { preference, newValue ->
                try {
                    val minFaceSize: Float = java.lang.Float.parseFloat(newValue as String)
                    if (minFaceSize in 0.0f..1.0f) {
                        minFaceSizePreference.summary = newValue
                        return@OnPreferenceChangeListener true
                    }
                } catch (e: NumberFormatException) {
                    // Fall through intentionally.
                }
                Toast.makeText(
                        this@LivePreviewPreferenceFragment.activity,
                        R.string.pref_toast_invalid_min_face_size, Toast.LENGTH_LONG
                    )
                    .show()
                false
            }
    }

    private fun setUpListPreference(@StringRes listPreferenceKeyId: Int) {
        val listPreference =
            findPreference(getString(listPreferenceKeyId)) as ListPreference
        listPreference.summary = listPreference.entry
        listPreference.onPreferenceChangeListener =
            OnPreferenceChangeListener { preference, newValue ->
                val index = listPreference.findIndexOfValue(newValue as String)
                listPreference.summary = listPreference.entries[index]
                true
            }
    }
}