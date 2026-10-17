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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.EventNote
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.data.local.entity.MataKuliahEntity
import com.example.data.local.entity.TugasEntity
import com.example.data.local.entity.UjianEntity
import com.example.data.model.KategoriUjian
import com.example.data.model.PrioritasTugas
import com.example.data.model.StatusTugas
import com.example.ui.components.ConfirmDeleteDialog
import com.example.ui.components.CoolyeahFilterChip
import com.example.ui.components.CoolyeahPriorityBadge
import com.example.ui.components.CoolyeahStatusBadge
import com.example.ui.components.CoolyeahUjianBadge
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.TugasFormDialog
import com.example.ui.components.UjianFormDialog
import com.example.ui.theme.AmberContainer
import com.example.ui.theme.AmberSecondary
import com.example.ui.theme.StatusDone
import com.example.ui.theme.StatusDoneContainer
import com.example.ui.theme.StatusOverdue
import com.example.ui.theme.StatusOverdueContainer
import com.example.ui.theme.TealLight
import com.example.ui.theme.TealPrimary
import com.example.ui.viewmodel.CoolyeahViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun TugasScreen(
    viewModel: CoolyeahViewModel,
    matkulList: List<MataKuliahEntity>,
    tugasList: List<TugasEntity>,
    ujianList: List<UjianEntity>,
    modifier: Modifier = Modifier
) {
    var selectedMainTab by remember { mutableIntStateOf(0) } // 0 = Tugas, 1 = Ujian
    var tugasFilterStatus by remember { mutableStateOf<StatusTugas?>(null) } // null = Semua
    var ujianFilterKategori by remember { mutableStateOf<KategoriUjian?>(null) } // null = Semua

    var showAddTugasDialog by remember { mutableStateOf(false) }
    var editingTugas by remember { mutableStateOf<TugasEntity?>(null) }
    var deletingTugas by remember { mutableStateOf<TugasEntity?>(null) }

    var showAddUjianDialog by remember { mutableStateOf(false) }
    var editingUjian by remember { mutableStateOf<UjianEntity?>(null) }
    var deletingUjian by remember { mutableStateOf<UjianEntity?>(null) }

    val nowMillis = remember { System.currentTimeMillis() }

    val filteredTugas = remember(tugasList, tugasFilterStatus) {
        if (tugasFilterStatus == null) {
            tugasList.sortedWith(compareBy({ it.isSelesai }, { it.deadlineMillis }))
        } else {
            tugasList.filter { it.getStatus(nowMillis) == tugasFilterStatus }
                .sortedBy { it.deadlineMillis }
        }
    }

    val filteredUjian = remember(ujianList, ujianFilterKategori) {
        if (ujianFilterKategori == null) {
            ujianList.sortedBy { it.tanggalMillis }
        } else {
            ujianList.filter { it.kategori == ujianFilterKategori }
                .sortedBy { it.tanggalMillis }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Main TabRow: Tugas Kuliah vs Kuis & Ujian
            TabRow(
                selectedTabIndex = selectedMainTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = TealPrimary,
                modifier = Modifier.testTag("tugas_ujian_tab_row")
            ) {
                Tab(
                    selected = selectedMainTab == 0,
                    onClick = { selectedMainTab = 0 },
                    text = {
                        Text(
                            text = "Tugas Kuliah (${tugasList.size})",
                            fontWeight = if (selectedMainTab == 0) FontWeight.Bold else FontWeight.Medium
                        )
                    },
                    modifier = Modifier.testTag("tab_tugas")
                )
                Tab(
                    selected = selectedMainTab == 1,
                    onClick = { selectedMainTab = 1 },
                    text = {
                        Text(
                            text = "Kuis & Ujian (${ujianList.size})",
                            fontWeight = if (selectedMainTab == 1) FontWeight.Bold else FontWeight.Medium
                        )
                    },
                    modifier = Modifier.testTag("tab_ujian")
                )
            }

            if (selectedMainTab == 0) {
                // Tab 1: Tugas Filters
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth().testTag("tugas_filter_row")
                ) {
                    item {
                        CoolyeahFilterChip(
                            selected = tugasFilterStatus == null,
                            onClick = { tugasFilterStatus = null },
                            label = "Semua",
                            selectedContainerColor = TealPrimary,
                            modifier = Modifier.testTag("filter_tugas_semua")
                        )
                    }
                    items(StatusTugas.values()) { status ->
                        CoolyeahFilterChip(
                            selected = tugasFilterStatus == status,
                            onClick = { tugasFilterStatus = status },
                            label = status.label,
                            selectedContainerColor = when (status) {
                                StatusTugas.BELUM_SELESAI -> AmberSecondary
                                StatusTugas.SELESAI -> StatusDone
                                StatusTugas.TERLEWAT -> StatusOverdue
                            },
                            modifier = Modifier.testTag("filter_tugas_${status.name}")
                        )
                    }
                }

                // Tugas List
                if (filteredTugas.isEmpty()) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 60.dp)
                    ) {
                        EmptyStateCard(
                            icon = Icons.AutoMirrored.Filled.Assignment,
                            title = if (tugasFilterStatus == null) "Belum Ada Tugas Kuliah" else "Tidak Ada Tugas ${tugasFilterStatus?.label}",
                            message = "Catat tugas perkuliahanmu sekarang agar tidak ada deadline yang terlewat.",
                            buttonText = "+ Tambah Tugas Baru",
                            onButtonClick = { showAddTugasDialog = true }
                        )
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("list_tugas")
                    ) {
                        items(filteredTugas, key = { "tugas_${it.id}" }) { tugas ->
                            val matkul = matkulList.find { it.id == tugas.mataKuliahId }
                            TugasCardItem(
                                tugas = tugas,
                                matkul = matkul,
                                onToggleSelesai = { isDone ->
                                    viewModel.setTugasSelesai(tugas, isDone)
                                },
                                onEdit = { editingTugas = tugas },
                                onDelete = { deletingTugas = tugas }
                            )
                        }

                        item { Spacer(modifier = Modifier.height(72.dp)) }
                    }
                }
            } else {
                // Tab 2: Ujian Filters (KUIS, UTS, UAS)
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth().testTag("ujian_filter_row")
                ) {
                    item {
                        CoolyeahFilterChip(
                            selected = ujianFilterKategori == null,
                            onClick = { ujianFilterKategori = null },
                            label = "Semua",
                            selectedContainerColor = TealPrimary,
                            modifier = Modifier.testTag("filter_ujian_semua")
                        )
                    }
                    items(KategoriUjian.values()) { kat ->
                        CoolyeahFilterChip(
                            selected = ujianFilterKategori == kat,
                            onClick = { ujianFilterKategori = kat },
                            label = kat.label,
                            selectedContainerColor = when (kat) {
                                KategoriUjian.KUIS -> AmberSecondary
                                KategoriUjian.UTS -> TealPrimary
                                KategoriUjian.UAS -> Color(0xFF6366F1)
                            },
                            modifier = Modifier.testTag("filter_ujian_${kat.name}")
                        )
                    }
                }

                // Ujian List
                if (filteredUjian.isEmpty()) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 60.dp)
                    ) {
                        EmptyStateCard(
                            icon = Icons.AutoMirrored.Filled.EventNote,
                            title = if (ujianFilterKategori == null) "Belum Ada Jadwal Ujian" else "Belum Ada Jadwal ${ujianFilterKategori?.label}",
                            message = "Jadwalkan kuis mingguan, UTS, atau UAS agar persiapan belajarmu lebih matang.",
                            buttonText = "+ Tambah Jadwal Ujian",
                            onButtonClick = { showAddUjianDialog = true }
                        )
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("list_ujian")
                    ) {
                        items(filteredUjian, key = { "ujian_${it.id}" }) { ujian ->
                            val matkul = matkulList.find { it.id == ujian.mataKuliahId }
                            UjianCardItem(
                                ujian = ujian,
                                matkul = matkul,
                                onEdit = { editingUjian = ujian },
                                onDelete = { deletingUjian = ujian }
                            )
                        }

                        item { Spacer(modifier = Modifier.height(72.dp)) }
                    }
                }
            }
        }

        // Action FAB (Add Tugas or Add Ujian depending on tab)
        FloatingActionButton(
            onClick = {
                if (selectedMainTab == 0) showAddTugasDialog = true
                else showAddUjianDialog = true
            },
            containerColor = TealPrimary,
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("button_add_tugas_ujian_fab")
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = if (selectedMainTab == 0) "Tambah Tugas" else "Tambah Ujian"
            )
        }
    }

    // Dialog Tambah Tugas
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

    // Dialog Edit Tugas
    if (editingTugas != null) {
        TugasFormDialog(
            matkulList = matkulList,
            initialTugas = editingTugas,
            onSave = { matkulId, judul, deskripsi, deadline, prioritas ->
                val curr = editingTugas ?: return@TugasFormDialog
                viewModel.updateTugas(
                    curr.copy(
                        mataKuliahId = matkulId,
                        judul = judul,
                        deskripsi = deskripsi,
                        deadlineMillis = deadline,
                        prioritas = prioritas
                    )
                )
                editingTugas = null
            },
            onDismiss = { editingTugas = null }
        )
    }

    // Dialog Hapus Tugas
    if (deletingTugas != null) {
        val t = deletingTugas!!
        ConfirmDeleteDialog(
            title = "Hapus Tugas?",
            message = "Tugas \"${t.judul}\" akan dihapus secara permanen.",
            onConfirm = {
                viewModel.deleteTugas(t)
                deletingTugas = null
            },
            onDismiss = { deletingTugas = null }
        )
    }

    // Dialog Tambah Ujian
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

    // Dialog Edit Ujian
    if (editingUjian != null) {
        UjianFormDialog(
            matkulList = matkulList,
            initialUjian = editingUjian,
            onSave = { matkulId, kategori, tgl, mulai, selesai, ruang, catatan ->
                val curr = editingUjian ?: return@UjianFormDialog
                viewModel.updateUjian(
                    curr.copy(
                        mataKuliahId = matkulId,
                        kategori = kategori,
                        tanggalMillis = tgl,
                        jamMulai = mulai,
                        jamSelesai = selesai,
                        ruang = ruang,
                        catatan = catatan
                    )
                )
                editingUjian = null
            },
            onDismiss = { editingUjian = null }
        )
    }

    // Dialog Hapus Ujian
    if (deletingUjian != null) {
        val u = deletingUjian!!
        val matkul = matkulList.find { it.id == u.mataKuliahId }
        ConfirmDeleteDialog(
            title = "Hapus Jadwal Ujian?",
            message = "Agenda ${u.kategori.label} untuk mata kuliah ${matkul?.nama ?: ""} akan dihapus.",
            onConfirm = {
                viewModel.deleteUjian(u)
                deletingUjian = null
            },
            onDismiss = { deletingUjian = null }
        )
    }
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun TugasCardItem(
    tugas: TugasEntity,
    matkul: MataKuliahEntity?,
    onToggleSelesai: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val sdf = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.forLanguageTag("id-ID")) }
    val status = tugas.getStatus()

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Checkbox, Titles & Info, Edit & Delete actions
            Row(
                verticalAlignment = Alignment.Top,
                modifier = Modifier.fillMaxWidth()
            ) {
                Checkbox(
                    checked = tugas.isSelesai,
                    onCheckedChange = onToggleSelesai,
                    colors = CheckboxDefaults.colors(checkedColor = StatusDone),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(top = 1.dp)
                ) {
                    Text(
                        text = tugas.judul,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            textDecoration = if (tugas.isSelesai) TextDecoration.LineThrough else TextDecoration.None
                        ),
                        color = if (tugas.isSelesai) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = matkul?.nama ?: "Mata Kuliah",
                        style = MaterialTheme.typography.bodySmall,
                        color = TealPrimary,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (tugas.deskripsi.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = tugas.deskripsi,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 4.dp)
                ) {
                    IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = "Edit",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Hapus",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Footer: Deadline and Badges (Priority & Status)
            // FlowRow ensures on narrow screens (<360dp) Badges cleanly flow into their own line,
            // while preserving single-line text and consistent badge dimensions.
            FlowRow(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 34.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.CalendarToday,
                        contentDescription = null,
                        tint = if (status == StatusTugas.TERLEWAT) StatusOverdue else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = sdf.format(tugas.deadlineMillis),
                        style = MaterialTheme.typography.bodySmall,
                        color = if (status == StatusTugas.TERLEWAT) StatusOverdue else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = if (status == StatusTugas.TERLEWAT) FontWeight.Bold else FontWeight.Normal,
                        maxLines = 1,
                        softWrap = false
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CoolyeahPriorityBadge(prioritas = tugas.prioritas)
                    CoolyeahStatusBadge(status = status)
                }
            }
        }
    }
}

@Composable
private fun UjianCardItem(
    ujian: UjianEntity,
    matkul: MataKuliahEntity?,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val sdfDate = remember { SimpleDateFormat("EEEE, dd MMMM yyyy", Locale("id", "ID")) }

    Card(
        shape = RoundedCornerShape(16.dp),
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
                    CoolyeahUjianBadge(kategori = ujian.kategori)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = matkul?.nama ?: "Mata Kuliah",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 4.dp)
                ) {
                    IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = "Edit",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Hapus",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.CalendarToday,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = sdfDate.format(ujian.tanggalMillis),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            if (ujian.jamMulai.isNotBlank() || ujian.jamSelesai.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Schedule,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${ujian.jamMulai} - ${ujian.jamSelesai}".trim(' ', '-'),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (ujian.ruang.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Ruang / Platform: ${ujian.ruang}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (ujian.catatan.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Materi Ujian: ${ujian.catatan}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
