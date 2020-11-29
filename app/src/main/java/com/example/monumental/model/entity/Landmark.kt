package com.example.monumental.model.entity

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.*

@Entity(foreignKeys = [ForeignKey(entity = Journey::class,
    parentColumns = arrayOf("id"),
    childColumns = arrayOf("journey_id"),
    onDelete = ForeignKey.NO_ACTION)]
)
data class Landmark(
    @PrimaryKey(autoGenerate = true)
    @NonNull
    @ColumnInfo(name = "id") val id: Int?,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "img_uri") val img_uri: String,
    @ColumnInfo(name = "date") val date: Date?,
    @ColumnInfo(name = "journey_id") val journey_id: Int?
)