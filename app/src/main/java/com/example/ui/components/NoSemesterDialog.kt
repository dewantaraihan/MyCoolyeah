package com.example.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import com.example.ui.theme.TealPrimary

/**
 * Dialog informatif ketika pengguna mencoba menambahkan mata kuliah
 * namun belum ada semester yang terdaftar di database.
 */
@Composable
fun NoSemesterDialog(
    onAddSemester: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Semester Belum Ditambahkan",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
        },
        text = {
            Text(
                text = "Kamu belum memiliki semester. Tambahkan semester terlebih dahulu sebelum menambahkan mata kuliah.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        confirmButton = {
            Button(
                onClick = onAddSemester,
                colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                modifier = Modifier.testTag("dialog_btn_tambah_semester")
            ) {
                Text("Tambah Semester")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("dialog_btn_nanti")
            ) {
                Text("Nanti")
            }
        },
        modifier = modifier.testTag("dialog_no_semester")
    )
}
