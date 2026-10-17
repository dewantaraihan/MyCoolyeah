package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.SemesterEntity
import com.example.ui.theme.AmberSecondary
import com.example.ui.theme.TealLight
import com.example.ui.theme.TealPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoolyeahTopBar(
    semesters: List<SemesterEntity>,
    selectedSemesterId: Long?,
    onSemesterSelected: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var dropdownExpanded by remember { mutableStateOf(false) }

    val currentSelectedSemester = semesters.find { it.id == selectedSemesterId }
        ?: semesters.find { it.isActive }
        ?: semesters.firstOrNull()

    TopAppBar(
        modifier = modifier.testTag("coolyeah_top_bar"),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(TealLight),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.School,
                        contentDescription = "MyCoolyeah Logo",
                        tint = TealPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "My",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = TealPrimary
                        )
                    )
                    Text(
                        text = "Coolyeah",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            color = AmberSecondary
                        )
                    )
                }
            }
        },
        actions = {
            if (semesters.isNotEmpty() && currentSelectedSemester != null) {
                Box(modifier = Modifier.padding(end = 12.dp)) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        tonalElevation = 2.dp,
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { dropdownExpanded = true }
                            .testTag("semester_filter_dropdown")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = currentSelectedSemester.namaSemester,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                modifier = Modifier.widthIn(max = 110.dp)
                            )
                            if (currentSelectedSemester.isActive) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(TealPrimary)
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = "Aktif",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        ),
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.Filled.ArrowDropDown,
                                contentDescription = "Pilih Semester",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false },
                        modifier = Modifier.testTag("semester_dropdown_menu")
                    ) {
                        semesters.forEach { semester ->
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = semester.namaSemester,
                                            fontWeight = if (semester.id == currentSelectedSemester.id) FontWeight.Bold else FontWeight.Normal,
                                            color = if (semester.id == currentSelectedSemester.id) TealPrimary else MaterialTheme.colorScheme.onSurface
                                        )
                                        if (semester.isActive) {
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(TealLight)
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = "Semester Aktif",
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        color = TealPrimary,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                )
                                            }
                                        }
                                    }
                                },
                                onClick = {
                                    dropdownExpanded = false
                                    onSemesterSelected(semester.id)
                                }
                            )
                        }
                    }
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    )
}
