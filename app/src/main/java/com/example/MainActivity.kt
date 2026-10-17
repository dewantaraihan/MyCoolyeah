package com.example

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.notification.CoolyeahAlarmScheduler
import com.example.notification.NotificationHelper
import com.example.ui.components.CoolyeahBottomBar
import com.example.ui.components.CoolyeahTopBar
import com.example.ui.navigation.NavigationItem
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.JadwalScreen
import com.example.ui.screens.ProfilScreen
import com.example.ui.screens.TugasScreen
import com.example.ui.theme.MyCoolyeahTheme
import com.example.ui.viewmodel.CoolyeahViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Setup local notification channels and 00:00 schedule trigger
        NotificationHelper.createNotificationChannels(this)
        CoolyeahAlarmScheduler.scheduleDailyScheduleAlarm(this)

        setContent {
            MyCoolyeahTheme {
                MyCoolyeahApp()
            }
        }
    }
}

@Composable
fun MyCoolyeahApp(
    viewModel: CoolyeahViewModel = viewModel()
) {
    // Request notification permission on Android 13+
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val permissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { _ -> }

        LaunchedEffect(Unit) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    var currentRoute by remember { mutableStateOf(NavigationItem.Home.route) }

    // Intercept back button to return to Home screen when on other tabs
    BackHandler(enabled = currentRoute != NavigationItem.Home.route) {
        currentRoute = NavigationItem.Home.route
    }

    // Collect reactive state flows
    val profile by viewModel.profile.collectAsStateWithLifecycle()
    val semesters by viewModel.allSemesters.collectAsStateWithLifecycle()
    val activeSemester by viewModel.activeSemester.collectAsStateWithLifecycle()
    val selectedFilterId by viewModel.selectedSemesterFilterId.collectAsStateWithLifecycle()
    val currentSemesterId by viewModel.currentSemesterId.collectAsStateWithLifecycle()
    val matkulList by viewModel.currentMataKuliahList.collectAsStateWithLifecycle()
    val jadwalList by viewModel.currentJadwalList.collectAsStateWithLifecycle()
    val tugasList by viewModel.currentTugasList.collectAsStateWithLifecycle()
    val ujianList by viewModel.currentUjianList.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            CoolyeahTopBar(
                semesters = semesters,
                selectedSemesterId = currentSemesterId,
                onSemesterSelected = { semId ->
                    viewModel.setSemesterFilter(semId)
                }
            )
        },
        bottomBar = {
            CoolyeahBottomBar(
                currentRoute = currentRoute,
                onNavigateToRoute = { route ->
                    currentRoute = route
                }
            )
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        val screenModifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)

        when (currentRoute) {
            NavigationItem.Home.route -> {
                HomeScreen(
                    viewModel = viewModel,
                    profile = profile,
                    semesters = semesters,
                    activeSemester = activeSemester,
                    currentSemesterId = currentSemesterId,
                    matkulList = matkulList,
                    jadwalList = jadwalList,
                    tugasList = tugasList,
                    onNavigateToTab = { targetTab ->
                        currentRoute = targetTab
                    },
                    modifier = screenModifier
                )
            }
            NavigationItem.Jadwal.route -> {
                JadwalScreen(
                    viewModel = viewModel,
                    matkulList = matkulList,
                    jadwalList = jadwalList,
                    modifier = screenModifier
                )
            }
            NavigationItem.Tugas.route -> {
                TugasScreen(
                    viewModel = viewModel,
                    matkulList = matkulList,
                    tugasList = tugasList,
                    ujianList = ujianList,
                    modifier = screenModifier
                )
            }
            NavigationItem.Profil.route -> {
                ProfilScreen(
                    viewModel = viewModel,
                    profile = profile,
                    semesters = semesters,
                    activeSemester = activeSemester,
                    currentSemesterId = currentSemesterId,
                    matkulList = matkulList,
                    tugasList = tugasList,
                    modifier = screenModifier
                )
            }
        }
    }
}
