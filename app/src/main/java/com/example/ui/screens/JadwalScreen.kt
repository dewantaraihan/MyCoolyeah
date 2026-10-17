package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import com.example.ui.components.CoolyeahFilterChip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.OndemandVideo
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.JadwalKuliahEntity
import com.example.data.local.entity.MataKuliahEntity
import com.example.data.local.entity.MateriEntity
import com.example.data.model.TipeMateri
import com.example.ui.components.ConfirmDeleteDialog
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.JadwalFormDialog
import com.example.ui.components.MateriFormDialog
import com.example.ui.theme.TealLight
import com.example.ui.theme.TealPrimary
import com.example.ui.viewmodel.CoolyeahViewModel
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JadwalScreen(
    viewModel: CoolyeahViewModel,
    matkulList: List<MataKuliahEntity>,
    jadwalList: List<JadwalKuliahEntity>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Day of week mapping: 1=Senin .. 7=Minggu
    val currentDayOfWeek = remember {
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

    var selectedDay by remember { mutableIntStateOf(currentDayOfWeek) }
    var showAddDialog by remember { mutableStateOf(false) }
    var editingJadwal by remember { mutableStateOf<JadwalKuliahEntity?>(null) }
    var deletingJadwal by remember { mutableStateOf<JadwalKuliahEntity?>(null) }

    // Inspect materials for a course
    var inspectingMatkulForMateri by remember { mutableStateOf<MataKuliahEntity?>(null) }
    var showAddMateriDialog by remember { mutableStateOf(false) }
    var editingMateri by remember { mutableStateOf<MateriEntity?>(null) }
    var deletingMateri by remember { mutableStateOf<MateriEntity?>(null) }

    val days = listOf(
        1 to "Senin",
        2 to "Selasa",
        3 to "Rabu",
        4 to "Kamis",
        5 to "Jumat",
        6 to "Sabtu",
        7 to "Minggu"
    )

    val filteredJadwal = remember(jadwalList, selectedDay) {
        jadwalList.filter { it.hari == selectedDay }.sortedBy { it.jamMulai }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Day selection tab bar
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth().testTag("jadwal_day_filter_row")
                ) {
                    items(days) { (dayNum, dayName) ->
                        val isSelected = selectedDay == dayNum
                        val isToday = currentDayOfWeek == dayNum

                        CoolyeahFilterChip(
                            selected = isSelected,
                            onClick = { selectedDay = dayNum },
                            label = dayName,
                            selectedContainerColor = TealPrimary,
                            leadingIcon = if (isToday) {
                                {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(if (isSelected) Color.White else TealPrimary)
                                    )
                                }
                            } else null,
                            modifier = Modifier.testTag("day_chip_$dayNum")
                        )
                    }
                }
            }

            // Schedules List
            if (filteredJadwal.isEmpty()) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 60.dp)
                ) {
                    val dayName = days.find { it.first == selectedDay }?.second ?: "Hari ini"
                    EmptyStateCard(
                        icon = Icons.Filled.CalendarMonth,
                        title = "$dayName Free Kuliah!",
                        message = "Belum ada jadwal kelas yang terdaftar pada hari ini. Kamu bisa bersantai atau menambah jadwal baru.",
                        buttonText = "+ Tambah Jadwal $dayName",
                        onButtonClick = { showAddDialog = true }
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("jadwal_list")
                ) {
                    items(filteredJadwal, key = { "jadwal_${it.id}" }) { jadwal ->
                        val matkul = matkulList.find { it.id == jadwal.mataKuliahId }
                        JadwalCard(
                            jadwal = jadwal,
                            matkul = matkul,
                            onEdit = { editingJadwal = jadwal },
                            onDelete = { deletingJadwal = jadwal },
                            onOpenMateri = { inspectingMatkulForMateri = matkul }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(72.dp))
                    }
                }
            }
        }

        // Add Jadwal FAB
        FloatingActionButton(
            onClick = { showAddDialog = true },
            containerColor = TealPrimary,
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("button_add_jadwal_fab")
        ) {
            Icon(imageVector = Icons.Filled.Add, contentDescription = "Tambah Jadwal")
        }
    }

    // Add Jadwal Dialog
    if (showAddDialog) {
        JadwalFormDialog(
            matkulList = matkulList,
            defaultHari = selectedDay,
            onSave = { matkulId, hari, mulai, selesai, ruang, catatan ->
                viewModel.addJadwal(matkulId, hari, mulai, selesai, ruang, catatan)
                showAddDialog = false
            },
            onDismiss = { showAddDialog = false }
        )
    }

    // Edit Jadwal Dialog
    if (editingJadwal != null) {
        JadwalFormDialog(
            matkulList = matkulList,
            initialJadwal = editingJadwal,
            defaultHari = editingJadwal?.hari ?: selectedDay,
            onSave = { matkulId, hari, mulai, selesai, ruang, catatan ->
                val current = editingJadwal ?: return@JadwalFormDialog
                viewModel.updateJadwal(
                    current.copy(
                        mataKuliahId = matkulId,
                        hari = hari,
                        jamMulai = mulai,
                        jamSelesai = selesai,
                        ruang = ruang,
                        catatan = catatan
                    )
                )
                editingJadwal = null
            },
            onDismiss = { editingJadwal = null }
        )
    }

    // Delete Jadwal Confirmation Dialog
    if (deletingJadwal != null) {
        val j = deletingJadwal!!
        val m = matkulList.find { it.id == j.mataKuliahId }
        ConfirmDeleteDialog(
            title = "Hapus Jadwal Kuliah?",
            message = "Jadwal ${m?.nama ?: "Kelas"} hari ${days.find { it.first == j.hari }?.second} (${j.jamMulai} - ${j.jamSelesai}) akan dihapus dari agenda.",
            onConfirm = {
                viewModel.deleteJadwal(j)
                deletingJadwal = null
            },
            onDismiss = { deletingJadwal = null }
        )
    }

    // Bottom Sheet for Inspecting & Managing Course Materials
    if (inspectingMatkulForMateri != null) {
        val matkul = inspectingMatkulForMateri!!
        val materiList by viewModel.getMateriForMatkul(matkul.id).collectAsState(initial = emptyList())

        ModalBottomSheet(
            onDismissRequest = { inspectingMatkulForMateri = null },
            sheetState = rememberModalBottomSheetState(),
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
                    .navigationBarsPadding()
                    .imePadding()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp)
                    ) {
                        Text(
                            text = "Materi Perkuliahan",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = matkul.nama,
                            style = MaterialTheme.typography.bodySmall,
                            color = TealPrimary,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    OutlinedButton(
                        onClick = { showAddMateriDialog = true },
                        modifier = Modifier.defaultMinSize(minHeight = 36.dp)
                    ) {
                        Icon(imageVector = Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Tambah", fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (materiList.isEmpty()) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp)
                    ) {
                        Text(
                            text = "Belum ada berkas atau tautan materi untuk mata kuliah ini.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false)
                    ) {
                        items(materiList, key = { "materi_${it.id}" }) { materi ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (materi.tipeMateri == TipeMateri.DOKUMEN_LOKAL) TealLight
                                                else Color(0xFFFFE4E6)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = if (materi.tipeMateri == TipeMateri.DOKUMEN_LOKAL) Icons.Filled.Description else Icons.Filled.OndemandVideo,
                                            contentDescription = null,
                                            tint = if (materi.tipeMateri == TipeMateri.DOKUMEN_LOKAL) TealPrimary else Color(0xFFE11D48),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable {
                                                openMateriContent(context, materi)
                                            }
                                    ) {
                                        Text(
                                            text = "Pertemuan ${materi.pertemuanKe}: ${materi.judulMateri}",
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.bodyMedium,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = if (materi.tipeMateri == TipeMateri.DOKUMEN_LOKAL) "Buka Berkas Dokumen" else "Tonton Video YouTube",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TealPrimary
                                        )
                                        if (materi.catatanTambahan.isNotBlank()) {
                                            Text(
                                                text = materi.catatanTambahan,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(start = 4.dp)
                                    ) {
                                        IconButton(
                                            onClick = { editingMateri = materi },
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Edit,
                                                contentDescription = "Edit Materi",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        IconButton(
                                            onClick = { deletingMateri = materi },
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Delete,
                                                contentDescription = "Hapus Materi",
                                                tint = MaterialTheme.colorScheme.error,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }

        // Add Materi Form Dialog
        if (showAddMateriDialog) {
            MateriFormDialog(
                mataKuliahId = matkul.id,
                onSave = { pKe, judul, tipe, uriOrUrl, catatan ->
                    viewModel.addMateri(matkul.id, pKe, judul, tipe, uriOrUrl, catatan)
                    showAddMateriDialog = false
                },
                onDismiss = { showAddMateriDialog = false }
            )
        }

        // Edit Materi Form Dialog
        if (editingMateri != null) {
            val m = editingMateri!!
            MateriFormDialog(
                mataKuliahId = matkul.id,
                initialMateri = m,
                onSave = { pKe, judul, tipe, uriOrUrl, catatan ->
                    viewModel.updateMateri(
                        m.copy(
                            pertemuanKe = pKe,
                            judulMateri = judul,
                            tipeMateri = tipe,
                            uriOrUrl = uriOrUrl,
                            catatanTambahan = catatan
                        )
                    )
                    editingMateri = null
                },
                onDismiss = { editingMateri = null }
            )
        }

        // Delete Materi Confirm Dialog
        if (deletingMateri != null) {
            val m = deletingMateri!!
            ConfirmDeleteDialog(
                title = "Hapus Materi?",
                message = "Materi Pertemuan ${m.pertemuanKe} (${m.judulMateri}) akan dihapus.",
                onConfirm = {
                    viewModel.deleteMateri(m)
                    deletingMateri = null
                },
                onDismiss = { deletingMateri = null }
            )
        }
    }
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun JadwalCard(
    jadwal: JadwalKuliahEntity,
    matkul: MataKuliahEntity?,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onOpenMateri: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .width(6.dp)
                            .height(36.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(Color(matkul?.warnaLabel ?: 0xFF0D9488))
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = matkul?.nama ?: "Mata Kuliah",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (matkul?.kode?.isNotBlank() == true || (matkul?.sks ?: 0) > 0) {
                            Text(
                                text = "${matkul?.kode ?: ""} • ${matkul?.sks ?: 3} SKS".trim(' ', '•'),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Row(modifier = Modifier.padding(start = 4.dp)) {
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(imageVector = Icons.Filled.Edit, contentDescription = "Edit", modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(imageVector = Icons.Filled.Delete, contentDescription = "Hapus", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = TealLight
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(imageVector = Icons.Filled.Schedule, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${jadwal.jamMulai} - ${jadwal.jamSelesai}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TealPrimary,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }

                if (jadwal.ruang.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(imageVector = Icons.Filled.LocationOn, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = jadwal.ruang,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            if (matkul?.namaDosen?.isNotBlank() == true) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Filled.Person, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Dosen: ${matkul.namaDosen}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            if (jadwal.catatan.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Catatan: ${jadwal.catatan}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedButton(
                onClick = onOpenMateri,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(imageVector = Icons.AutoMirrored.Filled.MenuBook, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Lihat Materi Perkuliahan", fontSize = 13.sp)
            }
        }
    }
}

private fun openMateriContent(context: android.content.Context, materi: MateriEntity) {
    if (materi.tipeMateri == TipeMateri.DOKUMEN_LOKAL) {
        runCatching {
            val uri = Uri.parse(materi.uriOrUrl)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, context.contentResolver.getType(uri) ?: "*/*")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }.onFailure {
            Toast.makeText(context, "Berkas tidak ditemukan atau tidak dapat dibuka di perangkat ini", Toast.LENGTH_SHORT).show()
        }
    } else {
        runCatching {
            var url = materi.uriOrUrl.trim()
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                url = "https://$url"
            }
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        }.onFailure {
            Toast.makeText(context, "Tidak dapat membuka tautan video", Toast.LENGTH_SHORT).show()
        }
    }
}
