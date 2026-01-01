package com.example.itravel

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "travel_entries")
data class TravelEntry(
    @PrimaryKey val date: String,
    val imagePath: String?,
    val description: String
)