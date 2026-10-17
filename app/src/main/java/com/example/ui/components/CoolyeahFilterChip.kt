package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.theme.TealPrimary

/**
 * Reusable filter chip providing uniform height, corner radius, padding,
 * typography, and touch target across all filter bars in the application.
 */
@Composable
fun CoolyeahFilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    selectedContainerColor: Color = TealPrimary,
    selectedContentColor: Color = Color.White,
    unselectedContainerColor: Color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
    unselectedContentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    leadingIcon: (@Composable () -> Unit)? = null
) {
    Surface(
        selected = selected,
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = if (selected) selectedContainerColor else unselectedContainerColor,
        contentColor = if (selected) selectedContentColor else unselectedContentColor,
        border = if (!selected) BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)) else null,
        shadowElevation = if (selected) 2.dp else 0.dp,
        modifier = modifier
            .height(36.dp)
            .defaultMinSize(minWidth = 56.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            androidx.compose.foundation.layout.Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (leadingIcon != null) {
                    leadingIcon()
                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(end = 6.dp))
                }
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }
    }
}
