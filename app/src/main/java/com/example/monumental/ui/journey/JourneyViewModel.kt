package com.example.monumental.ui.journey

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.monumental.room.repository.JourneyRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class JourneyViewModel(application: Application) : AndroidViewModel(application) {

    private val journeyRepository = JourneyRepository(application.applicationContext)

    var journeys = journeyRepository.getJourneys()

    private val mainScope = CoroutineScope(Dispatchers.Main)


}