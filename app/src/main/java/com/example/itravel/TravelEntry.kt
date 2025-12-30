package com.example.itravel
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "travel_entries")
data class TravelEntry(
    // We use String for dates (e.g., "2025-12-30") because it's easy to query and sort
    @PrimaryKey val date: String,
    val imagePath: String?,
    val description: String
)