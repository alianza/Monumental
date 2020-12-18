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

    /**
     * Inserts new Landmark
     *
     * @return Long ID of inserted journey
     */
    fun createLandmark(landmark: Landmark) {
        mainScope.launch {
            landmarkRepository.insertLandmark(landmark)
        }
    }

    /**
     * Gets all Landmarks of Journey
     *
     * @param journeyId ID of Landmark to get Landmarks of
     * @return List of Landmarks
     */
    fun getLandmarksByJourney(journeyId: Int): LiveData<List<Landmark>?> {
        return landmarkRepository.getLandmarksByJourney(journeyId)
    }

    /**
     * Sets the active Journey, unset all other journeys
     *
     * @param journey Journey to set to active
     */
    fun setActiveJourney(journey: Journey) {
        mainScope.launch {
            journeyRepository.setActiveJourney(journey)
        }
    }

    /**
     * Gets a Bitmap from the device storage
     *
     * @param contentResolver ContentResolver class provides applications access to the content model
     * @param imageUri Uri image to retrieve
     * @return Bitmap that's retrieved
     */
    fun getBitmap(contentResolver: ContentResolver?, imageUri: Uri): Bitmap? {
        return bitmapHelper.getBitmap(contentResolver!!, imageUri)
    }

    /**
     * Creates a File for saving an image
     *
     * @return File
     */
    fun getOutputMediaFile(): File? {
        return mediaFileHelper.getOutputMediaFile()
    }

    /**
     * Creates a file Uri for saving an image
     *
     * @return Uri from File
     */
    fun getOutputMediaFileUri(): Uri {
        return mediaFileHelper.getOutputMediaFileUri()
    }

    /**
     * Removes a Landmark
     *
     * @param landmark Landmark to remove
     */
    fun deleteLandmark(landmark: Landmark) {
        mainScope.launch {
            landmarkRepository.deleteLandmark(landmark)
        }
    }
}