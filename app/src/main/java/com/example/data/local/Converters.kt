package com.example.data.local

import androidx.room.TypeConverter
import com.example.data.model.KategoriUjian
import com.example.data.model.PrioritasTugas
import com.example.data.model.TipeMateri

class Converters {
    @TypeConverter
    fun fromKategoriUjian(value: KategoriUjian?): String? = value?.name

    @TypeConverter
    fun toKategoriUjian(value: String?): KategoriUjian? =
        value?.let { runCatching { KategoriUjian.valueOf(it) }.getOrDefault(KategoriUjian.KUIS) }

    @TypeConverter
    fun fromTipeMateri(value: TipeMateri?): String? = value?.name

    @TypeConverter
    fun toTipeMateri(value: String?): TipeMateri? =
        value?.let { runCatching { TipeMateri.valueOf(it) }.getOrDefault(TipeMateri.DOKUMEN_LOKAL) }

    @TypeConverter
    fun fromPrioritasTugas(value: PrioritasTugas?): String? = value?.name

    @TypeConverter
    fun toPrioritasTugas(value: String?): PrioritasTugas? =
        value?.let { runCatching { PrioritasTugas.valueOf(it) }.getOrDefault(PrioritasTugas.SEDANG) }
}
