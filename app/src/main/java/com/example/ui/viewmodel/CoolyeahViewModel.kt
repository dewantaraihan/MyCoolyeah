package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
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
import com.example.data.model.TipeMateri
import com.example.data.repository.AcademicRepository
import com.example.data.repository.AcademicRepositoryImpl
import com.example.data.repository.MataKuliahDeletionImpact
import com.example.data.repository.SemesterDeletionImpact
import com.example.notification.CoolyeahAlarmScheduler
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class CoolyeahViewModel @JvmOverloads constructor(
    application: Application,
    private val repository: AcademicRepository = AcademicRepositoryImpl(AppDatabase.getDatabase(application))
) : AndroidViewModel(application) {

    val profile: StateFlow<ProfileEntity?> = repository.getProfile()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allSemesters: StateFlow<List<SemesterEntity>> = repository.getAllSemesters()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeSemester: StateFlow<SemesterEntity?> = repository.getActiveSemester()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // View-only semester filter for TopBar
    private val _selectedSemesterFilterId = MutableStateFlow<Long?>(null)
    val selectedSemesterFilterId: StateFlow<Long?> = _selectedSemesterFilterId.asStateFlow()

    fun setSemesterFilter(semesterId: Long) {
        _selectedSemesterFilterId.value = semesterId
    }

    // Current effective semester ID (either filtered or active or first available)
    val currentSemesterId: StateFlow<Long?> = _selectedSemesterFilterId.flatMapLatest { filterId ->
        if (filterId != null) {
            flowOf(filterId)
        } else {
            activeSemester.flatMapLatest { active ->
                if (active != null) flowOf(active.id)
                else allSemesters.flatMapLatest { sList -> flowOf(sList.firstOrNull()?.id) }
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Reactive Mata Kuliah for current effective semester
    val currentMataKuliahList: StateFlow<List<MataKuliahEntity>> = currentSemesterId.flatMapLatest { semId ->
        if (semId != null) repository.getMataKuliahBySemester(semId)
        else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Reactive Jadwal for current effective semester
    val currentJadwalList: StateFlow<List<JadwalKuliahEntity>> = currentSemesterId.flatMapLatest { semId ->
        if (semId != null) repository.getJadwalBySemester(semId)
        else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Reactive Tugas for current effective semester
    val currentTugasList: StateFlow<List<TugasEntity>> = currentSemesterId.flatMapLatest { semId ->
        if (semId != null) repository.getTugasBySemester(semId)
        else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Reactive Ujian for current effective semester
    val currentUjianList: StateFlow<List<UjianEntity>> = currentSemesterId.flatMapLatest { semId ->
        if (semId != null) repository.getUjianBySemester(semId)
        else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Profile actions
    fun updateProfile(nama: String, nim: String, universitas: String, jurusan: String, angkatan: String) {
        viewModelScope.launch {
            repository.saveProfile(
                ProfileEntity(
                    id = 1,
                    nama = nama,
                    nim = nim,
                    universitas = universitas,
                    jurusan = jurusan,
                    angkatan = angkatan,
                    fotoUri = profile.value?.fotoUri
                )
            )
        }
    }

    // Semester actions
    fun addSemester(namaSemester: String, tahunAjaran: String, isActive: Boolean) {
        viewModelScope.launch {
            val isFirst = allSemesters.value.isEmpty()
            val newId = repository.insertSemester(
                SemesterEntity(
                    namaSemester = namaSemester,
                    tahunAjaran = tahunAjaran,
                    isActive = isActive || isFirst
                )
            )
            if (isActive || isFirst) {
                repository.setActiveSemester(newId)
                _selectedSemesterFilterId.value = newId
            }
        }
    }

    fun updateSemester(semester: SemesterEntity) {
        viewModelScope.launch {
            repository.updateSemester(semester)
        }
    }

    fun setActiveSemester(semesterId: Long) {
        viewModelScope.launch {
            val success = repository.setActiveSemester(semesterId)
            if (success) {
                _selectedSemesterFilterId.value = semesterId
            }
        }
    }

    fun deleteSemester(semester: SemesterEntity) {
        viewModelScope.launch {
            repository.deleteSemester(semester)
            if (_selectedSemesterFilterId.value == semester.id) {
                _selectedSemesterFilterId.value = null
            }
        }
    }

    suspend fun getSemesterDeletionImpact(semesterId: Long): SemesterDeletionImpact {
        return repository.getSemesterDeletionImpact(semesterId)
    }

    // Mata Kuliah actions
    fun addMataKuliah(semesterId: Long, nama: String, kode: String, sks: Int, namaDosen: String, ruang: String, warnaLabel: Long) {
        viewModelScope.launch {
            repository.insertMataKuliah(
                MataKuliahEntity(
                    semesterId = semesterId,
                    nama = nama,
                    kode = kode,
                    sks = sks,
                    namaDosen = namaDosen,
                    ruang = ruang,
                    warnaLabel = warnaLabel
                )
            )
        }
    }

    fun updateMataKuliah(mataKuliah: MataKuliahEntity) {
        viewModelScope.launch {
            repository.updateMataKuliah(mataKuliah)
        }
    }

    fun deleteMataKuliah(mataKuliah: MataKuliahEntity) {
        viewModelScope.launch {
            repository.deleteMataKuliah(mataKuliah)
        }
    }

    suspend fun getMataKuliahDeletionImpact(mataKuliahId: Long): MataKuliahDeletionImpact {
        return repository.getMataKuliahDeletionImpact(mataKuliahId)
    }

    // Jadwal Kuliah actions
    fun addJadwal(mataKuliahId: Long, hari: Int, jamMulai: String, jamSelesai: String, ruang: String, catatan: String) {
        viewModelScope.launch {
            repository.insertJadwal(
                JadwalKuliahEntity(
                    mataKuliahId = mataKuliahId,
                    hari = hari,
                    jamMulai = jamMulai,
                    jamSelesai = jamSelesai,
                    ruang = ruang,
                    catatan = catatan
                )
            )
        }
    }

    fun updateJadwal(jadwal: JadwalKuliahEntity) {
        viewModelScope.launch {
            repository.updateJadwal(jadwal)
        }
    }

    fun deleteJadwal(jadwal: JadwalKuliahEntity) {
        viewModelScope.launch {
            repository.deleteJadwal(jadwal)
        }
    }

    // Tugas actions
    fun addTugas(mataKuliahId: Long, judul: String, deskripsi: String, deadlineMillis: Long, prioritas: PrioritasTugas) {
        viewModelScope.launch {
            val id = repository.insertTugas(
                TugasEntity(
                    mataKuliahId = mataKuliahId,
                    judul = judul,
                    deskripsi = deskripsi,
                    deadlineMillis = deadlineMillis,
                    prioritas = prioritas,
                    isSelesai = false
                )
            )
            CoolyeahAlarmScheduler.scheduleTaskReminder(
                getApplication(),
                id,
                judul,
                deadlineMillis
            )
        }
    }

    fun setTugasSelesai(tugas: TugasEntity, isSelesai: Boolean) {
        viewModelScope.launch {
            repository.setTugasSelesai(tugas.id, isSelesai)
            if (isSelesai) {
                CoolyeahAlarmScheduler.cancelTaskReminder(getApplication(), tugas.id)
            } else {
                CoolyeahAlarmScheduler.scheduleTaskReminder(
                    getApplication(),
                    tugas.id,
                    tugas.judul,
                    tugas.deadlineMillis
                )
            }
        }
    }

    fun updateTugas(tugas: TugasEntity) {
        viewModelScope.launch {
            repository.updateTugas(tugas)
            if (!tugas.isSelesai) {
                CoolyeahAlarmScheduler.scheduleTaskReminder(
                    getApplication(),
                    tugas.id,
                    tugas.judul,
                    tugas.deadlineMillis
                )
            } else {
                CoolyeahAlarmScheduler.cancelTaskReminder(getApplication(), tugas.id)
            }
        }
    }

    fun deleteTugas(tugas: TugasEntity) {
        viewModelScope.launch {
            repository.deleteTugas(tugas)
            CoolyeahAlarmScheduler.cancelTaskReminder(getApplication(), tugas.id)
        }
    }

    // Ujian actions
    fun addUjian(mataKuliahId: Long, kategori: KategoriUjian, tanggalMillis: Long, jamMulai: String, jamSelesai: String, ruang: String, catatan: String) {
        viewModelScope.launch {
            val id = repository.insertUjian(
                UjianEntity(
                    mataKuliahId = mataKuliahId,
                    kategori = kategori,
                    tanggalMillis = tanggalMillis,
                    jamMulai = jamMulai,
                    jamSelesai = jamSelesai,
                    ruang = ruang,
                    catatan = catatan
                )
            )
            val matkul = currentMataKuliahList.value.find { it.id == mataKuliahId }
            val title = "${kategori.label} ${matkul?.nama ?: ""}".trim()
            CoolyeahAlarmScheduler.scheduleExamReminder(
                getApplication(),
                id,
                title,
                tanggalMillis
            )
        }
    }

    fun updateUjian(ujian: UjianEntity) {
        viewModelScope.launch {
            repository.updateUjian(ujian)
            val matkul = currentMataKuliahList.value.find { it.id == ujian.mataKuliahId }
            val title = "${ujian.kategori.label} ${matkul?.nama ?: ""}".trim()
            CoolyeahAlarmScheduler.scheduleExamReminder(
                getApplication(),
                ujian.id,
                title,
                ujian.tanggalMillis
            )
        }
    }

    fun deleteUjian(ujian: UjianEntity) {
        viewModelScope.launch {
            repository.deleteUjian(ujian)
            CoolyeahAlarmScheduler.cancelExamReminder(getApplication(), ujian.id)
        }
    }

    // Materi actions
    fun getMateriForMatkul(mataKuliahId: Long) = repository.getMateriByMataKuliah(mataKuliahId)

    fun addMateri(mataKuliahId: Long, pertemuanKe: Int, judulMateri: String, tipeMateri: TipeMateri, uriOrUrl: String, catatan: String) {
        viewModelScope.launch {
            repository.insertMateri(
                MateriEntity(
                    mataKuliahId = mataKuliahId,
                    pertemuanKe = pertemuanKe,
                    judulMateri = judulMateri,
                    tipeMateri = tipeMateri,
                    uriOrUrl = uriOrUrl,
                    catatanTambahan = catatan
                )
            )
        }
    }

    fun updateMateri(materi: MateriEntity) {
        viewModelScope.launch {
            repository.updateMateri(materi)
        }
    }

    fun deleteMateri(materi: MateriEntity) {
        viewModelScope.launch {
            repository.deleteMateri(materi)
        }
    }
}
