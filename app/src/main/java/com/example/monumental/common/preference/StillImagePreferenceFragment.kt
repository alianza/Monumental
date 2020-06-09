@file:Suppress("DEPRECATION")

package com.example.monumental.common.preference

import android.os.Bundle
import android.preference.PreferenceFragment
import com.example.monumental.R

/** Configures still image demo settings.  */
class StillImagePreferenceFragment : PreferenceFragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.preference_still_image)
    }
}