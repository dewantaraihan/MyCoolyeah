package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.data.local.dao.JadwalKuliahDao
import com.example.data.local.dao.MataKuliahDao
import com.example.data.local.dao.MateriDao
import com.example.data.local.dao.ProfileDao
import com.example.data.local.dao.SemesterDao
import com.example.data.local.dao.TugasDao
import com.example.data.local.dao.UjianDao
import com.example.data.local.entity.JadwalKuliahEntity
import com.example.data.local.entity.MataKuliahEntity
import com.example.data.local.entity.MateriEntity
import com.example.data.local.entity.ProfileEntity
import com.example.data.local.entity.SemesterEntity
import com.example.data.local.entity.TugasEntity
import com.example.data.local.entity.UjianEntity

@Database(
    entities = [
        ProfileEntity::class,
        SemesterEntity::class,
        MataKuliahEntity::class,
        JadwalKuliahEntity::class,
        TugasEntity::class,
        UjianEntity::class,
        MateriEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun profileDao(): ProfileDao
    abstract fun semesterDao(): SemesterDao
    abstract fun mataKuliahDao(): MataKuliahDao
    abstract fun jadwalKuliahDao(): JadwalKuliahDao
    abstract fun tugasDao(): TugasDao
    abstract fun ujianDao(): UjianDao
    abstract fun materiDao(): MateriDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mycoolyeah_database"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
