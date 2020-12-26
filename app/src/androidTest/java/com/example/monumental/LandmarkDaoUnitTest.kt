package com.example.monumental

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.monumental.model.data.room.MonumentalRoomDatabase
import com.example.monumental.model.entity.Journey
import com.example.monumental.model.entity.Landmark
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.equalTo
import org.junit.After
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import java.util.*
import java.util.concurrent.Executors

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class LandmarkDaoUnitTest {
    private lateinit var db: MonumentalRoomDatabase

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, MonumentalRoomDatabase::class.java)
            .setTransactionExecutor(Executors.newSingleThreadExecutor())
            .build()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    @Throws(Exception::class)
    fun writeLandmarkAndRetrieve() {
        val journey = Journey(1, "Test Journey", true)
        val date = Date()
        val landmark = Landmark(
            null,
            "Test Landmark",
            "/storage/emulated/0/Pictures/Monumental/IMG_Sun, 6 Dec 2020 04:54:24 +0100.jpg",
            date,
            journey.id
        )

        runBlocking {
            db.journeyDao().insertJourney(journey)
            db.landmarkDao().insertLandmark(landmark)
        }

        val retrievedLandmark = db.landmarkDao().getLandmark(landmark.name).getOrAwaitValue()

        assertThat(retrievedLandmark?.name, equalTo(landmark.name))
    }

    @Test
    @Throws(Exception::class)
    fun writeLandmarkAndUpdate() {
        val journey = Journey(1, "Test Journey", true)
        val date = Date()
        val landmark = Landmark(
            null,
            "Test Landmark",
            "/storage/emulated/0/Pictures/Monumental/IMG_Sun, 6 Dec 2020 04:54:24 +0100.jpg",
            date,
            journey.id
        )
        val nameToChangeTo = "Changed name"

        runBlocking {
            db.journeyDao().insertJourney(journey)
            db.landmarkDao().insertLandmark(landmark)
        }

        landmark.name = nameToChangeTo

        runBlocking { db.landmarkDao().updateLandmark(landmark) }.runCatching {

            val retrievedLandmark = db.landmarkDao().getLandmark(landmark.name).getOrAwaitValue()

            assertThat(retrievedLandmark?.name, equalTo(landmark.name))
        }
    }

    @Test
    @Throws(Exception::class)
    fun writeLandmarkAndDelete() {
        val journey = Journey(1, "Test Journey", true)
        val date = Date()
        val landmark = Landmark(
            null,
            "Test Landmark",
            "/storage/emulated/0/Pictures/Monumental/IMG_Sun, 6 Dec 2020 04:54:24 +0100.jpg",
            date,
            journey.id
        )

        runBlocking {
            db.journeyDao().insertJourney(journey)
            db.landmarkDao().insertLandmark(landmark)
        }

        runBlocking { db.landmarkDao().deleteLandmark(landmark) }.runCatching {

            val retrievedLandmark = db.landmarkDao().getLandmark(landmark.name).getOrAwaitValue()

            assertThat(retrievedLandmark, equalTo(null))
        }
    }

    @Test
    @Throws(Exception::class)
    fun writeLandmarksAndGetByJourney() {
        val journey = Journey(1, "Test Journey", true)
        val date = Date()
        val landmark1 = Landmark(
            1,
            "Test Landmark 1",
            "/storage/emulated/0/Pictures/Monumental/IMG_Sun, 6 Dec 2020 04:54:24 +0100.jpg",
            date,
            journey.id
        )
        val landmark2 = Landmark(
            2,
            "Test Landmark 2",
            "/storage/emulated/0/Pictures/Monumental/IMG_Sun, 6 Dec 2020 04:54:24 +0100.jpg",
            date,
            journey.id
        )

        runBlocking {
            db.journeyDao().insertJourney(journey)
            db.landmarkDao().insertLandmark(landmark1)
            db.landmarkDao().insertLandmark(landmark2)
        }

        val retrievedLandmarks = db.landmarkDao().getLandmarksByJourney(journey.id!!).getOrAwaitValue()

        assertThat(retrievedLandmarks, equalTo(listOf(landmark1, landmark2)))
    }
}