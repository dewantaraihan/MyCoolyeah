package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.MataKuliahEntity
import com.example.data.local.entity.ProfileEntity
import com.example.data.local.entity.SemesterEntity
import com.example.data.local.entity.TugasEntity
import com.example.data.repository.MataKuliahDeletionImpact
import com.example.data.repository.SemesterDeletionImpact
import com.example.ui.components.ConfirmDeleteDialog
import com.example.ui.components.MataKuliahFormDialog
import com.example.ui.components.SemesterFormDialog
import com.example.ui.theme.AmberLight
import com.example.ui.theme.AmberSecondary
import com.example.ui.theme.StatusDone
import com.example.ui.theme.TealContainer
import com.example.ui.theme.TealLight
import com.example.ui.theme.TealPrimary
import com.example.ui.viewmodel.CoolyeahViewModel
import kotlinx.coroutines.launch

@Composable
fun ProfilScreen(
    viewModel: CoolyeahViewModel,
    profile: ProfileEntity?,
    semesters: List<SemesterEntity>,
    activeSemester: SemesterEntity?,
    currentSemesterId: Long?,
    matkulList: List<MataKuliahEntity>,
    tugasList: List<TugasEntity>,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()

    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showAddSemesterDialog by remember { mutableStateOf(false) }
    var editingSemester by remember { mutableStateOf<SemesterEntity?>(null) }
    var deletingSemesterWithImpact by remember { mutableStateOf<Pair<SemesterEntity, SemesterDeletionImpact>?>(null) }

    var showAddMatkulDialog by remember { mutableStateOf(false) }
    var editingMatkul by remember { mutableStateOf<MataKuliahEntity?>(null) }
    var deletingMatkulWithImpact by remember { mutableStateOf<Pair<MataKuliahEntity, MataKuliahDeletionImpact>?>(null) }

    val totalCompletedTasks = remember(tugasList) {
        tugasList.count { it.isSelesai }
    }
    val totalSks = remember(matkulList) {
        matkulList.sumOf { it.sks }
    }

    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier
            .fillMaxSize()
            .testTag("profil_screen_scroll")
    ) {
        // 1. Profile Header Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(TealContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = profile?.nama?.take(1)?.uppercase()?.ifBlank { "M" } ?: "M",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TealPrimary
                                )
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = profile?.nama?.ifBlank { "Mahasiswa Keren" } ?: "Mahasiswa Keren",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                            )
                            if (profile?.nim?.isNotBlank() == true) {
                                Text(
                                    text = "NIM: ${profile.nim}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (profile?.universitas?.isNotBlank() == true) {
                                Text(
                                    text = profile.universitas,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TealPrimary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            if (profile?.jurusan?.isNotBlank() == true || profile?.angkatan?.isNotBlank() == true) {
                                Text(
                                    text = "${profile?.jurusan ?: ""} ${profile?.angkatan ?: ""}".trim(),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        IconButton(
                            onClick = { showEditProfileDialog = true },
                            modifier = Modifier.testTag("button_edit_profile")
                        ) {
                            Icon(imageVector = Icons.Filled.Edit, contentDescription = "Edit Profil", tint = TealPrimary)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Academic Summary Counters
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = TealContainer,
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("Total SKS", style = MaterialTheme.typography.labelSmall, color = TealPrimary)
                                Text("$totalSks", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TealPrimary)
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = AmberLight,
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("Mata Kuliah", style = MaterialTheme.typography.labelSmall, color = AmberSecondary)
                                Text("${matkulList.size}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = AmberSecondary)
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("Tugas Beres", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("$totalCompletedTasks", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = StatusDone)
                            }
                        }
                    }
                }
            }
        }

        // 2. Manage Semesters Section
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Daftar Semester",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = { showAddSemesterDialog = true }) {
                    Icon(imageVector = Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Tambah", fontWeight = FontWeight.Bold, color = TealPrimary)
                }
            }
        }

        if (semesters.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.padding(16.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Belum ada semester. Klik \"Tambah\" untuk membuat semester pertama.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(semesters, key = { "sem_${it.id}" }) { semester ->
                SemesterItemCard(
                    semester = semester,
                    isActive = semester.isActive,
                    onSetActive = {
                        viewModel.setActiveSemester(semester.id)
                    },
                    onEdit = { editingSemester = semester },
                    onDelete = {
                        coroutineScope.launch {
                            val impact = viewModel.getSemesterDeletionImpact(semester.id)
                            deletingSemesterWithImpact = Pair(semester, impact)
                        }
                    }
                )
            }
        }

        // 3. Manage Mata Kuliah Section for Selected Semester
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = "Mata Kuliah Semester Ini",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${matkulList.size} mata kuliah terdaftar",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (currentSemesterId != null) {
                    TextButton(onClick = { showAddMatkulDialog = true }) {
                        Icon(imageVector = Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Tambah", fontWeight = FontWeight.Bold, color = TealPrimary)
                    }
                }
            }
        }

        if (currentSemesterId == null || matkulList.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.padding(16.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = if (currentSemesterId == null) "Pilih semester terlebih dahulu."
                            else "Belum ada mata kuliah di semester ini.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(matkulList, key = { "matkul_${it.id}" }) { matkul ->
                MatkulManagementItem(
                    matkul = matkul,
                    onEdit = { editingMatkul = matkul },
                    onDelete = {
                        coroutineScope.launch {
                            val impact = viewModel.getMataKuliahDeletionImpact(matkul.id)
                            deletingMatkulWithImpact = Pair(matkul, impact)
                        }
                    }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    // Edit Profile Dialog
    if (showEditProfileDialog) {
        ProfileEditDialog(
            currentProfile = profile,
            onSave = { nama, nim, univ, jur, angkatan ->
                viewModel.updateProfile(nama, nim, univ, jur, angkatan)
                showEditProfileDialog = false
            },
            onDismiss = { showEditProfileDialog = false }
        )
    }

    // Add Semester Dialog
    if (showAddSemesterDialog) {
        SemesterFormDialog(
            onSave = { nama, tahun, isActive ->
                viewModel.addSemester(nama, tahun, isActive)
                showAddSemesterDialog = false
            },
            onDismiss = { showAddSemesterDialog = false }
        )
    }

    // Edit Semester Dialog
    if (editingSemester != null) {
        SemesterFormDialog(
            initialSemester = editingSemester,
            onSave = { nama, tahun, isActive ->
                val curr = editingSemester ?: return@SemesterFormDialog
                viewModel.updateSemester(curr.copy(namaSemester = nama, tahunAjaran = tahun, isActive = isActive))
                if (isActive) {
                    viewModel.setActiveSemester(curr.id)
                }
                editingSemester = null
            },
            onDismiss = { editingSemester = null }
        )
    }

    // Delete Semester Confirmation with CASCADE Impact Details
    if (deletingSemesterWithImpact != null) {
        val (semester, impact) = deletingSemesterWithImpact!!
        ConfirmDeleteDialog(
            title = "Hapus ${semester.namaSemester}?",
            message = "PERINGATAN: Menghapus semester ini akan menghapus:\n" +
                    "• ${impact.matkulCount} mata kuliah\n" +
                    "• ${impact.jadwalCount} jadwal kelas\n" +
                    "• ${impact.tugasCount} tugas kuliah\n" +
                    "• ${impact.ujianCount} agenda ujian\n" +
                    "• ${impact.materiCount} materi perkuliahan\n\n" +
                    "Tindakan ini tidak dapat dibatalkan. Lanjutkan hapus?",
            onConfirm = {
                viewModel.deleteSemester(semester)
                deletingSemesterWithImpact = null
            },
            onDismiss = { deletingSemesterWithImpact = null }
        )
    }

    // Add Mata Kuliah Dialog
    if (showAddMatkulDialog && currentSemesterId != null) {
        MataKuliahFormDialog(
            semesterId = currentSemesterId,
            onSave = { nama, kode, sks, dosen, ruang, warna ->
                viewModel.addMataKuliah(currentSemesterId, nama, kode, sks, dosen, ruang, warna)
                showAddMatkulDialog = false
            },
            onDismiss = { showAddMatkulDialog = false }
        )
    }

    // Edit Mata Kuliah Dialog
    if (editingMatkul != null) {
        val curr = editingMatkul!!
        MataKuliahFormDialog(
            semesterId = curr.semesterId,
            initialMatkul = curr,
            onSave = { nama, kode, sks, dosen, ruang, warna ->
                viewModel.updateMataKuliah(
                    curr.copy(
                        nama = nama,
                        kode = kode,
                        sks = sks,
                        namaDosen = dosen,
                        ruang = ruang,
                        warnaLabel = warna
                    )
                )
                editingMatkul = null
            },
            onDismiss = { editingMatkul = null }
        )
    }

    // Delete Mata Kuliah Confirmation with CASCADE Impact Details
    if (deletingMatkulWithImpact != null) {
        val (matkul, impact) = deletingMatkulWithImpact!!
        ConfirmDeleteDialog(
            title = "Hapus Mata Kuliah ${matkul.nama}?",
            message = "Menghapus mata kuliah ini akan menghapus:\n" +
                    "• ${impact.jadwalCount} jadwal kelas\n" +
                    "• ${impact.tugasCount} tugas kuliah\n" +
                    "• ${impact.ujianCount} agenda ujian\n" +
                    "• ${impact.materiCount} berkas materi\n\n" +
                    "Tetap hapus mata kuliah ini?",
            onConfirm = {
                viewModel.deleteMataKuliah(matkul)
                deletingMatkulWithImpact = null
            },
            onDismiss = { deletingMatkulWithImpact = null }
        )
    }
}

@Composable
private fun SemesterItemCard(
    semester: SemesterEntity,
    isActive: Boolean,
    onSetActive: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(14.dp)
        ) {
            IconButton(onClick = onSetActive) {
                Icon(
                    imageVector = if (isActive) Icons.Filled.Star else Icons.Outlined.StarBorder,
                    contentDescription = if (isActive) "Semester Aktif" else "Set Aktif",
                    tint = if (isActive) AmberSecondary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = semester.namaSemester,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    if (isActive) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(TealLight)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Aktif",
                                style = MaterialTheme.typography.labelSmall,
                                color = TealPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                Text(
                    text = semester.tahunAjaran,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row {
                IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                    Icon(imageVector = Icons.Filled.Edit, contentDescription = "Edit", modifier = Modifier.size(16.dp))
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(imageVector = Icons.Filled.Delete, contentDescription = "Hapus", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
private fun MatkulManagementItem(
    matkul: MataKuliahEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(Color(matkul.warnaLabel))
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = matkul.nama,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "${matkul.kode.ifBlank { "Mata Kuliah" }} • ${matkul.sks} SKS ${if (matkul.namaDosen.isNotBlank()) "• " + matkul.namaDosen else ""}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }

            Row {
                IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                    Icon(imageVector = Icons.Filled.Edit, contentDescription = "Edit", modifier = Modifier.size(16.dp))
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(imageVector = Icons.Filled.Delete, contentDescription = "Hapus", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
private fun ProfileEditDialog(
    currentProfile: ProfileEntity?,
    onSave: (nama: String, nim: String, univ: String, jur: String, angkatan: String) -> Unit,
    onDismiss: () -> Unit
) {
    var nama by remember { mutableStateOf(currentProfile?.nama ?: "") }
    var nim by remember { mutableStateOf(currentProfile?.nim ?: "") }
    var univ by remember { mutableStateOf(currentProfile?.universitas ?: "") }
    var jur by remember { mutableStateOf(currentProfile?.jurusan ?: "") }
    var angkatan by remember { mutableStateOf(currentProfile?.angkatan ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Profil Mahasiswa", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(
                    value = nama,
                    onValueChange = { nama = it },
                    label = { Text("Nama Lengkap / Panggilan") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_profile_nama")
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = nim,
                    onValueChange = { nim = it },
                    label = { Text("Nomor Induk Mahasiswa (NIM)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = univ,
                    onValueChange = { univ = it },
                    label = { Text("Perguruan Tinggi / Universitas") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = jur,
                    onValueChange = { jur = it },
                    label = { Text("Program Studi / Jurusan") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = angkatan,
                    onValueChange = { angkatan = it },
                    label = { Text("Tahun Angkatan (misal: 2024)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(nama.trim(), nim.trim(), univ.trim(), jur.trim(), angkatan.trim()) },
                colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                modifier = Modifier.testTag("button_save_profile")
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
