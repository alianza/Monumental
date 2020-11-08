package com.example.monumental.helpers

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction.*
import com.example.monumental.R
import com.example.monumental.model.Journey
import com.example.monumental.ui.journey.JourneyFragment
import com.example.monumental.ui.landmark.LandmarkFragment

class FragmentHelper(private val context: AppCompatActivity) {

    private var journeyFragmentIsOpen = false
    private var landmarkFragmentIsOpen = false

    private val journeyFragment: JourneyFragment = JourneyFragment.newInstance()
    private var landmarkFragment: LandmarkFragment = LandmarkFragment.newInstance()

    private fun openJourneyFragment() {
        context.supportFragmentManager.beginTransaction()
            .setTransition(TRANSIT_FRAGMENT_OPEN)
            .replace(R.id.journey_fragment_container, journeyFragment)
            .commitNow()
        journeyFragmentIsOpen = true
    }

    fun closeJourneyFragment(): Boolean {
        if (journeyFragmentIsOpen) {
            context.supportFragmentManager.beginTransaction()
                .setTransition(TRANSIT_FRAGMENT_FADE)
                .remove(journeyFragment)
                .commitNow()
            this.journeyFragmentIsOpen = false
            return true
        }
        return false
    }

    fun openLandmarkFragment(journey: Journey) {
        val arguments = Bundle()
        arguments.putParcelable("Journey", journey)

        landmarkFragment.arguments = arguments
        context.supportFragmentManager.beginTransaction()
            .setTransition(TRANSIT_FRAGMENT_OPEN)
            .replace(R.id.landmark_fragment_container, landmarkFragment)
            .commitNow()
        landmarkFragmentIsOpen = true
    }

    fun closeLandmarkFragment(): Boolean {
        if (landmarkFragmentIsOpen) {
            context.supportFragmentManager.beginTransaction()
                .setTransition(TRANSIT_FRAGMENT_CLOSE)
                .remove(landmarkFragment)
                .commitNow()
            landmarkFragmentIsOpen = false
            landmarkFragment = LandmarkFragment.newInstance() // Refresh instance
            return true
        }
        return false
    }

    fun toggleJourneyFragment():Boolean {
        if (journeyFragmentIsOpen) {
            if (landmarkFragmentIsOpen) {
                closeLandmarkFragment()
            }
            closeJourneyFragment()
        } else {
            openJourneyFragment()
        }
        return journeyFragmentIsOpen
    }
}