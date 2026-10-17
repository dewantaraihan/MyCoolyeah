package com.example.ui.components

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.JadwalKuliahEntity
import com.example.data.local.entity.MataKuliahEntity
import com.example.data.local.entity.MateriEntity
import com.example.data.local.entity.SemesterEntity
import com.example.data.local.entity.TugasEntity
import com.example.data.local.entity.UjianEntity
import com.example.data.model.KategoriUjian
import com.example.data.model.PrioritasTugas
import com.example.data.model.TipeMateri
import com.example.ui.theme.AmberSecondary
import com.example.ui.theme.TealPrimary
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

// 1. Dialog Tambah / Edit Semester
@Composable
fun SemesterFormDialog(
    initialSemester: SemesterEntity? = null,
    onSave: (nama: String, tahunAjaran: String, isActive: Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var namaSemester by remember { mutableStateOf(initialSemester?.namaSemester ?: "") }
    var tahunAjaran by remember { mutableStateOf(initialSemester?.tahunAjaran ?: "") }
    var isActive by remember { mutableStateOf(initialSemester?.isActive ?: false) }
    var errorText by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initialSemester == null) "Tambah Semester" else "Edit Semester",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(
                    value = namaSemester,
                    onValueChange = {
                        namaSemester = it
                        errorText = null
                    },
                    label = { Text("Nama Semester (misal: Semester 5)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_semester_nama")
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = tahunAjaran,
                    onValueChange = {
                        tahunAjaran = it
                        errorText = null
                    },
                    label = { Text("Tahun Ajaran (misal: 2026/2027 Ganjil)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_semester_tahun")
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Jadikan Semester Aktif",
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Akan otomatis menjadi filter default aplikasi",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = isActive,
                        onCheckedChange = { isActive = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = TealPrimary)
                    )
                }

                if (errorText != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorText ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (namaSemester.isBlank()) {
                        errorText = "Nama semester tidak boleh kosong!"
                        return@Button
                    }
                    onSave(namaSemester.trim(), tahunAjaran.trim(), isActive)
                },
                colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                modifier = Modifier.testTag("button_save_semester")
            ) {
                Text("Simpan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}

// 2. Dialog Tambah / Edit Mata Kuliah
@Composable
fun MataKuliahFormDialog(
    semesterId: Long,
    initialMatkul: MataKuliahEntity? = null,
    onSave: (nama: String, kode: String, sks: Int, dosen: String, ruang: String, warna: Long) -> Unit,
    onDismiss: () -> Unit
) {
    var nama by remember { mutableStateOf(initialMatkul?.nama ?: "") }
    var kode by remember { mutableStateOf(initialMatkul?.kode ?: "") }
    var sksText by remember { mutableStateOf(initialMatkul?.sks?.toString() ?: "3") }
    var namaDosen by remember { mutableStateOf(initialMatkul?.namaDosen ?: "") }
    var ruang by remember { mutableStateOf(initialMatkul?.ruang ?: "") }
    var selectedColor by remember { mutableLongStateOf(initialMatkul?.warnaLabel ?: 0xFF0D9488) }
    var errorText by remember { mutableStateOf<String?>(null) }

    val presetColors = listOf(
        0xFF0D9488, // Teal
        0xFFF59E0B, // Amber
        0xFF6366F1, // Indigo
        0xFFEC4899, // Pink
        0xFF10B981, // Emerald
        0xFF8B5CF6  // Purple
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initialMatkul == null) "Tambah Mata Kuliah" else "Edit Mata Kuliah",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(
                    value = nama,
                    onValueChange = {
                        nama = it
                        errorText = null
                    },
                    label = { Text("Nama Mata Kuliah *") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_matkul_nama")
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = kode,
                        onValueChange = { kode = it },
                        label = { Text("Kode (opsional)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = sksText,
                        onValueChange = { sksText = it },
                        label = { Text("SKS") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.width(90.dp)
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = namaDosen,
                    onValueChange = { namaDosen = it },
                    label = { Text("Dosen Pengampu (opsional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = ruang,
                    onValueChange = { ruang = it },
                    label = { Text("Ruangan Kelas (opsional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "Warna Label",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    presetColors.forEach { colorVal ->
                        val isPicked = selectedColor == colorVal
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color(colorVal))
                                .border(
                                    width = if (isPicked) 3.dp else 0.dp,
                                    color = if (isPicked) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { selectedColor = colorVal }
                        )
                    }
                }

                if (errorText != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = errorText ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (nama.isBlank()) {
                        errorText = "Nama mata kuliah wajib diisi!"
                        return@Button
                    }
                    val sksInt = sksText.toIntOrNull() ?: 3
                    onSave(nama.trim(), kode.trim(), sksInt, namaDosen.trim(), ruang.trim(), selectedColor)
                },
                colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                modifier = Modifier.testTag("button_save_matkul")
            ) {
                Text("Simpan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}

// 3. Dialog Tambah / Edit Jadwal Kuliah
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JadwalFormDialog(
    matkulList: List<MataKuliahEntity>,
    initialJadwal: JadwalKuliahEntity? = null,
    defaultHari: Int = 1,
    onSave: (mataKuliahId: Long, hari: Int, jamMulai: String, jamSelesai: String, ruang: String, catatan: String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedMatkulId by remember {
        mutableLongStateOf(initialJadwal?.mataKuliahId ?: matkulList.firstOrNull()?.id ?: -1L)
    }
    var selectedHari by remember { mutableIntStateOf(initialJadwal?.hari ?: defaultHari) }
    var jamMulai by remember { mutableStateOf(initialJadwal?.jamMulai ?: "08:00") }
    var jamSelesai by remember { mutableStateOf(initialJadwal?.jamSelesai ?: "09:40") }
    var ruang by remember { mutableStateOf(initialJadwal?.ruang ?: "") }
    var catatan by remember { mutableStateOf(initialJadwal?.catatan ?: "") }
    var matkulDropdownExpanded by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val hariNames = listOf(
        1 to "Senin",
        2 to "Selasa",
        3 to "Rabu",
        4 to "Kamis",
        5 to "Jumat",
        6 to "Sabtu",
        7 to "Minggu"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initialJadwal == null) "Tambah Jadwal Kuliah" else "Edit Jadwal Kuliah",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                if (matkulList.isEmpty()) {
                    Text(
                        text = "Belum ada mata kuliah di semester ini. Tambahkan mata kuliah terlebih dahulu!",
                        color = MaterialTheme.colorScheme.error
                    )
                } else {
                    // Mata Kuliah Dropdown
                    ExposedDropdownMenuBox(
                        expanded = matkulDropdownExpanded,
                        onExpandedChange = { matkulDropdownExpanded = !matkulDropdownExpanded }
                    ) {
                        val currentMatkul = matkulList.find { it.id == selectedMatkulId }
                        OutlinedTextField(
                            value = currentMatkul?.nama ?: "Pilih Mata Kuliah",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Mata Kuliah *") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = matkulDropdownExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
                        )
                        ExposedDropdownMenu(
                            expanded = matkulDropdownExpanded,
                            onDismissRequest = { matkulDropdownExpanded = false }
                        ) {
                            matkulList.forEach { m ->
                                DropdownMenuItem(
                                    text = { Text(m.nama) },
                                    onClick = {
                                        selectedMatkulId = m.id
                                        matkulDropdownExpanded = false
                                        if (ruang.isBlank() && m.ruang.isNotBlank()) {
                                            ruang = m.ruang
                                        }
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Hari selection
                    Text(
                        text = "Pilih Hari",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        hariNames.take(4).forEach { (hNum, hName) ->
                            FilterChip(
                                selected = selectedHari == hNum,
                                onClick = { selectedHari = hNum },
                                label = { Text(hName, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = TealPrimary)
                            )
                        }
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        hariNames.drop(4).forEach { (hNum, hName) ->
                            FilterChip(
                                selected = selectedHari == hNum,
                                onClick = { selectedHari = hNum },
                                label = { Text(hName, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = TealPrimary)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Jam Mulai & Selesai
                    Row(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = jamMulai,
                            onValueChange = { jamMulai = it },
                            label = { Text("Mulai (JJ:MM)") },
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.Schedule,
                                    contentDescription = "Pilih Jam",
                                    modifier = Modifier.clickable {
                                        showTimePicker(context, jamMulai) { jamMulai = it }
                                    }
                                )
                            },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedTextField(
                            value = jamSelesai,
                            onValueChange = { jamSelesai = it },
                            label = { Text("Selesai (JJ:MM)") },
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.Schedule,
                                    contentDescription = "Pilih Jam Selesai",
                                    modifier = Modifier.clickable {
                                        showTimePicker(context, jamSelesai) { jamSelesai = it }
                                    }
                                )
                            },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = ruang,
                        onValueChange = { ruang = it },
                        label = { Text("Ruangan (opsional)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = catatan,
                        onValueChange = { catatan = it },
                        label = { Text("Catatan Tambahan (opsional)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (errorText != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = errorText ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (selectedMatkulId == -1L) {
                        errorText = "Pilih mata kuliah terlebih dahulu!"
                        return@Button
                    }
                    if (jamMulai.isBlank() || jamSelesai.isBlank()) {
                        errorText = "Jam mulai dan jam selesai wajib diisi!"
                        return@Button
                    }
                    onSave(selectedMatkulId, selectedHari, jamMulai.trim(), jamSelesai.trim(), ruang.trim(), catatan.trim())
                },
                enabled = matkulList.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
            ) {
                Text("Simpan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}

// 4. Dialog Tambah / Edit Tugas
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TugasFormDialog(
    matkulList: List<MataKuliahEntity>,
    initialTugas: TugasEntity? = null,
    onSave: (mataKuliahId: Long, judul: String, deskripsi: String, deadlineMillis: Long, prioritas: PrioritasTugas) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedMatkulId by remember {
        mutableLongStateOf(initialTugas?.mataKuliahId ?: matkulList.firstOrNull()?.id ?: -1L)
    }
    var judul by remember { mutableStateOf(initialTugas?.judul ?: "") }
    var deskripsi by remember { mutableStateOf(initialTugas?.deskripsi ?: "") }
    var deadlineMillis by remember {
        mutableLongStateOf(initialTugas?.deadlineMillis ?: (System.currentTimeMillis() + 86400000L * 2))
    }
    var prioritas by remember { mutableStateOf(initialTugas?.prioritas ?: PrioritasTugas.SEDANG) }
    var matkulDropdownExpanded by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val sdf = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.forLanguageTag("id-ID")) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initialTugas == null) "Tambah Tugas" else "Edit Tugas",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                if (matkulList.isEmpty()) {
                    Text(
                        text = "Belum ada mata kuliah di semester ini. Tambahkan mata kuliah terlebih dahulu!",
                        color = MaterialTheme.colorScheme.error
                    )
                } else {
                    ExposedDropdownMenuBox(
                        expanded = matkulDropdownExpanded,
                        onExpandedChange = { matkulDropdownExpanded = !matkulDropdownExpanded }
                    ) {
                        val currentMatkul = matkulList.find { it.id == selectedMatkulId }
                        OutlinedTextField(
                            value = currentMatkul?.nama ?: "Pilih Mata Kuliah",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Mata Kuliah *") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = matkulDropdownExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
                        )
                        ExposedDropdownMenu(
                            expanded = matkulDropdownExpanded,
                            onDismissRequest = { matkulDropdownExpanded = false }
                        ) {
                            matkulList.forEach { m ->
                                DropdownMenuItem(
                                    text = { Text(m.nama) },
                                    onClick = {
                                        selectedMatkulId = m.id
                                        matkulDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = judul,
                        onValueChange = {
                            judul = it
                            errorText = null
                        },
                        label = { Text("Judul Tugas *") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_tugas_judul")
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = deskripsi,
                        onValueChange = { deskripsi = it },
                        label = { Text("Deskripsi / Petunjuk Pengerjaan") },
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Batas Waktu (Deadline)",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedButton(
                        onClick = {
                            showDateTimePicker(context, deadlineMillis) { newTime ->
                                deadlineMillis = newTime
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Filled.CalendarToday, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(sdf.format(deadlineMillis))
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Prioritas",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        PrioritasTugas.values().forEach { p ->
                            FilterChip(
                                selected = prioritas == p,
                                onClick = { prioritas = p },
                                label = { Text(p.label) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = when (p) {
                                        PrioritasTugas.TINGGI -> Color(0xFFEF4444)
                                        PrioritasTugas.SEDANG -> AmberSecondary
                                        PrioritasTugas.RENDAH -> TealPrimary
                                    }
                                )
                            )
                        }
                    }
                }

                if (errorText != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = errorText ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (selectedMatkulId == -1L) {
                        errorText = "Pilih mata kuliah terlebih dahulu!"
                        return@Button
                    }
                    if (judul.isBlank()) {
                        errorText = "Judul tugas wajib diisi!"
                        return@Button
                    }
                    onSave(selectedMatkulId, judul.trim(), deskripsi.trim(), deadlineMillis, prioritas)
                },
                enabled = matkulList.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                modifier = Modifier.testTag("button_save_tugas")
            ) {
                Text("Simpan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}

// 5. Dialog Tambah / Edit Ujian (KUIS, UTS, UAS)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UjianFormDialog(
    matkulList: List<MataKuliahEntity>,
    initialUjian: UjianEntity? = null,
    onSave: (mataKuliahId: Long, kategori: KategoriUjian, tanggalMillis: Long, jamMulai: String, jamSelesai: String, ruang: String, catatan: String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedMatkulId by remember {
        mutableLongStateOf(initialUjian?.mataKuliahId ?: matkulList.firstOrNull()?.id ?: -1L)
    }
    var kategori by remember { mutableStateOf(initialUjian?.kategori ?: KategoriUjian.KUIS) }
    var tanggalMillis by remember {
        mutableLongStateOf(initialUjian?.tanggalMillis ?: (System.currentTimeMillis() + 86400000L * 7))
    }
    var jamMulai by remember { mutableStateOf(initialUjian?.jamMulai ?: "08:00") }
    var jamSelesai by remember { mutableStateOf(initialUjian?.jamSelesai ?: "10:00") }
    var ruang by remember { mutableStateOf(initialUjian?.ruang ?: "") }
    var catatan by remember { mutableStateOf(initialUjian?.catatan ?: "") }
    var matkulDropdownExpanded by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val sdfDate = remember { SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.forLanguageTag("id-ID")) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initialUjian == null) "Tambah Agenda Ujian" else "Edit Agenda Ujian",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                if (matkulList.isEmpty()) {
                    Text(
                        text = "Belum ada mata kuliah di semester ini. Tambahkan mata kuliah terlebih dahulu!",
                        color = MaterialTheme.colorScheme.error
                    )
                } else {
                    ExposedDropdownMenuBox(
                        expanded = matkulDropdownExpanded,
                        onExpandedChange = { matkulDropdownExpanded = !matkulDropdownExpanded }
                    ) {
                        val currentMatkul = matkulList.find { it.id == selectedMatkulId }
                        OutlinedTextField(
                            value = currentMatkul?.nama ?: "Pilih Mata Kuliah",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Mata Kuliah *") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = matkulDropdownExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
                        )
                        ExposedDropdownMenu(
                            expanded = matkulDropdownExpanded,
                            onDismissRequest = { matkulDropdownExpanded = false }
                        ) {
                            matkulList.forEach { m ->
                                DropdownMenuItem(
                                    text = { Text(m.nama) },
                                    onClick = {
                                        selectedMatkulId = m.id
                                        matkulDropdownExpanded = false
                                        if (ruang.isBlank() && m.ruang.isNotBlank()) ruang = m.ruang
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Kategori Evaluasi",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        KategoriUjian.values().forEach { k ->
                            FilterChip(
                                selected = kategori == k,
                                onClick = { kategori = k },
                                label = { Text(k.label, fontWeight = FontWeight.Bold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = when (k) {
                                        KategoriUjian.KUIS -> AmberSecondary
                                        KategoriUjian.UTS -> TealPrimary
                                        KategoriUjian.UAS -> Color(0xFF6366F1)
                                    }
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Tanggal Ujian",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedButton(
                        onClick = {
                            val c = Calendar.getInstance().apply { timeInMillis = tanggalMillis }
                            DatePickerDialog(
                                context,
                                { _, year, month, dayOfMonth ->
                                    val newCal = Calendar.getInstance().apply {
                                        set(Calendar.YEAR, year)
                                        set(Calendar.MONTH, month)
                                        set(Calendar.DAY_OF_MONTH, dayOfMonth)
                                    }
                                    tanggalMillis = newCal.timeInMillis
                                },
                                c.get(Calendar.YEAR),
                                c.get(Calendar.MONTH),
                                c.get(Calendar.DAY_OF_MONTH)
                            ).show()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Filled.CalendarToday, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(sdfDate.format(tanggalMillis))
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = jamMulai,
                            onValueChange = { jamMulai = it },
                            label = { Text("Mulai") },
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.Schedule,
                                    contentDescription = null,
                                    modifier = Modifier.clickable {
                                        showTimePicker(context, jamMulai) { jamMulai = it }
                                    }
                                )
                            },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedTextField(
                            value = jamSelesai,
                            onValueChange = { jamSelesai = it },
                            label = { Text("Selesai") },
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.Schedule,
                                    contentDescription = null,
                                    modifier = Modifier.clickable {
                                        showTimePicker(context, jamSelesai) { jamSelesai = it }
                                    }
                                )
                            },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = ruang,
                        onValueChange = { ruang = it },
                        label = { Text("Ruangan / Media Ujian") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = catatan,
                        onValueChange = { catatan = it },
                        label = { Text("Materi yang diujikan / Catatan") },
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (errorText != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = errorText ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (selectedMatkulId == -1L) {
                        errorText = "Pilih mata kuliah terlebih dahulu!"
                        return@Button
                    }
                    onSave(selectedMatkulId, kategori, tanggalMillis, jamMulai.trim(), jamSelesai.trim(), ruang.trim(), catatan.trim())
                },
                enabled = matkulList.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
            ) {
                Text("Simpan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}

// 6. Dialog Tambah / Edit Materi (Dokumen Lokal PDF/DOCX/PPTX & Link YouTube)
@Composable
fun MateriFormDialog(
    mataKuliahId: Long,
    initialMateri: MateriEntity? = null,
    onSave: (pertemuanKe: Int, judulMateri: String, tipeMateri: TipeMateri, uriOrUrl: String, catatan: String) -> Unit,
    onDismiss: () -> Unit
) {
    var pertemuanKeText by remember { mutableStateOf(initialMateri?.pertemuanKe?.toString() ?: "1") }
    var judulMateri by remember { mutableStateOf(initialMateri?.judulMateri ?: "") }
    var tipeMateri by remember { mutableStateOf(initialMateri?.tipeMateri ?: TipeMateri.DOKUMEN_LOKAL) }
    var uriOrUrl by remember { mutableStateOf(initialMateri?.uriOrUrl ?: "") }
    var catatanTambahan by remember { mutableStateOf(initialMateri?.catatanTambahan ?: "") }
    var errorText by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current

    // SAF Document Picker for PDF, DOCX, PPTX
    val openDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
            uriOrUrl = uri.toString()
            errorText = null
        }
    }

    val scrollState = rememberScrollState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initialMateri == null) "Tambah Materi Kuliah" else "Edit Materi Kuliah",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp)
                    .verticalScroll(scrollState)
            ) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = pertemuanKeText,
                        onValueChange = { pertemuanKeText = it },
                        label = { Text("Pertemuan") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(0.4f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = judulMateri,
                        onValueChange = {
                            judulMateri = it
                            errorText = null
                        },
                        label = { Text("Topik / Judul Materi *") },
                        singleLine = true,
                        modifier = Modifier
                            .weight(0.6f)
                            .testTag("input_materi_judul")
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "Jenis Sumber Belajar",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TipeMateri.values().forEach { t ->
                        FilterChip(
                            selected = tipeMateri == t,
                            onClick = {
                                tipeMateri = t
                                errorText = null
                            },
                            label = { Text(t.label) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = TealPrimary)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (tipeMateri == TipeMateri.DOKUMEN_LOKAL) {
                    Text(
                        text = "Berkas Dokumen (PDF, DOCX, PPTX)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedButton(
                        onClick = {
                            openDocumentLauncher.launch(
                                arrayOf(
                                    "application/pdf",
                                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                                    "application/vnd.openxmlformats-officedocument.presentationml.presentation",
                                    "application/msword",
                                    "application/vnd.ms-powerpoint"
                                )
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Filled.AttachFile, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (uriOrUrl.isNotBlank()) "Ganti Berkas Dokumen" else "Pilih Berkas Dokumen...")
                    }
                    if (uriOrUrl.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "URI: $uriOrUrl",
                            style = MaterialTheme.typography.labelSmall,
                            color = TealPrimary,
                            maxLines = 2
                        )
                    }
                } else {
                    OutlinedTextField(
                        value = uriOrUrl,
                        onValueChange = {
                            uriOrUrl = it
                            errorText = null
                        },
                        label = { Text("Tautan Video YouTube (https://...) *") },
                        placeholder = { Text("https://youtu.be/...") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = catatanTambahan,
                    onValueChange = { catatanTambahan = it },
                    label = { Text("Catatan Singkat / Ringkasan (opsional)") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                if (errorText != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = errorText ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (judulMateri.isBlank()) {
                        errorText = "Judul materi wajib diisi!"
                        return@Button
                    }
                    if (uriOrUrl.isBlank()) {
                        errorText = if (tipeMateri == TipeMateri.DOKUMEN_LOKAL)
                            "Pilih berkas dokumen terlebih dahulu!"
                        else
                            "Masukkan tautan video YouTube!"
                        return@Button
                    }
                    val pKe = pertemuanKeText.toIntOrNull() ?: 1
                    onSave(pKe, judulMateri.trim(), tipeMateri, uriOrUrl.trim(), catatanTambahan.trim())
                },
                colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                modifier = Modifier.testTag("button_save_materi")
            ) {
                Text(if (initialMateri == null) "Simpan" else "Simpan Perubahan")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("button_cancel_materi")
            ) {
                Text("Batal")
            }
        }
    )
}

private fun showTimePicker(context: Context, currentTimeStr: String, onTimePicked: (String) -> Unit) {
    val parts = currentTimeStr.split(":")
    val currentHour = parts.getOrNull(0)?.toIntOrNull() ?: 8
    val currentMinute = parts.getOrNull(1)?.toIntOrNull() ?: 0

    TimePickerDialog(
        context,
        { _, hourOfDay, minute ->
            val formatted = String.format(Locale.ROOT, "%02d:%02d", hourOfDay, minute)
            onTimePicked(formatted)
        },
        currentHour,
        currentMinute,
        true
    ).show()
}

private fun showDateTimePicker(context: Context, currentMillis: Long, onPicked: (Long) -> Unit) {
    val cal = Calendar.getInstance().apply { timeInMillis = currentMillis }
    DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            TimePickerDialog(
                context,
                { _, hourOfDay, minute ->
                    val resultCal = Calendar.getInstance().apply {
                        set(Calendar.YEAR, year)
                        set(Calendar.MONTH, month)
                        set(Calendar.DAY_OF_MONTH, dayOfMonth)
                        set(Calendar.HOUR_OF_DAY, hourOfDay)
                        set(Calendar.MINUTE, minute)
                        set(Calendar.SECOND, 0)
                    }
                    onPicked(resultCal.timeInMillis)
                },
                cal.get(Calendar.HOUR_OF_DAY),
                cal.get(Calendar.MINUTE),
                true
            ).show()
        },
        cal.get(Calendar.YEAR),
        cal.get(Calendar.MONTH),
        cal.get(Calendar.DAY_OF_MONTH)
    ).show()
}
