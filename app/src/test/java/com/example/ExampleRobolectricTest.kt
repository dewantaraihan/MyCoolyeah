package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.entity.JadwalKuliahEntity
import com.example.data.local.entity.MataKuliahEntity
import com.example.data.local.entity.MateriEntity
import com.example.data.local.entity.ProfileEntity
import com.example.data.local.entity.SemesterEntity
import com.example.data.local.entity.TugasEntity
import com.example.data.local.entity.UjianEntity
import com.example.data.model.KategoriUjian
import com.example.data.model.PrioritasTugas
import com.example.data.model.StatusTugas
import com.example.data.model.TipeMateri
import com.example.data.repository.AcademicRepositoryImpl
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    private lateinit var context: Context
    private lateinit var db: AppDatabase
    private lateinit var repository: AcademicRepositoryImpl

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = AcademicRepositoryImpl(db)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun verifyAppName() {
        val appName = context.getString(R.string.app_name)
        assertEquals("MyCoolyeah", appName)
    }

    @Test
    fun testMainActivityLaunchesSuccessfully() {
        val controller = org.robolectric.Robolectric.buildActivity(MainActivity::class.java).setup()
        val activity = controller.get()
        assertNotNull(activity)
    }

    @Test
    fun testProfileInsertAndRead() = runBlocking {
        val profile = ProfileEntity(
            id = 1,
            nama = "Raihan",
            nim = "220101",
            universitas = "Institut Teknologi",
            jurusan = "Teknik Informatika",
            angkatan = "2022"
        )
        repository.saveProfile(profile)

        val retrieved = repository.getProfile().first()
        assertNotNull(retrieved)
        assertEquals("Raihan", retrieved?.nama)
        assertEquals("Teknik Informatika", retrieved?.jurusan)
    }

    @Test
    fun testSingleSourceOfTruthActiveSemesterTransaction() = runBlocking {
        // Insert two semesters
        val sem1Id = repository.insertSemester(
            SemesterEntity(namaSemester = "Semester 1", tahunAjaran = "2024/2025 Ganjil", isActive = true)
        )
        val sem2Id = repository.insertSemester(
            SemesterEntity(namaSemester = "Semester 2", tahunAjaran = "2024/2025 Genap", isActive = false)
        )

        // Verify initial state
        var active = repository.getActiveSemester().first()
        assertEquals(sem1Id, active?.id)

        // Switch to semester 2
        val success = repository.setActiveSemester(sem2Id)
        assertTrue(success)

        active = repository.getActiveSemester().first()
        assertEquals(sem2Id, active?.id)

        val all = repository.getAllSemesters().first()
        val sem1 = all.find { it.id == sem1Id }
        val sem2 = all.find { it.id == sem2Id }
        assertFalse(sem1!!.isActive)
        assertTrue(sem2!!.isActive)

        // Test invalid semester ID: must return false and preserve sem2 as active
        val invalidSuccess = repository.setActiveSemester(9999L)
        assertFalse(invalidSuccess)
        active = repository.getActiveSemester().first()
        assertEquals(sem2Id, active?.id)
    }

    @Test
    fun testCascadeDeleteSemesterToAllChildren() = runBlocking {
        val semId = repository.insertSemester(
            SemesterEntity(namaSemester = "Semester 3", tahunAjaran = "2025/2026 Ganjil", isActive = true)
        )
        val matkulId = repository.insertMataKuliah(
            MataKuliahEntity(semesterId = semId, nama = "Algoritma & Struktur Data", sks = 4)
        )
        repository.insertJadwal(
            JadwalKuliahEntity(mataKuliahId = matkulId, hari = 1, jamMulai = "08:00", jamSelesai = "10:30")
        )
        repository.insertTugas(
            TugasEntity(
                mataKuliahId = matkulId,
                judul = "Tugas Binary Tree",
                deadlineMillis = System.currentTimeMillis() + 100000L
            )
        )
        repository.insertUjian(
            UjianEntity(
                mataKuliahId = matkulId,
                kategori = KategoriUjian.UTS,
                tanggalMillis = System.currentTimeMillis() + 500000L
            )
        )
        repository.insertMateri(
            MateriEntity(
                mataKuliahId = matkulId,
                pertemuanKe = 1,
                judulMateri = "Pengenalan Tree",
                tipeMateri = TipeMateri.DOKUMEN_LOKAL,
                uriOrUrl = "content://sample/tree.pdf"
            )
        )

        // Verify impact check before deletion
        val impact = repository.getSemesterDeletionImpact(semId)
        assertEquals(1, impact.matkulCount)
        assertEquals(1, impact.jadwalCount)
        assertEquals(1, impact.tugasCount)
        assertEquals(1, impact.ujianCount)
        assertEquals(1, impact.materiCount)

        // Delete Semester
        val semesterEntity = repository.getAllSemesters().first().first { it.id == semId }
        repository.deleteSemester(semesterEntity)

        // Verify CASCADE deletion
        val matkulRemaining = repository.getMataKuliahBySemester(semId).first()
        assertTrue(matkulRemaining.isEmpty())

        val jadwalRemaining = repository.getJadwalBySemester(semId).first()
        assertTrue(jadwalRemaining.isEmpty())

        val tugasRemaining = repository.getTugasBySemester(semId).first()
        assertTrue(tugasRemaining.isEmpty())

        val ujianRemaining = repository.getUjianBySemester(semId).first()
        assertTrue(ujianRemaining.isEmpty())

        val materiRemaining = repository.getMateriByMataKuliah(matkulId).first()
        assertTrue(materiRemaining.isEmpty())
    }

    @Test
    fun testTaskStatusCalculation() {
        val now = System.currentTimeMillis()

        val taskSelesai = TugasEntity(
            mataKuliahId = 1,
            judul = "Selesai",
            deadlineMillis = now - 50000L,
            isSelesai = true
        )
        assertEquals(StatusTugas.SELESAI, taskSelesai.getStatus(now))

        val taskBelum = TugasEntity(
            mataKuliahId = 1,
            judul = "Belum",
            deadlineMillis = now + 50000L,
            isSelesai = false
        )
        assertEquals(StatusTugas.BELUM_SELESAI, taskBelum.getStatus(now))

        val taskTerlewat = TugasEntity(
            mataKuliahId = 1,
            judul = "Terlewat",
            deadlineMillis = now - 50000L,
            isSelesai = false
        )
        assertEquals(StatusTugas.TERLEWAT, taskTerlewat.getStatus(now))
    }

    @Test
    fun testCascadeDeleteMataKuliah() = runBlocking {
        val semId = repository.insertSemester(
            SemesterEntity(namaSemester = "Semester 4", tahunAjaran = "2025/2026 Genap", isActive = false)
        )
        val matkulId = repository.insertMataKuliah(
            MataKuliahEntity(semesterId = semId, nama = "Basis Data Lanjut", sks = 3)
        )
        repository.insertJadwal(
            JadwalKuliahEntity(mataKuliahId = matkulId, hari = 2, jamMulai = "10:00", jamSelesai = "12:00")
        )
        repository.insertTugas(
            TugasEntity(mataKuliahId = matkulId, judul = "ERD Database", deadlineMillis = System.currentTimeMillis() + 200000L)
        )
        repository.insertUjian(
            UjianEntity(mataKuliahId = matkulId, kategori = KategoriUjian.KUIS, tanggalMillis = System.currentTimeMillis() + 100000L)
        )
        repository.insertMateri(
            MateriEntity(mataKuliahId = matkulId, pertemuanKe = 2, judulMateri = "Normalization", tipeMateri = TipeMateri.YOUTUBE, uriOrUrl = "https://youtube.com/sample")
        )

        val impact = repository.getMataKuliahDeletionImpact(matkulId)
        assertEquals(1, impact.jadwalCount)
        assertEquals(1, impact.tugasCount)
        assertEquals(1, impact.ujianCount)
        assertEquals(1, impact.materiCount)

        val matkulEntity = repository.getMataKuliahBySemester(semId).first().first { it.id == matkulId }
        repository.deleteMataKuliah(matkulEntity)

        assertTrue(repository.getJadwalBySemester(semId).first().isEmpty())
        assertTrue(repository.getMateriByMataKuliah(matkulId).first().isEmpty())
    }

    @Test
    fun testNotificationSchedulerAndChannels() {
        com.example.notification.NotificationHelper.createNotificationChannels(context)
        com.example.notification.CoolyeahAlarmScheduler.scheduleDailyScheduleAlarm(context)
        com.example.notification.CoolyeahAlarmScheduler.scheduleTaskReminder(context, 101L, "Tugas Riset", System.currentTimeMillis() + 86400000L * 3)
        com.example.notification.CoolyeahAlarmScheduler.cancelTaskReminder(context, 101L)
        com.example.notification.CoolyeahAlarmScheduler.scheduleExamReminder(context, 202L, "KUIS Matematika", System.currentTimeMillis() + 86400000L * 5)
        com.example.notification.CoolyeahAlarmScheduler.cancelExamReminder(context, 202L)
    }
}
