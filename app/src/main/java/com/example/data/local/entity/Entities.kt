package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.data.model.KategoriUjian
import com.example.data.model.PrioritasTugas
import com.example.data.model.StatusTugas
import com.example.data.model.TipeMateri

@Entity(tableName = "profile")
data class ProfileEntity(
    @PrimaryKey val id: Int = 1,
    val nama: String = "",
    val nim: String = "",
    val universitas: String = "",
    val jurusan: String = "",
    val angkatan: String = "",
    val fotoUri: String? = null
)

@Entity(tableName = "semester")
data class SemesterEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val namaSemester: String,
    val tahunAjaran: String,
    val isActive: Boolean = false
)

@Entity(
    tableName = "mata_kuliah",
    foreignKeys = [
        ForeignKey(
            entity = SemesterEntity::class,
            parentColumns = ["id"],
            childColumns = ["semesterId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["semesterId"])]
)
data class MataKuliahEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val semesterId: Long,
    val nama: String,
    val kode: String = "",
    val sks: Int = 3,
    val namaDosen: String = "",
    val ruang: String = "",
    val warnaLabel: Long = 0xFF0D9488
)

@Entity(
    tableName = "jadwal_kuliah",
    foreignKeys = [
        ForeignKey(
            entity = MataKuliahEntity::class,
            parentColumns = ["id"],
            childColumns = ["mataKuliahId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["mataKuliahId"])]
)
data class JadwalKuliahEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val mataKuliahId: Long,
    val hari: Int, // 1=Senin, 2=Selasa, 3=Rabu, 4=Kamis, 5=Jumat, 6=Sabtu, 7=Minggu
    val jamMulai: String,
    val jamSelesai: String,
    val ruang: String = "",
    val catatan: String = ""
)

@Entity(
    tableName = "tugas",
    foreignKeys = [
        ForeignKey(
            entity = MataKuliahEntity::class,
            parentColumns = ["id"],
            childColumns = ["mataKuliahId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["mataKuliahId"])]
)
data class TugasEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val mataKuliahId: Long,
    val judul: String,
    val deskripsi: String = "",
    val deadlineMillis: Long,
    val prioritas: PrioritasTugas = PrioritasTugas.SEDANG,
    val isSelesai: Boolean = false
) {
    fun getStatus(nowMillis: Long = System.currentTimeMillis()): StatusTugas {
        return when {
            isSelesai -> StatusTugas.SELESAI
            deadlineMillis < nowMillis -> StatusTugas.TERLEWAT
            else -> StatusTugas.BELUM_SELESAI
        }
    }
}

@Entity(
    tableName = "ujian",
    foreignKeys = [
        ForeignKey(
            entity = MataKuliahEntity::class,
            parentColumns = ["id"],
            childColumns = ["mataKuliahId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["mataKuliahId"])]
)
data class UjianEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val mataKuliahId: Long,
    val kategori: KategoriUjian, // KUIS, UTS, UAS
    val tanggalMillis: Long,
    val jamMulai: String = "",
    val jamSelesai: String = "",
    val ruang: String = "",
    val catatan: String = ""
)

@Entity(
    tableName = "materi",
    foreignKeys = [
        ForeignKey(
            entity = MataKuliahEntity::class,
            parentColumns = ["id"],
            childColumns = ["mataKuliahId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["mataKuliahId"])]
)
data class MateriEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val mataKuliahId: Long,
    val pertemuanKe: Int = 1,
    val judulMateri: String,
    val tipeMateri: TipeMateri, // DOKUMEN_LOKAL, YOUTUBE
    val uriOrUrl: String,
    val catatanTambahan: String = ""
)
