package com.example.monumental.common.preference

import android.os.Bundle
import android.preference.PreferenceFragment
import androidx.appcompat.app.AppCompatActivity
import com.example.monumental.R
import com.example.monumental.common.preference.SettingsActivity.LaunchSource

/**
 * Hosts the preference fragment to configure settings for a demo activity that specified by the
 * [LaunchSource].
 */
class SettingsActivity : AppCompatActivity() {
    /** Specifies where this activity is launched from.  */
    enum class LaunchSource(
        val titleResId: Int,
        val prefFragmentClass: Class<out PreferenceFragment?>
    ) {
        STILL_IMAGE(
            R.string.pref_screen_title_still_image,
            StillImagePreferenceFragment::class.java
        );
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        val launchSource =
            intent.getSerializableExtra(EXTRA_LAUNCH_SOURCE) as LaunchSource
        val actionBar = supportActionBar
        actionBar?.setTitle(launchSource.titleResId)
        try {
            fragmentManager
                .beginTransaction()
                .replace(
                    R.id.settings_container,
                    launchSource.prefFragmentClass.getDeclaredConstructor().newInstance()
                )
                .commit()
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    companion object {
        const val EXTRA_LAUNCH_SOURCE = "extra_launch_source"
    }
}