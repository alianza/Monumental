package com.example.monumental.helpers

import androidx.appcompat.app.AppCompatActivity
import com.example.monumental.R
import com.example.monumental.ui.journey.JourneyFragment

class FragmentHelper(private val context: AppCompatActivity) {

    var journeyFragmentIsOpen = false

    private val journeyFragment: JourneyFragment = JourneyFragment.newInstance()

    fun openJourneyFragment() {
        context.supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, journeyFragment)
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

    fun toggleJourneyFragment() {
        if (journeyFragmentIsOpen) {
            closeJourneyFragment()
        } else {
            openJourneyFragment()
        }
    }
}