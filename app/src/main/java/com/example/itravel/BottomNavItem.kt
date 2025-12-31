package com.example.itravel

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    data object Home : BottomNavItem("calendar", "Home", Icons.Filled.Home)
    data object Gallery : BottomNavItem("gallery", "Gallery", Icons.Filled.List)
}
