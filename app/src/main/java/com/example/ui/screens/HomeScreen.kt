package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.EventNote
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Class
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.JadwalKuliahEntity
import com.example.data.local.entity.MataKuliahEntity
import com.example.data.local.entity.ProfileEntity
import com.example.data.local.entity.SemesterEntity
import com.example.data.local.entity.TugasEntity
import com.example.data.model.PrioritasTugas
import com.example.data.model.StatusTugas
import com.example.ui.components.CoolyeahPriorityBadge
import com.example.ui.components.CoolyeahStatusBadge
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.JadwalFormDialog
import com.example.ui.components.MataKuliahFormDialog
import com.example.ui.components.SemesterFormDialog
import com.example.ui.components.TugasFormDialog
import com.example.ui.components.UjianFormDialog
import com.example.ui.theme.AmberLight
import com.example.ui.theme.AmberSecondary
import com.example.ui.theme.StatusDone
import com.example.ui.theme.StatusOverdue
import com.example.ui.theme.TealContainer
import com.example.ui.theme.TealLight
import com.example.ui.theme.TealPrimary
import com.example.ui.viewmodel.CoolyeahViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: CoolyeahViewModel,
    profile: ProfileEntity?,
    semesters: List<SemesterEntity>,
    activeSemester: SemesterEntity?,
    currentSemesterId: Long?,
    matkulList: List<MataKuliahEntity>,
    jadwalList: List<JadwalKuliahEntity>,
    tugasList: List<TugasEntity>,
    onNavigateToTab: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showQuickActionSheet by remember { mutableStateOf(false) }
    var showAddSemesterDialog by remember { mutableStateOf(false) }
    var showAddMatkulDialog by remember { mutableStateOf(false) }
    var showAddJadwalDialog by remember { mutableStateOf(false) }
    var showAddTugasDialog by remember { mutableStateOf(false) }
    var showAddUjianDialog by remember { mutableStateOf(false) }

    // Identify today's day of week: 1=Senin .. 7=Minggu
    val todayHari = remember {
        val cal = Calendar.getInstance()
        when (cal.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> 1
            Calendar.TUESDAY -> 2
            Calendar.WEDNESDAY -> 3
            Calendar.THURSDAY -> 4
            Calendar.FRIDAY -> 5
            Calendar.SATURDAY -> 6
            Calendar.SUNDAY -> 7
            else -> 1
        }
    }

    val todayJadwal = remember(jadwalList, todayHari) {
        jadwalList.filter { it.hari == todayHari }.sortedBy { it.jamMulai }
    }

    val urgentTugas = remember(tugasList) {
        tugasList.filter { !it.isSelesai }
            .sortedBy { it.deadlineMillis }
            .take(4)
    }

    val totalSks = remember(matkulList) {
        matkulList.sumOf { it.sks }
    }

    val currentSemester = semesters.find { it.id == currentSemesterId }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxSize()
                .testTag("home_screen_scroll")
        ) {
            // 1. Adaptive Greeting Card
            item {
                GreetingSection(profile = profile)
            }

            // 2. Semester Overview & Onboarding
            item {
                if (semesters.isEmpty()) {
                    EmptyStateCard(
                        icon = Icons.Filled.School,
                        title = "Yuk Kenalan & Atur Kuliahmu!",
                        message = "Belum ada semester yang ditambahkan. Mulai langkah pertamamu dengan membuat semester aktif agar jadwal dan tugas tertata rapi.",
                        buttonText = "+ Buat Semester Pertama",
                        onButtonClick = { showAddSemesterDialog = true }
                    )
                } else {
                    SemesterOverviewCard(
                        semester = currentSemester,
                        totalSks = totalSks,
                        totalMatkul = matkulList.size,
                        onAddMatkul = { showAddMatkulDialog = true }
                    )
                }
            }

            // 3. Today's Class Schedule Section
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(TealLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Schedule,
                                contentDescription = null,
                                tint = TealPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Kuliah Hari Ini",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    TextButton(onClick = { onNavigateToTab("jadwal") }) {
                        Text("Lihat Semua", color = TealPrimary, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            if (semesters.isNotEmpty()) {
                if (todayJadwal.isEmpty()) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(AmberLight),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.CheckCircle,
                                        contentDescription = null,
                                        tint = AmberSecondary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Hari ini free kuliah!",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    Text(
                                        text = "Santai dulu ga sih. Jangan lupa cicil tugas tipis-tipis ya.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                } else {
                    items(todayJadwal, key = { "jadwal_${it.id}" }) { jadwal ->
                        val matkul = matkulList.find { it.id == jadwal.mataKuliahId }
                        HomeClassCard(jadwal = jadwal, matkul = matkul)
                    }
                }
            }

            // 4. Urgent / Pending Tasks Section
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(AmberLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Assignment,
                                contentDescription = null,
                                tint = AmberSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Tugas Mendesak",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    TextButton(onClick = { onNavigateToTab("tugas") }) {
                        Text("Lihat Semua", color = TealPrimary, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            if (semesters.isNotEmpty()) {
                if (urgentTugas.isEmpty()) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(TealLight),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.CheckCircle,
                                        contentDescription = null,
                                        tint = TealPrimary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Semua tugas udah beres!",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    Text(
                                        text = "Tidak ada tugas yang tertunda di semester ini. Mantap coy!",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                } else {
                    items(urgentTugas, key = { "tugas_${it.id}" }) { tugas ->
                        val matkul = matkulList.find { it.id == tugas.mataKuliahId }
                        HomeTaskItem(
                            tugas = tugas,
                            matkul = matkul,
                            onToggleSelesai = { isDone ->
                                viewModel.setTugasSelesai(tugas, isDone)
                            }
                        )
                    }
                }
            }

            // Bottom spacer for FAB
            item {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }

        // Quick Action Speed Dial FAB
        FloatingActionButton(
            onClick = { showQuickActionSheet = true },
            containerColor = TealPrimary,
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("home_quick_action_fab")
        ) {
            Icon(imageVector = Icons.Filled.Add, contentDescription = "Tambah Cepat", modifier = Modifier.size(28.dp))
        }
    }

    // Modal Bottom Sheet for Quick Actions
    if (showQuickActionSheet) {
        ModalBottomSheet(
            onDismissRequest = { showQuickActionSheet = false },
            sheetState = rememberModalBottomSheetState(),
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                Text(
                    text = "Aksi Cepat MyCoolyeah",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))

                QuickActionItem(
                    icon = Icons.Filled.Class,
                    color = TealPrimary,
                    title = "Tambah Mata Kuliah",
                    subtitle = "Masukkan matkul baru, SKS, dan dosen",
                    onClick = {
                        showQuickActionSheet = false
                        showAddMatkulDialog = true
                    }
                )

                QuickActionItem(
                    icon = Icons.Filled.CalendarToday,
                    color = AmberSecondary,
                    title = "Tambah Jadwal Kuliah",
                    subtitle = "Tentukan hari, jam kelas, dan ruangan",
                    onClick = {
                        showQuickActionSheet = false
                        showAddJadwalDialog = true
                    }
                )

                QuickActionItem(
                    icon = Icons.AutoMirrored.Filled.Assignment,
                    color = Color(0xFF6366F1),
                    title = "Tambah Tugas Kuliah",
                    subtitle = "Catat deadline dan prioritas tugas",
                    onClick = {
                        showQuickActionSheet = false
                        showAddTugasDialog = true
                    }
                )

                QuickActionItem(
                    icon = Icons.AutoMirrored.Filled.EventNote,
                    color = Color(0xFFEC4899),
                    title = "Tambah Agenda Ujian",
                    subtitle = "Jadwalkan Kuis, UTS, atau UAS",
                    onClick = {
                        showQuickActionSheet = false
                        showAddUjianDialog = true
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Dialogs
    if (showAddSemesterDialog) {
        SemesterFormDialog(
            onSave = { nama, tahun, isActive ->
                viewModel.addSemester(nama, tahun, isActive)
                showAddSemesterDialog = false
            },
            onDismiss = { showAddSemesterDialog = false }
        )
    }

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

    if (showAddJadwalDialog) {
        JadwalFormDialog(
            matkulList = matkulList,
            defaultHari = todayHari,
            onSave = { matkulId, hari, mulai, selesai, ruang, catatan ->
                viewModel.addJadwal(matkulId, hari, mulai, selesai, ruang, catatan)
                showAddJadwalDialog = false
            },
            onDismiss = { showAddJadwalDialog = false }
        )
    }

    if (showAddTugasDialog) {
        TugasFormDialog(
            matkulList = matkulList,
            onSave = { matkulId, judul, deskripsi, deadline, prioritas ->
                viewModel.addTugas(matkulId, judul, deskripsi, deadline, prioritas)
                showAddTugasDialog = false
            },
            onDismiss = { showAddTugasDialog = false }
        )
    }

    if (showAddUjianDialog) {
        UjianFormDialog(
            matkulList = matkulList,
            onSave = { matkulId, kategori, tgl, mulai, selesai, ruang, catatan ->
                viewModel.addUjian(matkulId, kategori, tgl, mulai, selesai, ruang, catatan)
                showAddUjianDialog = false
            },
            onDismiss = { showAddUjianDialog = false }
        )
    }
}

@Composable
private fun GreetingSection(profile: ProfileEntity?) {
    val nama = profile?.nama?.trim()
    val isNameEmpty = nama.isNullOrBlank()

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = TealContainer,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("home_greeting_card")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(18.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(TealPrimary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = if (isNameEmpty) "Halo, Sobat Kuliah!" else "Halo, $nama!",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F766E),
                        fontSize = 17.sp
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (isNameEmpty)
                        "Yuk mulai atur jadwal kamu."
                    else
                        "Kuliah hari ini santai tapi pasti. Jangan lupa cek tugas ya!",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF115E59)
                )
            }
        }
    }
}

@Composable
private fun SemesterOverviewCard(
    semester: SemesterEntity?,
    totalSks: Int,
    totalMatkul: Int,
    onAddMatkul: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = semester?.namaSemester ?: "Pilih Semester",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = semester?.tahunAjaran ?: "-",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (semester?.isActive == true) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(TealLight)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Semester Aktif",
                            style = MaterialTheme.typography.labelSmall,
                            color = TealPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Total SKS", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("$totalSks SKS", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TealPrimary)
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Mata Kuliah", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("$totalMatkul Matkul", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = AmberSecondary)
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeClassCard(jadwal: JadwalKuliahEntity, matkul: MataKuliahEntity?) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(5.dp)
                    .height(48.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(matkul?.warnaLabel ?: 0xFF0D9488))
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = matkul?.nama ?: "Mata Kuliah",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Schedule,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${jadwal.jamMulai} - ${jadwal.jamSelesai}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (jadwal.ruang.isNotBlank()) {
                        Spacer(modifier = Modifier.width(12.dp))
                        Icon(
                            imageVector = Icons.Filled.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = jadwal.ruang,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeTaskItem(
    tugas: TugasEntity,
    matkul: MataKuliahEntity?,
    onToggleSelesai: (Boolean) -> Unit
) {
    val sdf = remember { SimpleDateFormat("dd MMM, HH:mm", Locale.forLanguageTag("id-ID")) }
    val status = tugas.getStatus()

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(12.dp)
        ) {
            Checkbox(
                checked = tugas.isSelesai,
                onCheckedChange = onToggleSelesai,
                colors = CheckboxDefaults.colors(checkedColor = StatusDone)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 4.dp)
            ) {
                Text(
                    text = tugas.judul,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        textDecoration = if (tugas.isSelesai) TextDecoration.LineThrough else TextDecoration.None
                    ),
                    color = if (tugas.isSelesai) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = matkul?.nama ?: "Kuliah",
                        style = MaterialTheme.typography.bodySmall,
                        color = TealPrimary,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "• ${sdf.format(tugas.deadlineMillis)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (status == StatusTugas.TERLEWAT) StatusOverdue else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = if (status == StatusTugas.TERLEWAT) FontWeight.Bold else FontWeight.Normal,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }

            if (status == StatusTugas.TERLEWAT) {
                Spacer(modifier = Modifier.width(6.dp))
                CoolyeahStatusBadge(status = StatusTugas.TERLEWAT)
            }
        }
    }
}

@Composable
private fun QuickActionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(text = title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
