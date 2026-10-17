package com.example.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.local.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

class CoolyeahNotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val database = AppDatabase.getDatabase(context)
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                when (action) {
                    CoolyeahAlarmScheduler.ACTION_DAILY_SCHEDULE -> {
                        val calendar = Calendar.getInstance()
                        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
                        val hari = when (dayOfWeek) {
                            Calendar.MONDAY -> 1
                            Calendar.TUESDAY -> 2
                            Calendar.WEDNESDAY -> 3
                            Calendar.THURSDAY -> 4
                            Calendar.FRIDAY -> 5
                            Calendar.SATURDAY -> 6
                            Calendar.SUNDAY -> 7
                            else -> 1
                        }

                        val activeSemester = database.semesterDao().getActiveSemesterSync()
                        if (activeSemester != null) {
                            val classes = database.jadwalKuliahDao()
                                .getJadwalBySemesterAndHariSync(activeSemester.id, hari)
                            if (classes.isNotEmpty()) {
                                val detail = if (classes.size == 1) {
                                    "Ada 1 jadwal kelas hari ini pada jam ${classes.first().jamMulai}. Semangat!"
                                } else {
                                    "Ada ${classes.size} jadwal kelas hari ini dimulai jam ${classes.first().jamMulai}. Semangat!"
                                }
                                NotificationHelper.showNotification(
                                    context,
                                    1001,
                                    NotificationHelper.CHANNEL_SCHEDULE_ID,
                                    "Jadwal Kuliah Hari Ini",
                                    detail
                                )
                            }
                        }
                        // Re-schedule for next midnight
                        CoolyeahAlarmScheduler.scheduleDailyScheduleAlarm(context)
                    }

                    CoolyeahAlarmScheduler.ACTION_TASK_REMINDER -> {
                        val taskId = intent.getLongExtra(CoolyeahAlarmScheduler.EXTRA_TASK_ID, -1L)
                        val taskTitle = intent.getStringExtra(CoolyeahAlarmScheduler.EXTRA_TASK_TITLE) ?: "Tugas Kuliah"
                        if (taskId != -1L) {
                            val task = database.tugasDao().getTugasById(taskId)
                            if (task != null && !task.isSelesai) {
                                NotificationHelper.showNotification(
                                    context,
                                    (10000 + taskId).toInt(),
                                    NotificationHelper.CHANNEL_REMINDER_ID,
                                    "Pengingat Tugas H-1",
                                    "Tugas \"$taskTitle\" memiliki batas waktu besok! Yuk selesaikan malam ini."
                                )
                            }
                        }
                    }

                    CoolyeahAlarmScheduler.ACTION_EXAM_REMINDER -> {
                        val examId = intent.getLongExtra(CoolyeahAlarmScheduler.EXTRA_EXAM_ID, -1L)
                        val examTitle = intent.getStringExtra(CoolyeahAlarmScheduler.EXTRA_EXAM_TITLE) ?: "Ujian Kuliah"
                        if (examId != -1L) {
                            NotificationHelper.showNotification(
                                context,
                                (20000 + examId).toInt(),
                                NotificationHelper.CHANNEL_REMINDER_ID,
                                "Pengingat Evaluasi & Ujian H-1",
                                "Besok ada agenda \"$examTitle\"! Siapkan diri dan istirahat yang cukup malam ini."
                            )
                        }
                    }

                    Intent.ACTION_BOOT_COMPLETED -> {
                        CoolyeahAlarmScheduler.scheduleDailyScheduleAlarm(context)
                        val pendingTasks = database.tugasDao().getAllPendingTugasSync()
                        for (t in pendingTasks) {
                            CoolyeahAlarmScheduler.scheduleTaskReminder(context, t.id, t.judul, t.deadlineMillis)
                        }
                        val upcomingExams = database.ujianDao().getAllUjianSync()
                        for (e in upcomingExams) {
                            val matkul = database.mataKuliahDao().getMataKuliahById(e.mataKuliahId)
                            val title = "${e.kategori.label} ${matkul?.nama ?: ""}".trim()
                            CoolyeahAlarmScheduler.scheduleExamReminder(context, e.id, title, e.tanggalMillis)
                        }
                    }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
