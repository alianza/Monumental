package com.example.monumental.ui.landmark

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.monumental.model.Landmark
import com.example.monumental.room.repository.LandmarkRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class LandmarkViewModel(application: Application) : AndroidViewModel(application) {
    private val landmarkRepository = LandmarkRepository(application.applicationContext)

    var landmarks = landmarkRepository.getLandmarks()

    private val mainScope = CoroutineScope(Dispatchers.Main)

    fun deleteLandmark(landmark: Landmark) {
        mainScope.launch {
            landmarkRepository.deleteLandmark(landmark)
        }
    }

    fun createLandmarkTest(journeyId: Int) {
        mainScope.launch {
            landmarkRepository.insertLandmark(Landmark(
                null,
                "Eiffel Tower",
                "file:///storage/emulated/0/Pictures/Monumental/IMG_Mon%2C%202%20Nov%202020%2021%3A54%3A59%20%2B0100.jpg",
                Date(),
                journeyId
            ))
        }
    }
}