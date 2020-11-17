package com.example.monumental.common.helpers

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction.*
import com.example.monumental.R
import com.example.monumental.model.Journey
import com.example.monumental.view.journey.JourneyFragment
import com.example.monumental.view.landmark.LandmarkFragment

class FragmentManager(private val activity: AppCompatActivity) {

    private var journeyFragmentIsOpen = false
    private var landmarkFragmentIsOpen = false

    private val journeyFragment: JourneyFragment = JourneyFragment.newInstance()
    private var landmarkFragment: LandmarkFragment = LandmarkFragment.newInstance()

    private fun openJourneyFragment() {
        activity.supportFragmentManager.beginTransaction()
            .setTransition(TRANSIT_FRAGMENT_OPEN)
            .replace(R.id.journey_fragment_container, journeyFragment)
            .commitNow()
        journeyFragmentIsOpen = true
    }

    fun closeJourneyFragment(): Boolean {
        if (journeyFragmentIsOpen) {
            activity.supportFragmentManager.beginTransaction()
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
        activity.supportFragmentManager.beginTransaction()
            .setTransition(TRANSIT_FRAGMENT_OPEN)
            .replace(R.id.landmark_fragment_container, landmarkFragment)
            .commitNow()
        landmarkFragmentIsOpen = true
    }

    fun closeLandmarkFragment(): Boolean {
        if (landmarkFragmentIsOpen) {
            activity.supportFragmentManager.beginTransaction()
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