package com.example.monumental.viewModel.landmark

import android.app.Application
import android.content.ContentResolver
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.example.monumental.model.data.room.repository.JourneyRepository
import com.example.monumental.model.data.room.repository.LandmarkRepository
import com.example.monumental.model.entity.Journey
import com.example.monumental.model.entity.Landmark
import com.example.monumental.view.common.helpers.BitmapHelper
import com.example.monumental.view.common.helpers.MediaFileHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class LandmarksViewModel(application: Application) : AndroidViewModel(application) {
    private val landmarkRepository = LandmarkRepository(application.applicationContext)
    private val journeyRepository = JourneyRepository(application.applicationContext)
    private val bitmapHelper = BitmapHelper()
    private val mediaFileHelper = MediaFileHelper()

    private val mainScope = CoroutineScope(Dispatchers.Main)

    fun deleteLandmark(landmark: Landmark) {
        mainScope.launch {
            landmarkRepository.deleteLandmark(landmark)
        }
    }

    fun saveLandmark(landmark: Landmark) {
        mainScope.launch {
            landmarkRepository.insertLandmark(landmark)
        }
    }

    fun getLandmarksByJourney(journeyId: Int): LiveData<List<Landmark>?> {
        return landmarkRepository.getLandmarksByJourney(journeyId)
    }

    fun setActiveJourney(journey: Journey) {
        mainScope.launch {
            journeyRepository.setActiveJourney(journey)
        }
    }

    fun getBitmap(contentResolver: ContentResolver?, imageUri: Uri): Bitmap? {
        return bitmapHelper.getBitmap(contentResolver!!, imageUri)
    }

    fun getOutputMediaFile(): File? {
        return mediaFileHelper.getOutputMediaFile()
    }

    fun getOutputMediaFileUri(): Uri {
        return mediaFileHelper.getOutputMediaFileUri()
    }
}