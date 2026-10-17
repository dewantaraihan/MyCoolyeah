package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.ui.graphics.vector.ImageVector

sealed class NavigationItem(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    data object Home : NavigationItem(
        route = "home",
        title = "Home",
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home
    )

    data object Jadwal : NavigationItem(
        route = "jadwal",
        title = "Jadwal",
        selectedIcon = Icons.Filled.DateRange,
        unselectedIcon = Icons.Outlined.DateRange
    )

    data object Tugas : NavigationItem(
        route = "tugas",
        title = "Tugas",
        selectedIcon = Icons.AutoMirrored.Filled.Assignment,
        unselectedIcon = Icons.AutoMirrored.Outlined.Assignment
    )

    data object Profil : NavigationItem(
        route = "profil",
        title = "Profil",
        selectedIcon = Icons.Filled.Person,
        unselectedIcon = Icons.Outlined.Person
    )

    companion object {
        val items: List<NavigationItem>
            get() = listOf(Home, Jadwal, Tugas, Profil)
    }
}
