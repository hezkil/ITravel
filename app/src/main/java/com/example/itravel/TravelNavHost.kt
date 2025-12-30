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
import androidx.compose.runtime.LaunchedEffect
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
            // 1. CHANGE: Use 'var' and 'mutableStateOf' to make it changeable
            var currentMonth by remember { mutableStateOf(YearMonth.now()) }

            // 2. Re-calculate the query whenever 'currentMonth' changes
            val monthQuery = "${currentMonth.year}-${String.format("%02d", currentMonth.monthValue)}"

            // 3. Fetch data. usage of 'remember(monthQuery)' ensures we reload when month changes
            val entriesState by remember(monthQuery) {
                viewModel.getEntriesForMonth(monthQuery)
            }.collectAsState(initial = emptyList())

            CalendarScreen(
                currentMonth = currentMonth,
                entries = entriesState,
                onDateClick = { dateString ->
                    navController.navigate("post/$dateString")
                },
                // 4. NEW: Handle button clicks
                onPrevMonth = {
                    currentMonth = currentMonth.minusMonths(1)
                },
                onNextMonth = {
                    currentMonth = currentMonth.plusMonths(1)
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
    existingEntry: TravelEntry?,
    onSave: (TravelEntry) -> Unit,
    onBack: () -> Unit
) {
    // 1. Setup the state
    var text by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    // --- NEW CODE STARTS HERE ---
    // This watches 'existingEntry'. Whenever the database sends new data,
    // we update our text and image variables.
    LaunchedEffect(existingEntry) {
        if (existingEntry != null) {
            text = existingEntry.description
            // Only update image if we haven't picked a new one manually yet
            if (existingEntry.imagePath != null) {
                imageUri = Uri.parse(existingEntry.imagePath)
            }
        }
    }
    // --- NEW CODE ENDS HERE ---

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) imageUri = uri
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Entry for $selectedDate") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val entry = TravelEntry(
                            date = selectedDate,
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
        // ... The rest of your UI code (Column, Box, TextField) stays exactly the same ...
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // PHOTO AREA
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(Color.LightGray.copy(alpha = 0.3f))
                    .clickable {
                        imagePickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                if (imageUri != null) {
                    AsyncImage(
                        model = imageUri,
                        contentDescription = "Selected Photo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Add, null, tint = Color.Gray)
                        Text("Tap to add photo", color = Color.Gray)
                    }
                }
            }

            // TEXT AREA
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Write your story here...") },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                )
            )
        }
    }
}