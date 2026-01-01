package com.example.itravel

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TravelDao {
    @Query("SELECT * FROM travel_entries WHERE date = :date")
    fun getEntryByDate(date: String): Flow<TravelEntry?>
    @Query("SELECT * FROM travel_entries WHERE date LIKE :yearMonth || '%'")
    fun getEntriesForMonth(yearMonth: String): Flow<List<TravelEntry>>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: TravelEntry)
    @Query("DELETE FROM travel_entries WHERE date = :date")
    suspend fun deleteEntry(date: String)
    @Query("SELECT * FROM travel_entries ORDER BY date DESC")
    fun getAllEntries(): Flow<List<TravelEntry>>
}