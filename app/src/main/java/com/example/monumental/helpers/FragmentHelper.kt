package com.example.monumental.helpers

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.monumental.R
import com.example.monumental.ui.journey.JourneyFragment
import com.example.monumental.ui.landmark.LandmarkFragment

class FragmentHelper(private val context: AppCompatActivity) {

    var journeyFragmentIsOpen = false
    var landmarkFragmentIsOpen = false

    private val journeyFragment: JourneyFragment = JourneyFragment.newInstance()
    private val landmarkFragment: LandmarkFragment = LandmarkFragment.newInstance()

    fun openJourneyFragment() {
        context.supportFragmentManager.beginTransaction()
            .replace(R.id.journey_fragment_container, journeyFragment)
            .commitNow()
        journeyFragmentIsOpen = true
    }

    fun closeJourneyFragment(): Boolean {
        if (journeyFragmentIsOpen) {
            context.supportFragmentManager.beginTransaction()
                .remove(journeyFragment)
                .commitNow()
            journeyFragmentIsOpen = false
            return true
        }
        return false
    }

    fun openLandmarkFragment(journeyId: Int) {
        val arguments = Bundle()
        arguments.putInt("JourneyId", journeyId)

        landmarkFragment.arguments = arguments
        context.supportFragmentManager.beginTransaction()
            .replace(R.id.landmark_fragment_container, landmarkFragment)
            .commitNow()
        landmarkFragmentIsOpen = true
    }

    fun closeLandmarkFragment():Boolean {
        if (landmarkFragmentIsOpen) {
            context.supportFragmentManager.beginTransaction()
                .remove(landmarkFragment)
                .commitNow()
            landmarkFragmentIsOpen = false
            return true
        }
        return false
    }

    fun toggleJourneyFragment() {
        if (journeyFragmentIsOpen) {
            if (landmarkFragmentIsOpen) {
                closeLandmarkFragment()
            }
            closeJourneyFragment()
        } else {
            openJourneyFragment()
        }
    }
}