package com.example.ui.components

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.navigation.NavigationItem
import com.example.ui.theme.TealLight
import com.example.ui.theme.TealPrimary

@Composable
fun CoolyeahBottomBar(
    currentRoute: String,
    onNavigateToRoute: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        tonalElevation = 6.dp,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier.testTag("coolyeah_bottom_bar")
    ) {
        NavigationItem.items.forEach { item ->
            val isSelected = currentRoute == item.route
            NavigationBarItem(
                selected = isSelected,
                onClick = {
                    if (currentRoute != item.route) {
                        onNavigateToRoute(item.route)
                    }
                },
                icon = {
                    Icon(
                        imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.title
                    )
                },
                alwaysShowLabel = true,
                label = {
                    Text(
                        text = item.title,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        style = MaterialTheme.typography.labelMedium,
                        maxLines = 1,
                        softWrap = false
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = TealPrimary,
                    selectedTextColor = TealPrimary,
                    indicatorColor = TealLight,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.testTag("nav_${item.route}")
            )
        }
    }
}
