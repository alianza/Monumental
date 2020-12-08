package com.example.monumental

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.monumental.model.data.room.MonumentalRoomDatabase
import com.example.monumental.model.entity.Journey
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.equalTo
import org.junit.After
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import java.util.concurrent.Executors


/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
open class JourneyDaoUnitTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var db: MonumentalRoomDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context, MonumentalRoomDatabase::class.java
        ).setTransactionExecutor(Executors.newSingleThreadExecutor())
            .build()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    @Throws(Exception::class)
    fun writeJourneyAndRetrieveActive() {
        val journey = Journey(null, "Test Journey", true)

        runBlocking { db.journeyDao().insertJourney(journey) }

        val activeJourney = db.journeyDao().getActiveJourney().getOrAwaitValue()

        assertThat(activeJourney?.name, equalTo(journey.name))
    }

    @Test
    @Throws(Exception::class)
    fun writeJourneyAndRetrieveByName() {
        val journey = Journey(null, "Test Journey", true)

        runBlocking { db.journeyDao().insertJourney(journey) }

        val retrievedJourney = db.journeyDao().getJourney("Test Journey").getOrAwaitValue()

        assertThat(retrievedJourney?.name, equalTo(journey.name))
    }

    @Test
    @Throws(Exception::class)
    fun writeJourneyAndDelete() {
        val journey = Journey(null, "Test Journey", true)

        runBlocking { db.journeyDao().insertJourney(journey) }

        runBlocking { db.journeyDao().deleteJourney(journey) }.runCatching {

            val retrievedJourney = db.journeyDao().getJourney("Test Journey").getOrAwaitValue()

            assertThat(retrievedJourney, equalTo(null))
        }
    }

    @Test
    @Throws(Exception::class)
    fun writeJourneyAndUpdate() {
        val journey = Journey(null, "Test Journey", true)
        val nameToChangeTo = "Changed name"

        runBlocking { db.journeyDao().insertJourney(journey) }

        journey.name = nameToChangeTo

        runBlocking { db.journeyDao().updateJourney(journey) }.runCatching {

            val retrievedJourney = db.journeyDao().getJourney(nameToChangeTo).getOrAwaitValue()

            assertThat(retrievedJourney, equalTo(journey))
        }
    }

    @Test
    @Throws(Exception::class)
    fun writeJourneysAndSwitchActive() {
        val journey1 = Journey(1, "Test Journey 1", true)
        val journey2 = Journey(2, "Test Journey 2", false)

        runBlocking {
            db.journeyDao().insertJourney(journey1)
            db.journeyDao().insertJourney(journey2)
        }

        runBlocking {
            db.journeyDao().setActiveJourney(journey2.id!!)
        }

        val retrievedJourney = db.journeyDao().getActiveJourney().getOrAwaitValue()

        assertThat(retrievedJourney?.current, equalTo(true))
    }
}