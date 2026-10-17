package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.local.entity.JadwalKuliahEntity
import com.example.data.local.entity.MataKuliahEntity
import com.example.data.local.entity.MateriEntity
import com.example.data.local.entity.ProfileEntity
import com.example.data.local.entity.SemesterEntity
import com.example.data.local.entity.TugasEntity
import com.example.data.local.entity.UjianEntity
import com.example.data.model.KategoriUjian
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileDao {
    @Query("SELECT * FROM profile WHERE id = 1 LIMIT 1")
    fun getProfile(): Flow<ProfileEntity?>

    @Query("SELECT * FROM profile WHERE id = 1 LIMIT 1")
    suspend fun getProfileSync(): ProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(profile: ProfileEntity)
}

@Dao
interface SemesterDao {
    @Query("SELECT * FROM semester ORDER BY id ASC")
    fun getAllSemesters(): Flow<List<SemesterEntity>>

    @Query("SELECT * FROM semester ORDER BY id ASC")
    suspend fun getAllSemestersSync(): List<SemesterEntity>

    @Query("SELECT * FROM semester WHERE isActive = 1 LIMIT 1")
    fun getActiveSemester(): Flow<SemesterEntity?>

    @Query("SELECT * FROM semester WHERE isActive = 1 LIMIT 1")
    suspend fun getActiveSemesterSync(): SemesterEntity?

    @Query("SELECT * FROM semester WHERE id = :semesterId LIMIT 1")
    suspend fun getSemesterById(semesterId: Long): SemesterEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSemester(semester: SemesterEntity): Long

    @Update
    suspend fun updateSemester(semester: SemesterEntity)

    @Delete
    suspend fun deleteSemester(semester: SemesterEntity)

    @Query("UPDATE semester SET isActive = 0")
    suspend fun clearActiveSemester()

    @Query("UPDATE semester SET isActive = 1 WHERE id = :semesterId")
    suspend fun markActiveSemester(semesterId: Long)

    @Transaction
    suspend fun setActiveSemester(semesterId: Long): Boolean {
        val target = getSemesterById(semesterId) ?: return false
        clearActiveSemester()
        markActiveSemester(target.id)
        return true
    }
}

@Dao
interface MataKuliahDao {
    @Query("SELECT * FROM mata_kuliah ORDER BY nama ASC")
    fun getAllMataKuliah(): Flow<List<MataKuliahEntity>>

    @Query("SELECT * FROM mata_kuliah WHERE semesterId = :semesterId ORDER BY nama ASC")
    fun getMataKuliahBySemester(semesterId: Long): Flow<List<MataKuliahEntity>>

    @Query("SELECT * FROM mata_kuliah WHERE semesterId = :semesterId ORDER BY nama ASC")
    suspend fun getMataKuliahBySemesterSync(semesterId: Long): List<MataKuliahEntity>

    @Query("SELECT * FROM mata_kuliah WHERE id = :id LIMIT 1")
    suspend fun getMataKuliahById(id: Long): MataKuliahEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMataKuliah(mataKuliah: MataKuliahEntity): Long

    @Update
    suspend fun updateMataKuliah(mataKuliah: MataKuliahEntity)

    @Delete
    suspend fun deleteMataKuliah(mataKuliah: MataKuliahEntity)
}

@Dao
interface JadwalKuliahDao {
    @Query("SELECT * FROM jadwal_kuliah ORDER BY hari ASC, jamMulai ASC")
    fun getAllJadwal(): Flow<List<JadwalKuliahEntity>>

    @Query("""
        SELECT j.* FROM jadwal_kuliah j
        INNER JOIN mata_kuliah m ON j.mataKuliahId = m.id
        WHERE m.semesterId = :semesterId
        ORDER BY j.hari ASC, j.jamMulai ASC
    """)
    fun getJadwalBySemester(semesterId: Long): Flow<List<JadwalKuliahEntity>>

    @Query("""
        SELECT j.* FROM jadwal_kuliah j
        INNER JOIN mata_kuliah m ON j.mataKuliahId = m.id
        WHERE m.semesterId = :semesterId AND j.hari = :hari
        ORDER BY j.jamMulai ASC
    """)
    fun getJadwalBySemesterAndHari(semesterId: Long, hari: Int): Flow<List<JadwalKuliahEntity>>

    @Query("""
        SELECT j.* FROM jadwal_kuliah j
        INNER JOIN mata_kuliah m ON j.mataKuliahId = m.id
        WHERE m.semesterId = :semesterId AND j.hari = :hari
        ORDER BY j.jamMulai ASC
    """)
    suspend fun getJadwalBySemesterAndHariSync(semesterId: Long, hari: Int): List<JadwalKuliahEntity>

    @Query("SELECT * FROM jadwal_kuliah WHERE hari = :hari ORDER BY jamMulai ASC")
    suspend fun getJadwalByHariSync(hari: Int): List<JadwalKuliahEntity>

    @Query("SELECT * FROM jadwal_kuliah WHERE mataKuliahId = :mataKuliahId ORDER BY hari ASC")
    fun getJadwalByMataKuliah(mataKuliahId: Long): Flow<List<JadwalKuliahEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJadwal(jadwal: JadwalKuliahEntity): Long

    @Update
    suspend fun updateJadwal(jadwal: JadwalKuliahEntity)

    @Delete
    suspend fun deleteJadwal(jadwal: JadwalKuliahEntity)
}

@Dao
interface TugasDao {
    @Query("SELECT * FROM tugas ORDER BY isSelesai ASC, deadlineMillis ASC")
    fun getAllTugas(): Flow<List<TugasEntity>>

    @Query("""
        SELECT t.* FROM tugas t
        INNER JOIN mata_kuliah m ON t.mataKuliahId = m.id
        WHERE m.semesterId = :semesterId
        ORDER BY t.isSelesai ASC, t.deadlineMillis ASC
    """)
    fun getTugasBySemester(semesterId: Long): Flow<List<TugasEntity>>

    @Query("""
        SELECT t.* FROM tugas t
        INNER JOIN mata_kuliah m ON t.mataKuliahId = m.id
        WHERE m.semesterId = :semesterId AND t.isSelesai = 0
        ORDER BY t.deadlineMillis ASC
    """)
    fun getPendingTugasBySemester(semesterId: Long): Flow<List<TugasEntity>>

    @Query("SELECT * FROM tugas WHERE isSelesai = 0")
    suspend fun getAllPendingTugasSync(): List<TugasEntity>

    @Query("SELECT * FROM tugas WHERE id = :id LIMIT 1")
    suspend fun getTugasById(id: Long): TugasEntity?

    @Query("UPDATE tugas SET isSelesai = :isSelesai WHERE id = :id")
    suspend fun updateStatusSelesai(id: Long, isSelesai: Boolean)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTugas(tugas: TugasEntity): Long

    @Update
    suspend fun updateTugas(tugas: TugasEntity)

    @Delete
    suspend fun deleteTugas(tugas: TugasEntity)
}

@Dao
interface UjianDao {
    @Query("SELECT * FROM ujian ORDER BY tanggalMillis ASC, jamMulai ASC")
    fun getAllUjian(): Flow<List<UjianEntity>>

    @Query("""
        SELECT u.* FROM ujian u
        INNER JOIN mata_kuliah m ON u.mataKuliahId = m.id
        WHERE m.semesterId = :semesterId
        ORDER BY u.tanggalMillis ASC, u.jamMulai ASC
    """)
    fun getUjianBySemester(semesterId: Long): Flow<List<UjianEntity>>

    @Query("""
        SELECT u.* FROM ujian u
        INNER JOIN mata_kuliah m ON u.mataKuliahId = m.id
        WHERE m.semesterId = :semesterId AND u.kategori = :kategori
        ORDER BY u.tanggalMillis ASC, u.jamMulai ASC
    """)
    fun getUjianBySemesterAndKategori(semesterId: Long, kategori: KategoriUjian): Flow<List<UjianEntity>>

    @Query("SELECT * FROM ujian ORDER BY tanggalMillis ASC")
    suspend fun getAllUjianSync(): List<UjianEntity>

    @Query("SELECT * FROM ujian WHERE id = :id LIMIT 1")
    suspend fun getUjianById(id: Long): UjianEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUjian(ujian: UjianEntity): Long

    @Update
    suspend fun updateUjian(ujian: UjianEntity)

    @Delete
    suspend fun deleteUjian(ujian: UjianEntity)
}

@Dao
interface MateriDao {
    @Query("SELECT * FROM materi WHERE mataKuliahId = :mataKuliahId ORDER BY pertemuanKe ASC")
    fun getMateriByMataKuliah(mataKuliahId: Long): Flow<List<MateriEntity>>

    @Query("SELECT * FROM materi WHERE mataKuliahId = :mataKuliahId ORDER BY pertemuanKe ASC")
    suspend fun getMateriByMataKuliahSync(mataKuliahId: Long): List<MateriEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMateri(materi: MateriEntity): Long

    @Update
    suspend fun updateMateri(materi: MateriEntity)

    @Delete
    suspend fun deleteMateri(materi: MateriEntity)
}
