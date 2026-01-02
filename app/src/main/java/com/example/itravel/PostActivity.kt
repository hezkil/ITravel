package com.example.itravel

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.itravel.ui.theme.ITravelTheme

class PostActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val date = intent.getStringExtra("date") ?: return
        setContent {
            ITravelTheme(darkTheme = false) {
                val viewModel: TravelViewModel = viewModel()
                val entryState = viewModel
                    .getEntry(date)
                    .collectAsState(initial = null)
                    .value
                TravelPostScreen(
                    selectedDate = date,
                    existingEntry = entryState,
                    onSave = {
                        viewModel.saveEntry(it.date, it.imagePath, it.description)
                        finish()
                    },
                    onDelete = {
                        viewModel.deleteEntry(date)
                        finish()
                    },
                    onBack = { finish() }
                )
            }
        }
    }
}
