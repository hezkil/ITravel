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
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
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
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import java.time.YearMonth

@Composable
fun TravelNavHost(
    modifier: Modifier = Modifier,
    viewModel: TravelViewModel = viewModel(),
    isDarkMode: Boolean,
    onToggleTheme: () -> Unit
) {
    val navController = rememberNavController()
    val items = listOf(BottomNavItem.Home, BottomNavItem.Gallery)
    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination: NavDestination? = navBackStackEntry?.destination
                items.forEach { item ->
                    val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(item.route) {
                                launchSingleTop = true
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                restoreState = true
                            }
                        },
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "calendar",
            modifier = modifier.padding(innerPadding)
        ) {
            composable("calendar") {
                var currentMonth by remember { mutableStateOf(YearMonth.now()) }
                val monthQuery =
                    "${currentMonth.year}-${String.format("%02d", currentMonth.monthValue)}"
                val entriesState by remember(monthQuery) {
                    viewModel.getEntriesForMonth(monthQuery)
                }.collectAsState(initial = emptyList())
                CalendarScreen(
                    currentMonth = currentMonth,
                    entries = entriesState,
                    onDateClick = { navController.navigate("post/$it") },
                    onPrevMonth = { currentMonth = currentMonth.minusMonths(1) },
                    onNextMonth = { currentMonth = currentMonth.plusMonths(1) },
                    isDarkMode = isDarkMode,
                    onToggleTheme = onToggleTheme
                )
            }
            composable("gallery") {
                val allEntries by viewModel.allEntries.collectAsState(initial = emptyList())
                GalleryScreen(
                    entries = allEntries,
                    onEntryClick = { navController.navigate("post/$it") },
                    isDarkMode = isDarkMode,
                    onToggleTheme = onToggleTheme
                )
            }
            composable(
                route = "post/{date}",
                arguments = listOf(navArgument("date") { type = NavType.StringType })
            ) { backStackEntry ->
                val dateStr = backStackEntry.arguments?.getString("date") ?: return@composable
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
                        navController.popBackStack()
                    },
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }
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
    var text by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    LaunchedEffect(existingEntry) {
        if (existingEntry != null) {
            text = existingEntry.description
            if (existingEntry.imagePath != null) {
                imageUri = Uri.parse(existingEntry.imagePath)
            }
        }
    }
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
                    } ) {
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
