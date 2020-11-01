package com.example.monumental.model

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.*

@Entity(foreignKeys = [ForeignKey(entity = Journey::class,
    parentColumns = arrayOf("name"),
    childColumns = arrayOf("journey_name"),
    onDelete = ForeignKey.NO_ACTION)]
)
data class Landmark(
    @PrimaryKey(autoGenerate = false)
    @NonNull
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "img_uri") val img_uri: String?,
    @ColumnInfo(name = "date") val date: Date?,
    @ColumnInfo(name = "journey_name") val journey_name: String?
)