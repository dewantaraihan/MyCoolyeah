package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.entity.JadwalKuliahEntity
import com.example.data.local.entity.MataKuliahEntity
import com.example.data.local.entity.MateriEntity
import com.example.data.local.entity.ProfileEntity
import com.example.data.local.entity.SemesterEntity
import com.example.data.local.entity.TugasEntity
import com.example.data.local.entity.UjianEntity
import com.example.data.model.KategoriUjian
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

data class SemesterDeletionImpact(
    val matkulCount: Int,
    val jadwalCount: Int,
    val tugasCount: Int,
    val ujianCount: Int,
    val materiCount: Int
)

data class MataKuliahDeletionImpact(
    val jadwalCount: Int,
    val tugasCount: Int,
    val ujianCount: Int,
    val materiCount: Int
)

interface AcademicRepository {
    // Profile
    fun getProfile(): Flow<ProfileEntity?>
    suspend fun saveProfile(profile: ProfileEntity)

    // Semester
    fun getAllSemesters(): Flow<List<SemesterEntity>>
    fun getActiveSemester(): Flow<SemesterEntity?>
    suspend fun insertSemester(semester: SemesterEntity): Long
    suspend fun updateSemester(semester: SemesterEntity)
    suspend fun deleteSemester(semester: SemesterEntity)
    suspend fun setActiveSemester(semesterId: Long): Boolean
    suspend fun getSemesterDeletionImpact(semesterId: Long): SemesterDeletionImpact

    // Mata Kuliah
    fun getAllMataKuliah(): Flow<List<MataKuliahEntity>>
    fun getMataKuliahBySemester(semesterId: Long): Flow<List<MataKuliahEntity>>
    suspend fun insertMataKuliah(mataKuliah: MataKuliahEntity): Long
    suspend fun updateMataKuliah(mataKuliah: MataKuliahEntity)
    suspend fun deleteMataKuliah(mataKuliah: MataKuliahEntity)
    suspend fun getMataKuliahDeletionImpact(mataKuliahId: Long): MataKuliahDeletionImpact

    // Jadwal Kuliah
    fun getAllJadwal(): Flow<List<JadwalKuliahEntity>>
    fun getJadwalBySemester(semesterId: Long): Flow<List<JadwalKuliahEntity>>
    fun getJadwalBySemesterAndHari(semesterId: Long, hari: Int): Flow<List<JadwalKuliahEntity>>
    suspend fun insertJadwal(jadwal: JadwalKuliahEntity): Long
    suspend fun updateJadwal(jadwal: JadwalKuliahEntity)
    suspend fun deleteJadwal(jadwal: JadwalKuliahEntity)

    // Tugas
    fun getAllTugas(): Flow<List<TugasEntity>>
    fun getTugasBySemester(semesterId: Long): Flow<List<TugasEntity>>
    fun getPendingTugasBySemester(semesterId: Long): Flow<List<TugasEntity>>
    suspend fun setTugasSelesai(id: Long, isSelesai: Boolean)
    suspend fun insertTugas(tugas: TugasEntity): Long
    suspend fun updateTugas(tugas: TugasEntity)
    suspend fun deleteTugas(tugas: TugasEntity)

    // Ujian
    fun getAllUjian(): Flow<List<UjianEntity>>
    fun getUjianBySemester(semesterId: Long): Flow<List<UjianEntity>>
    fun getUjianBySemesterAndKategori(semesterId: Long, kategori: KategoriUjian): Flow<List<UjianEntity>>
    suspend fun insertUjian(ujian: UjianEntity): Long
    suspend fun updateUjian(ujian: UjianEntity)
    suspend fun deleteUjian(ujian: UjianEntity)

    // Materi
    fun getMateriByMataKuliah(mataKuliahId: Long): Flow<List<MateriEntity>>
    suspend fun insertMateri(materi: MateriEntity): Long
    suspend fun updateMateri(materi: MateriEntity)
    suspend fun deleteMateri(materi: MateriEntity)
}

class AcademicRepositoryImpl(private val database: AppDatabase) : AcademicRepository {
    private val profileDao = database.profileDao()
    private val semesterDao = database.semesterDao()
    private val mataKuliahDao = database.mataKuliahDao()
    private val jadwalKuliahDao = database.jadwalKuliahDao()
    private val tugasDao = database.tugasDao()
    private val ujianDao = database.ujianDao()
    private val materiDao = database.materiDao()

    override fun getProfile(): Flow<ProfileEntity?> = profileDao.getProfile()
    override suspend fun saveProfile(profile: ProfileEntity) = profileDao.insertOrUpdate(profile)

    override fun getAllSemesters(): Flow<List<SemesterEntity>> = semesterDao.getAllSemesters()
    override fun getActiveSemester(): Flow<SemesterEntity?> = semesterDao.getActiveSemester()
    override suspend fun insertSemester(semester: SemesterEntity): Long = semesterDao.insertSemester(semester)
    override suspend fun updateSemester(semester: SemesterEntity) = semesterDao.updateSemester(semester)
    override suspend fun deleteSemester(semester: SemesterEntity) = semesterDao.deleteSemester(semester)
    override suspend fun setActiveSemester(semesterId: Long): Boolean = semesterDao.setActiveSemester(semesterId)

    override suspend fun getSemesterDeletionImpact(semesterId: Long): SemesterDeletionImpact {
        val matkulList = mataKuliahDao.getMataKuliahBySemesterSync(semesterId)
        var totalJadwal = 0
        var totalTugas = 0
        var totalUjian = 0
        var totalMateri = 0

        for (m in matkulList) {
            val impact = getMataKuliahDeletionImpact(m.id)
            totalJadwal += impact.jadwalCount
            totalTugas += impact.tugasCount
            totalUjian += impact.ujianCount
            totalMateri += impact.materiCount
        }

        return SemesterDeletionImpact(
            matkulCount = matkulList.size,
            jadwalCount = totalJadwal,
            tugasCount = totalTugas,
            ujianCount = totalUjian,
            materiCount = totalMateri
        )
    }

    override fun getAllMataKuliah(): Flow<List<MataKuliahEntity>> = mataKuliahDao.getAllMataKuliah()
    override fun getMataKuliahBySemester(semesterId: Long): Flow<List<MataKuliahEntity>> =
        mataKuliahDao.getMataKuliahBySemester(semesterId)
    override suspend fun insertMataKuliah(mataKuliah: MataKuliahEntity): Long =
        mataKuliahDao.insertMataKuliah(mataKuliah)
    override suspend fun updateMataKuliah(mataKuliah: MataKuliahEntity) =
        mataKuliahDao.updateMataKuliah(mataKuliah)
    override suspend fun deleteMataKuliah(mataKuliah: MataKuliahEntity) =
        mataKuliahDao.deleteMataKuliah(mataKuliah)

    override suspend fun getMataKuliahDeletionImpact(mataKuliahId: Long): MataKuliahDeletionImpact {
        val jadwalList = jadwalKuliahDao.getJadwalByMataKuliah(mataKuliahId).firstOrNull() ?: emptyList()
        val materiList = materiDao.getMateriByMataKuliahSync(mataKuliahId)
        val allTugas = tugasDao.getAllTugas().firstOrNull() ?: emptyList()
        val tugasCount = allTugas.count { it.mataKuliahId == mataKuliahId }
        val allUjian = ujianDao.getAllUjian().firstOrNull() ?: emptyList()
        val ujianCount = allUjian.count { it.mataKuliahId == mataKuliahId }

        return MataKuliahDeletionImpact(
            jadwalCount = jadwalList.size,
            tugasCount = tugasCount,
            ujianCount = ujianCount,
            materiCount = materiList.size
        )
    }

    override fun getAllJadwal(): Flow<List<JadwalKuliahEntity>> = jadwalKuliahDao.getAllJadwal()
    override fun getJadwalBySemester(semesterId: Long): Flow<List<JadwalKuliahEntity>> =
        jadwalKuliahDao.getJadwalBySemester(semesterId)
    override fun getJadwalBySemesterAndHari(semesterId: Long, hari: Int): Flow<List<JadwalKuliahEntity>> =
        jadwalKuliahDao.getJadwalBySemesterAndHari(semesterId, hari)
    override suspend fun insertJadwal(jadwal: JadwalKuliahEntity): Long =
        jadwalKuliahDao.insertJadwal(jadwal)
    override suspend fun updateJadwal(jadwal: JadwalKuliahEntity) =
        jadwalKuliahDao.updateJadwal(jadwal)
    override suspend fun deleteJadwal(jadwal: JadwalKuliahEntity) =
        jadwalKuliahDao.deleteJadwal(jadwal)

    override fun getAllTugas(): Flow<List<TugasEntity>> = tugasDao.getAllTugas()
    override fun getTugasBySemester(semesterId: Long): Flow<List<TugasEntity>> =
        tugasDao.getTugasBySemester(semesterId)
    override fun getPendingTugasBySemester(semesterId: Long): Flow<List<TugasEntity>> =
        tugasDao.getPendingTugasBySemester(semesterId)
    override suspend fun setTugasSelesai(id: Long, isSelesai: Boolean) =
        tugasDao.updateStatusSelesai(id, isSelesai)
    override suspend fun insertTugas(tugas: TugasEntity): Long =
        tugasDao.insertTugas(tugas)
    override suspend fun updateTugas(tugas: TugasEntity) =
        tugasDao.updateTugas(tugas)
    override suspend fun deleteTugas(tugas: TugasEntity) =
        tugasDao.deleteTugas(tugas)

    override fun getAllUjian(): Flow<List<UjianEntity>> = ujianDao.getAllUjian()
    override fun getUjianBySemester(semesterId: Long): Flow<List<UjianEntity>> =
        ujianDao.getUjianBySemester(semesterId)
    override fun getUjianBySemesterAndKategori(semesterId: Long, kategori: KategoriUjian): Flow<List<UjianEntity>> =
        ujianDao.getUjianBySemesterAndKategori(semesterId, kategori)
    override suspend fun insertUjian(ujian: UjianEntity): Long =
        ujianDao.insertUjian(ujian)
    override suspend fun updateUjian(ujian: UjianEntity) =
        ujianDao.updateUjian(ujian)
    override suspend fun deleteUjian(ujian: UjianEntity) =
        ujianDao.deleteUjian(ujian)

    override fun getMateriByMataKuliah(mataKuliahId: Long): Flow<List<MateriEntity>> =
        materiDao.getMateriByMataKuliah(mataKuliahId)
    override suspend fun insertMateri(materi: MateriEntity): Long =
        materiDao.insertMateri(materi)
    override suspend fun updateMateri(materi: MateriEntity) =
        materiDao.updateMateri(materi)
    override suspend fun deleteMateri(materi: MateriEntity) =
        materiDao.deleteMateri(materi)
}
