package com.example.itravel
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TravelDao {

    // 1. Get a specific entry when the user taps a date
    @Query("SELECT * FROM travel_entries WHERE date = :date")
    fun getEntryByDate(date: String): Flow<TravelEntry?>

    // 2. Get all entries for a specific month (Perfect for your Calendar screen)
    // Example usage: getEntriesForMonth("2025-12%")
    @Query("SELECT * FROM travel_entries WHERE date LIKE :yearMonth || '%'")
    fun getEntriesForMonth(yearMonth: String): Flow<List<TravelEntry>>

    // 3. Save or Update
    // OnConflictStrategy.REPLACE means if an entry for "2025-12-30" exists, overwrite it.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: TravelEntry)

    // 4. Delete (Optional, but good to have)
    @Query("DELETE FROM travel_entries WHERE date = :date")
    suspend fun deleteEntry(date: String)

    @Query("SELECT * FROM travel_entries ORDER BY date DESC")
    fun getAllEntries(): Flow<List<TravelEntry>>

}