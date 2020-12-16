package com.example.monumental.view.common

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction.*
import com.example.monumental.R
import com.example.monumental.model.entity.Journey
import com.example.monumental.view.journeys.JourneysFragment
import com.example.monumental.view.landmarks.LandmarksFragment

/**
 * Manager class for Journey and Landmark fragments
 *
 * @property activity MainActivity that contains the SupportFragmentManager
 */
class FragmentManager(private val activity: AppCompatActivity) {

    private var journeyFragmentIsOpen = false
    private var landmarkFragmentIsOpen = false

    private val journeysFragment: JourneysFragment = JourneysFragment.newInstance()
    private var landmarksFragment: LandmarksFragment = LandmarksFragment.newInstance()

    /**
     * Opens the Journey Fragment
     *
     */
    private fun openJourneyFragment() {
        activity.supportFragmentManager.beginTransaction()
            .setTransition(TRANSIT_FRAGMENT_OPEN)
            .replace(R.id.journey_fragment_container, journeysFragment)
            .commitNow()
        journeyFragmentIsOpen = true
    }

    /**
     * Closes the Journey fragment
     *
     * @return True if the Journey Fragment has been closed, false otherwise
     */
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

    /**
     * Opens the Landmark Fragment
     *
     * @param journey Journey to load Landmarks from
     */
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

    /**
     * Closes the Landmark Fragment
     *
     * @return True if the Landmark Fragment has been closed, false otherwise
     */
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

    /**
     * Toggles the Journey Fragment
     *
     * @return True if Journey fragment has been opened, False otherwise
     */
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