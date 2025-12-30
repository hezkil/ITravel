package com.example.itravel
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import coil.compose.AsyncImage
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import java.time.YearMonth

@Composable
fun TravelNavHost(
    modifier: Modifier = Modifier,
    // We create the ViewModel here so it lives as long as the navigation exists
    viewModel: TravelViewModel = viewModel()
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "calendar", // App starts here
        modifier = modifier
    ) {
        // --- SCREEN 1: CALENDAR GRID ---
        composable("calendar") {
            val currentMonth = remember { YearMonth.now() }

            // Format month for SQL query (e.g., "2025-12%")
            // String.format ensures we get "01", "02" instead of "1", "2"
            val monthQuery = "${currentMonth.year}-${String.format("%02d", currentMonth.monthValue)}"

            // Observe the database for this month's entries
            val entriesState by viewModel.getEntriesForMonth(monthQuery)
                .collectAsState(initial = emptyList())

            CalendarScreen(
                currentMonth = currentMonth,
                entries = entriesState,
                onDateClick = { dateString ->
                    // When a date is clicked, go to the Post screen
                    navController.navigate("post/$dateString")
                }
            )
        }

        // --- SCREEN 2: POSTING SCREEN ---
        composable(
            route = "post/{date}",
            arguments = listOf(navArgument("date") { type = NavType.StringType })
        ) { backStackEntry ->
            // 1. Get the date passed from the previous screen
            val dateStr = backStackEntry.arguments?.getString("date") ?: return@composable

            // 2. Observe the specific entry for this single date
            val entryState by viewModel.getEntry(dateStr).collectAsState(initial = null)

            TravelPostScreen(
                selectedDate = dateStr,
                existingEntry = entryState,
                onSave = { newEntry ->
                    viewModel.saveEntry(
                        newEntry.date,
                        newEntry.imagePath,
                        newEntry.description
                    )
                    // Go back to calendar after saving
                    navController.popBackStack()
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TravelPostScreen(
    selectedDate: String,
    existingEntry: TravelEntry?, // Can be null if no entry exists yet
    onSave: (TravelEntry) -> Unit,
    onBack: () -> Unit
) {
    // 1. State: Hold the text and the image path
    // If we have an existing entry, pre-fill the data.
    var text by remember { mutableStateOf(existingEntry?.description ?: "") }
    var imageUri by remember { mutableStateOf<Uri?>(existingEntry?.imagePath?.toUri()) }

    // 2. Image Picker: Logic to open the phone's gallery
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        // When user picks an image, update our state
        if (uri != null) {
            imageUri = uri
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Entry for $selectedDate") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        // Use AutoMirrored icon for RTL support (good practice)
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    // Save Button
                    IconButton(onClick = {
                        val entry = TravelEntry(
                            date = selectedDate,
                            // Convert Uri back to String for the Database
                            imagePath = imageUri?.toString(),
                            description = text
                        )
                        onSave(entry)
                    }) {
                        Icon(Icons.Default.Check, "Save")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // --- UPPER SCREEN: PHOTO AREA (50% Height) ---
            Box(
                modifier = Modifier
                    .weight(1f) // Takes up upper 50%
                    .fillMaxWidth()
                    .background(Color.LightGray.copy(alpha = 0.3f))
                    .clickable {
                        // Launch the photo picker
                        imagePickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                if (imageUri != null) {
                    // Show the selected image
                    AsyncImage(
                        model = imageUri,
                        contentDescription = "Selected Travel Photo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    // Show placeholder if no image
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = Color.Gray
                        )
                        Text("Tap to add photo", color = Color.Gray)
                    }
                }
            }

            // --- LOWER SCREEN: TEXT AREA (50% Height) ---
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier
                    .weight(1f) // Takes up lower 50%
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("How was your day? Write your story here...") },
                // Make the text field look clean (transparent background)
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                maxLines = 20
            )
        }
    }
}