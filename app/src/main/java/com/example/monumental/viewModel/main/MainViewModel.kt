package com.example.monumental.viewModel.main

import android.app.Application
import android.content.ContentResolver
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.monumental.model.data.cloudLandmarkRecognition.CloudLandmarkRecognitionProcessor
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

    /**
     * Inserts new Journey
     *
     * @return Long ID of inserted journey
     */
    fun createLandmark(landmark: Landmark) {
        mainScope.launch {
            landmarkRepository.insertLandmark(landmark)
        }
    }

    /**
     * Detect Landmarks in Bitmap image
     *
     * @param bitmap image to process
     * @param graphicOverlay overlay for displaying landmark boundaries
     * @param resultsList list to collect and return results
     */
    fun doDetectInBitmap(bitmap: Bitmap, graphicOverlay: GraphicOverlay, resultsList: MutableLiveData<LandmarkResultList>) {
        imageProcessor.process(bitmap, graphicOverlay, resultsList)
    }

    /**
     * Creates a File for saving an image
     *
     * @return File
     */
    fun getOutputMediaFileUri(): Uri {
        return mediaFileHelper.getOutputMediaFileUri()
    }

    /**
     * Creates a file Uri for saving an image
     *
     * @return Uri from File
     */
    fun getOutputMediaFile(): File? {
        return mediaFileHelper.getOutputMediaFile()
    }

    /**
     * Gets a scaled bitmap according to the ImageHelper dimensions
     *
     * @param contentResolver
     * @param imageUri
     * @param imageHelper
     * @return Bitmap
     */
    fun getScaledBitmap(contentResolver: ContentResolver?, imageUri: Uri, imageHelper: ImageHelper): Bitmap? {
        return bitmapHelper.getScaledBitmap(contentResolver!!, imageUri, imageHelper)
    }
}
