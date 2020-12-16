package com.example.monumental.model.entity

import android.os.Parcelable
import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

/**
 * Journey entity
 *
 * @property id ID of Journey
 * @property name Name of Journey
 * @property current Status of current Journey
 */
@Entity
@Parcelize
data class Journey(
    @PrimaryKey(autoGenerate = true)
    @NonNull
    @ColumnInfo(name = "id") val id: Int?,
    @ColumnInfo(name = "name") var name: String,
    @ColumnInfo(name = "current") var current: Boolean = false
) : Parcelable
