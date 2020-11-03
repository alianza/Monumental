package com.example.monumental.model

import android.os.Parcelable
import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Entity
@Parcelize
data class Journey(
    @PrimaryKey(autoGenerate = true)
    @NonNull
    @ColumnInfo(name = "id") val id: Int?,
    @ColumnInfo(name = "name") var name: String,
    @ColumnInfo(name = "current") var current: Boolean = false
) : Parcelable