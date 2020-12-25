package com.example.monumental.model.entity

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.*

/**
 * Landmark Entity
 *
 * @property id ID of Landmark
 * @property name Name of Landmark
 * @property imgUri Uri for image of Landmark
 * @property date Date of landmark creation
 * @property journeyId ID of associated Journey
 */
@Entity(foreignKeys = [ForeignKey(
    entity = Journey::class,
    parentColumns = arrayOf("id"),
    childColumns = arrayOf("journey_id"),
    onDelete = ForeignKey.CASCADE)]
)
data class Landmark(
    @PrimaryKey(autoGenerate = true)
    @NonNull
    @ColumnInfo(name = "id") val id: Int?,
    @ColumnInfo(name = "name") var name: String,
    @ColumnInfo(name = "img_uri") val imgUri: String,
    @ColumnInfo(name = "date") val date: Date?,
    @ColumnInfo(name = "journey_id",  index = true) val journeyId: Int?
)