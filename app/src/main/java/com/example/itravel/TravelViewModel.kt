package com.example.itravel
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

// We use AndroidViewModel so we can pass the 'application' context to the Database builder
class TravelViewModel(application: Application) : AndroidViewModel(application) {

    // 1. Initialize the Database
    // We use the Singleton instance we created earlier in TravelDatabase.kt
    private val database = TravelDatabase.getDatabase(application)
    private val dao = database.travelDao()

    // 2. Read Actions (Return Flows so UI updates automatically)

    // Used by CalendarScreen to show thumbnails
    fun getEntriesForMonth(yearMonth: String): Flow<List<TravelEntry>> {
        return dao.getEntriesForMonth(yearMonth)
    }

    // Used by TravelPostScreen to load a specific day's data
    fun getEntry(date: String): Flow<TravelEntry?> {
        return dao.getEntryByDate(date)
    }

    // 3. Write Actions (Must be run in a Coroutine)

    fun saveEntry(date: String, imagePath: String?, text: String) {
        viewModelScope.launch {

            // 1. Try to take persistent permission for the image
            if (imagePath != null) {
                try {
                    val uri = android.net.Uri.parse(imagePath)
                    val flags = android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                    getApplication<Application>().contentResolver.takePersistableUriPermission(uri, flags)
                } catch (e: Exception) {
                    // If it fails (e.g. image is not from gallery), just ignore
                    e.printStackTrace()
                }
            }

            // 2. Save to DB
            val entry = TravelEntry(date, imagePath, text)
            dao.insertEntry(entry)
        }
    }

    // Optional: Add a delete function if you want to clear a day
    fun deleteEntry(date: String) {
        viewModelScope.launch {
            dao.deleteEntry(date)
        }
    }
}

