package com.example.monumental.model

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Journey(
    @PrimaryKey(autoGenerate = false)
    @NonNull
    @ColumnInfo(name = "name") val name: String
//    @ColumnInfo(name = "landmarks") val landmarks: List<Landmark>?
)