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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton

@Composable
fun CalendarScreen(
    currentMonth: YearMonth,
    entries: List<TravelEntry>,
    onDateClick: (String) -> Unit,
    onNextMonth: () -> Unit,
    onPrevMonth: () -> Unit,
    isDarkMode: Boolean,
    onToggleTheme: () -> Unit
) {
    val daysList = remember(currentMonth) { getDaysInMonth(currentMonth) }
    val entryMap = remember(entries) { entries.associate { it.date to it.imagePath } }
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPrevMonth) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
            }
            Text(
                text = "${currentMonth.month.name} ${currentMonth.year}",
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = onToggleTheme) {
                Icon(
                    imageVector = if (isDarkMode)
                        Icons.Default.LightMode
                    else
                        Icons.Default.DarkMode,
                    contentDescription = "Toggle theme"
                )
            }
            IconButton(onClick = onNextMonth) {
                Icon(Icons.AutoMirrored.Filled.ArrowForward, null)
            }
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat").forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    color = Color.Gray
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(daysList) { date ->
                if (date != null) {
                    val dateKey = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
                    DayCell(
                        dayNumber = date.dayOfMonth,
                        imagePath = entryMap[dateKey],
                        onClick = { onDateClick(dateKey) }
                    )
                } else {
                    Box(modifier = Modifier.aspectRatio(0.8f))
                }
            }
        }
    }
}

@Composable
fun DayCell(
    dayNumber: Int,
    imagePath: String?,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .aspectRatio(0.8f)
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (imagePath == null) Color.LightGray.copy(alpha = 0.2f)
                else Color.Transparent
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (imagePath != null) {
            AsyncImage(
                model = imagePath,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.25f))
            )
        }
        Text(
            text = dayNumber.toString(),
            color = if (imagePath != null) Color.White else Color.Black,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

fun getDaysInMonth(yearMonth: YearMonth): List<LocalDate?> {
    val firstDayOfMonth = yearMonth.atDay(1)
    val daysInMonth = yearMonth.lengthOfMonth()
    val startDayOffset = firstDayOfMonth.dayOfWeek.value % 7
    val days = mutableListOf<LocalDate?>()
    repeat(startDayOffset) { days.add(null) }
    for (i in 1..daysInMonth) {
        days.add(yearMonth.atDay(i))
    }
    return days
}