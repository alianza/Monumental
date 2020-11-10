package com.example.monumental.ui.main

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.monumental.common.GraphicOverlay
import com.example.monumental.common.VisionImageProcessor
import com.example.monumental.model.Landmark
import com.example.monumental.model.LandmarkResultList
import com.example.monumental.room.repository.JourneyRepository
import com.example.monumental.room.repository.LandmarkRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val landmarkRepository = LandmarkRepository(application.applicationContext)
    private val journeyRepository = JourneyRepository(application.applicationContext)

    private val mainScope = CoroutineScope(Dispatchers.Main)

    var activeJourney = journeyRepository.getActiveJourney()

    fun saveLandmark(landmark: Landmark) {
        mainScope.launch {
            landmarkRepository.insertLandmark(landmark)
        }
    }

    fun doDetectInBitmap(imageProcessor: VisionImageProcessor, bitmap: Bitmap, previewOverlay: GraphicOverlay, resultsList: MutableLiveData<LandmarkResultList>) {
        imageProcessor.process(bitmap, previewOverlay, resultsList)
    }
}