package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.KategoriUjian
import com.example.data.model.PrioritasTugas
import com.example.data.model.StatusTugas
import com.example.ui.theme.AmberContainer
import com.example.ui.theme.AmberSecondary
import com.example.ui.theme.StatusDone
import com.example.ui.theme.StatusDoneContainer
import com.example.ui.theme.StatusOverdue
import com.example.ui.theme.StatusOverdueContainer
import com.example.ui.theme.TealLight
import com.example.ui.theme.TealPrimary

/**
 * Reusable badge component that enforces consistent height, typography, shape,
 * and single-line rendering across all screen sizes and font scales.
 */
@Composable
fun CoolyeahTagBadge(
    text: String,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(containerColor)
            .defaultMinSize(minHeight = 24.dp)
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
            color = contentColor,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Clip
        )
    }
}

@Composable
fun CoolyeahStatusBadge(
    status: StatusTugas,
    modifier: Modifier = Modifier
) {
    val containerColor = when (status) {
        StatusTugas.SELESAI -> StatusDoneContainer
        StatusTugas.TERLEWAT -> StatusOverdueContainer
        StatusTugas.BELUM_SELESAI -> AmberContainer
    }
    val contentColor = when (status) {
        StatusTugas.SELESAI -> StatusDone
        StatusTugas.TERLEWAT -> StatusOverdue
        StatusTugas.BELUM_SELESAI -> AmberSecondary
    }

    CoolyeahTagBadge(
        text = status.label,
        containerColor = containerColor,
        contentColor = contentColor,
        modifier = modifier
    )
}

@Composable
fun CoolyeahPriorityBadge(
    prioritas: PrioritasTugas,
    modifier: Modifier = Modifier
) {
    val containerColor = when (prioritas) {
        PrioritasTugas.TINGGI -> StatusOverdueContainer
        PrioritasTugas.SEDANG -> AmberContainer
        PrioritasTugas.RENDAH -> TealLight
    }
    val contentColor = when (prioritas) {
        PrioritasTugas.TINGGI -> StatusOverdue
        PrioritasTugas.SEDANG -> AmberSecondary
        PrioritasTugas.RENDAH -> TealPrimary
    }

    CoolyeahTagBadge(
        text = prioritas.label,
        containerColor = containerColor,
        contentColor = contentColor,
        modifier = modifier
    )
}

@Composable
fun CoolyeahUjianBadge(
    kategori: KategoriUjian,
    modifier: Modifier = Modifier
) {
    val containerColor = when (kategori) {
        KategoriUjian.KUIS -> AmberSecondary
        KategoriUjian.UTS -> TealPrimary
        KategoriUjian.UAS -> Color(0xFF6366F1)
    }

    CoolyeahTagBadge(
        text = kategori.label,
        containerColor = containerColor,
        contentColor = Color.White,
        modifier = modifier
    )
}
