package com.example

import android.content.Context
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.entity.MataKuliahEntity
import com.example.data.local.entity.TugasEntity
import com.example.data.local.entity.UjianEntity
import com.example.data.model.KategoriUjian
import com.example.data.model.PrioritasTugas
import com.example.data.model.StatusTugas
import com.example.ui.components.CoolyeahBottomBar
import com.example.ui.components.CoolyeahFilterChip
import com.example.ui.components.CoolyeahPriorityBadge
import com.example.ui.components.CoolyeahStatusBadge
import com.example.ui.components.CoolyeahUjianBadge
import com.example.ui.navigation.NavigationItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class UiLayoutAuditTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext<Context>()
    }

    @Test
    fun testNavigationItemLabelsAreSingleWordAndConcise() {
        // Navigation item "Tugas" must be a concise label to prevent two-line wrapping
        val items = NavigationItem.items
        assertEquals(4, items.size)
        assertEquals("Home", NavigationItem.Home.title)
        assertEquals("Jadwal", NavigationItem.Jadwal.title)
        assertEquals("Tugas", NavigationItem.Tugas.title)
        assertEquals("Profil", NavigationItem.Profil.title)

        // Ensure routes remain consistent
        assertEquals("home", NavigationItem.Home.route)
        assertEquals("jadwal", NavigationItem.Jadwal.route)
        assertEquals("tugas", NavigationItem.Tugas.route)
        assertEquals("profil", NavigationItem.Profil.route)
    }

    @Test
    fun testCoolyeahBottomBarRendersCleanly() {
        var navigatedRoute = ""
        composeTestRule.setContent {
            MaterialTheme {
                CoolyeahBottomBar(
                    currentRoute = "tugas",
                    onNavigateToRoute = { navigatedRoute = it }
                )
            }
        }

        composeTestRule.onNodeWithText("Tugas").assertIsDisplayed()
        composeTestRule.onNodeWithText("Home").assertIsDisplayed()
        composeTestRule.onNodeWithText("Jadwal").assertIsDisplayed()
        composeTestRule.onNodeWithText("Profil").assertIsDisplayed()

        // Click on Jadwal
        composeTestRule.onNodeWithText("Jadwal").performClick()
        assertEquals("jadwal", navigatedRoute)
    }

    @Test
    fun testStatusBadgesRenderExactLabelsWithoutWrapping() {
        composeTestRule.setContent {
            MaterialTheme {
                androidx.compose.foundation.layout.Column {
                    CoolyeahStatusBadge(status = StatusTugas.BELUM_SELESAI)
                    CoolyeahStatusBadge(status = StatusTugas.SELESAI)
                    CoolyeahStatusBadge(status = StatusTugas.TERLEWAT)
                }
            }
        }

        composeTestRule.onNodeWithText("Belum Selesai").assertIsDisplayed()
        composeTestRule.onNodeWithText("Selesai").assertIsDisplayed()
        composeTestRule.onNodeWithText("Terlewat").assertIsDisplayed()
    }

    @Test
    fun testPriorityBadgesRenderExactLabels() {
        composeTestRule.setContent {
            MaterialTheme {
                androidx.compose.foundation.layout.Column {
                    CoolyeahPriorityBadge(prioritas = PrioritasTugas.TINGGI)
                    CoolyeahPriorityBadge(prioritas = PrioritasTugas.SEDANG)
                    CoolyeahPriorityBadge(prioritas = PrioritasTugas.RENDAH)
                }
            }
        }

        composeTestRule.onNodeWithText("Tinggi").assertIsDisplayed()
        composeTestRule.onNodeWithText("Sedang").assertIsDisplayed()
        composeTestRule.onNodeWithText("Rendah").assertIsDisplayed()
    }

    @Test
    fun testUjianBadgesRenderExactLabels() {
        composeTestRule.setContent {
            MaterialTheme {
                androidx.compose.foundation.layout.Column {
                    CoolyeahUjianBadge(kategori = KategoriUjian.KUIS)
                    CoolyeahUjianBadge(kategori = KategoriUjian.UTS)
                    CoolyeahUjianBadge(kategori = KategoriUjian.UAS)
                }
            }
        }

        composeTestRule.onNodeWithText("Kuis").assertIsDisplayed()
        composeTestRule.onNodeWithText("UTS").assertIsDisplayed()
        composeTestRule.onNodeWithText("UAS").assertIsDisplayed()
    }

    @Test
    fun testReusableFilterChipSelectionAndClick() {
        var clicked = false
        composeTestRule.setContent {
            MaterialTheme {
                CoolyeahFilterChip(
                    selected = true,
                    onClick = { clicked = true },
                    label = "Belum Selesai"
                )
            }
        }

        val chipNode = composeTestRule.onNodeWithText("Belum Selesai")
        chipNode.assertIsDisplayed()
        chipNode.performClick()
        assertTrue(clicked)
    }

    @Test
    fun testTaskStatusCalculationsPreserved() {
        val now = System.currentTimeMillis()
        val overdueTask = TugasEntity(
            id = 1,
            mataKuliahId = 10,
            judul = "Tugas Arsitektur Komputer",
            deadlineMillis = now - 60000L,
            isSelesai = false
        )
        assertEquals(StatusTugas.TERLEWAT, overdueTask.getStatus(now))

        val pendingTask = TugasEntity(
            id = 2,
            mataKuliahId = 10,
            judul = "Tugas Praktikum Algoritma",
            deadlineMillis = now + 60000L,
            isSelesai = false
        )
        assertEquals(StatusTugas.BELUM_SELESAI, pendingTask.getStatus(now))

        val completedTask = TugasEntity(
            id = 3,
            mataKuliahId = 10,
            judul = "Laporan Selesai",
            deadlineMillis = now - 60000L,
            isSelesai = true
        )
        assertEquals(StatusTugas.SELESAI, completedTask.getStatus(now))
    }
}
