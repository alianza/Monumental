package com.example.monumental.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.monumental.model.Journey
import com.example.monumental.model.Landmark
import com.example.monumental.room.dao.JourneyDao
import com.example.monumental.room.dao.LandmarkDao
import com.example.monumental.room.typeConverters.DateConverter

@Database(entities = [Journey::class, Landmark::class], version = 1, exportSchema = false)
@TypeConverters(DateConverter::class)
abstract class MonumentalRoomDatabase : RoomDatabase() {

    abstract fun journeyDao(): JourneyDao
    abstract fun landmarkDao(): LandmarkDao

    companion object {
        private const val DATABASE_NAME = "MONUMENTAL_DATABASE"

        @Volatile
        private var INSTANCE: MonumentalRoomDatabase? = null

        fun getDatabase(context: Context): MonumentalRoomDatabase? {
            if (INSTANCE == null) {
                synchronized(MonumentalRoomDatabase::class.java) {
                    if (INSTANCE == null) {
                        INSTANCE = Room.databaseBuilder(
                            context.applicationContext,
                            MonumentalRoomDatabase::class.java, DATABASE_NAME
                        )
                            .fallbackToDestructiveMigration()
                            .build()
                    }
                }
            }
            return INSTANCE
        }
    }
}
