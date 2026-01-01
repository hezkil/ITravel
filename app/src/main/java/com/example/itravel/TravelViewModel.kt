package com.example.itravel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class TravelViewModel(application: Application) : AndroidViewModel(application) {
    private val database = TravelDatabase.getDatabase(application)
    private val dao = database.travelDao()
    fun getEntriesForMonth(yearMonth: String): Flow<List<TravelEntry>> {
        return dao.getEntriesForMonth(yearMonth)
    }
    fun getEntry(date: String): Flow<TravelEntry?> {
        return dao.getEntryByDate(date)
    }
    val allEntries: Flow<List<TravelEntry>> = dao.getAllEntries()
    fun saveEntry(date: String, imagePath: String?, text: String) {
        viewModelScope.launch {
            if (imagePath != null) {
                try {
                    val uri = android.net.Uri.parse(imagePath)
                    val flags = android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                    getApplication<Application>().contentResolver
                        .takePersistableUriPermission(uri, flags)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            val entry = TravelEntry(date, imagePath, text)
            dao.insertEntry(entry)
        }
    }
    fun deleteEntry(date: String) {
        viewModelScope.launch {
            dao.deleteEntry(date)
        }
    }
}
