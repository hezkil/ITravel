package com.example.itravel
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

// --- 1. MAIN SCREEN COMPOSABLE ---
@Composable
fun CalendarScreen(
    currentMonth: YearMonth,
    entries: List<TravelEntry>, // Your Room Entity list
    onDateClick: (String) -> Unit
) {
    // 1. Calculate the days to display (including empty slots for padding)
    val daysList = remember(currentMonth) { getDaysInMonth(currentMonth) }

    // 2. Convert List to Map for fast lookup.
    // Key: "2025-12-30", Value: "file://path/to/image.jpg"
    val entryMap = remember(entries) {
        entries.associate { it.date to it.imagePath }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // --- Header: Month Name ---
        Text(
            text = "${currentMonth.month.name} ${currentMonth.year}",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // --- Header: Day Names (Sun, Mon, Tue...) ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val daysOfWeek = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
            daysOfWeek.forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // --- Grid: The Calendar Days ---
        LazyVerticalGrid(
            columns = GridCells.Fixed(7), // 7 days in a week
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(daysList) { date ->
                if (date != null) {
                    // Create the key string "yyyy-MM-dd" to check the database
                    val dateKey = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
                    val imagePath = entryMap[dateKey]

                    DayCell(
                        dayNumber = date.dayOfMonth,
                        imagePath = imagePath,
                        onClick = { onDateClick(dateKey) }
                    )
                } else {
                    // Render an empty invisible box for padding at the start of the month
                    Box(modifier = Modifier.aspectRatio(0.8f))
                }
            }
        }
    }
}

// --- 2. SUB-COMPONENT: SINGLE DAY CELL ---
@Composable
fun DayCell(
    dayNumber: Int,
    imagePath: String?, // Null if no photo saved for this day
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .aspectRatio(0.8f) // 0.8f makes it slightly taller (portrait shape)
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (imagePath == null) Color.LightGray.copy(alpha = 0.2f)
                else Color.Transparent
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        // Layer 1: The Image (if exists)
        if (imagePath != null) {
            AsyncImage(
                model = imagePath,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Layer 2: Semi-transparent overlay so the number is readable
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.25f))
            )
        }

        // Layer 3: The Date Number
        Text(
            text = dayNumber.toString(),
            color = if (imagePath != null) Color.White else Color.Black,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

// --- 3. HELPER FUNCTION ---
/**
 * Returns a list representing the grid cells.
 * Null entries are "padding" for days before the 1st of the month.
 * Example: If Dec 1st is Wednesday, the list starts with [null, null, null, Dec1, Dec2...]
 */
fun getDaysInMonth(yearMonth: YearMonth): List<LocalDate?> {
    val firstDayOfMonth = yearMonth.atDay(1)
    val daysInMonth = yearMonth.lengthOfMonth()

    // Calculate padding.
    // java.time uses 1=Mon ... 7=Sun.
    // We want 0=Sun, 1=Mon ...
    // So "Mon(1) % 7 = 1" (1 empty slot: Sunday)
    // "Sun(7) % 7 = 0" (0 empty slots: Starts on Sunday)
    val startDayOffset = firstDayOfMonth.dayOfWeek.value % 7

    val days = mutableListOf<LocalDate?>()

    // Add nulls for empty slots at start
    repeat(startDayOffset) { days.add(null) }

    // Add actual dates
    for (i in 1..daysInMonth) {
        days.add(yearMonth.atDay(i))
    }

    return days
}