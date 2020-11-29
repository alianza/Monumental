package com.example.monumental.viewModel.main

import android.app.Application
import android.content.ContentResolver
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.monumental.model.data.cloudlandmarkrecognition.CloudLandmarkRecognitionProcessor
import com.example.monumental.model.data.room.repository.JourneyRepository
import com.example.monumental.model.data.room.repository.LandmarkRepository
import com.example.monumental.model.entity.Landmark
import com.example.monumental.model.entity.LandmarkResultList
import com.example.monumental.view.common.GraphicOverlay
import com.example.monumental.view.common.helpers.BitmapHelper
import com.example.monumental.view.common.helpers.ImageHelper
import com.example.monumental.view.common.helpers.MediaFileHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val landmarkRepository = LandmarkRepository(application.applicationContext)
    private val journeyRepository = JourneyRepository(application.applicationContext)
    private val imageProcessor = CloudLandmarkRecognitionProcessor()
    private val mediaFileHelper = MediaFileHelper()
    private val bitmapHelper = BitmapHelper()

    private val mainScope = CoroutineScope(Dispatchers.Main)

    var activeJourney = journeyRepository.getActiveJourney()

    fun saveLandmark(landmark: Landmark) {
        mainScope.launch {
            landmarkRepository.insertLandmark(landmark)
        }
    }

    fun doDetectInBitmap(bitmap: Bitmap, previewOverlay: GraphicOverlay, resultsList: MutableLiveData<LandmarkResultList>) {
        imageProcessor.process(bitmap, previewOverlay, resultsList)
    }

    fun getOutputMediaFileUri(): Uri {
        return mediaFileHelper.getOutputMediaFileUri()
    }

    fun getOutputMediaFile(): File? {
        return mediaFileHelper.getOutputMediaFile()
    }

    fun getScaledBitmap(contentResolver: ContentResolver?, imageUri: Uri, imageHelper: ImageHelper): Bitmap? {
        return bitmapHelper.getScaledBitmap(contentResolver!!, imageUri, imageHelper)
    }
}
