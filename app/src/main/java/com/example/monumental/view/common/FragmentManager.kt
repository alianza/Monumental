package com.example.monumental.view.common

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction.*
import com.example.monumental.R
import com.example.monumental.model.entity.Journey
import com.example.monumental.view.journeys.JourneysFragment
import com.example.monumental.view.landmarks.LandmarksFragment

open class FragmentManager(private val activity: AppCompatActivity) {

    private var journeyFragmentIsOpen = false
    private var landmarkFragmentIsOpen = false

    private val journeysFragment: JourneysFragment = JourneysFragment.newInstance()
    private var landmarksFragment: LandmarksFragment = LandmarksFragment.newInstance()

    private fun openJourneyFragment() {
        activity.supportFragmentManager.beginTransaction()
            .setTransition(TRANSIT_FRAGMENT_OPEN)
            .replace(R.id.journey_fragment_container, journeysFragment)
            .commitNow()
        journeyFragmentIsOpen = true
    }

    fun closeJourneyFragment(): Boolean {
        if (journeyFragmentIsOpen) {
            activity.supportFragmentManager.beginTransaction()
                .setTransition(TRANSIT_FRAGMENT_FADE)
                .remove(journeysFragment)
                .commitNow()
            this.journeyFragmentIsOpen = false
            return true
        }
        return false
    }

    fun openLandmarkFragment(journey: Journey) {
        val arguments = Bundle()
        arguments.putParcelable("Journey", journey)

        landmarksFragment.arguments = arguments
        activity.supportFragmentManager.beginTransaction()
            .setTransition(TRANSIT_FRAGMENT_OPEN)
            .replace(R.id.landmark_fragment_container, landmarksFragment)
            .commitNow()
        landmarkFragmentIsOpen = true
    }

    fun closeLandmarkFragment(): Boolean {
        if (landmarkFragmentIsOpen) {
            activity.supportFragmentManager.beginTransaction()
                .setTransition(TRANSIT_FRAGMENT_CLOSE)
                .remove(landmarksFragment)
                .commitNow()
            landmarkFragmentIsOpen = false
            landmarksFragment = LandmarksFragment.newInstance() // Refresh instance
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
