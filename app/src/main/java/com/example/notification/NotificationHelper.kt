package com.example.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity

object NotificationHelper {
    const val CHANNEL_SCHEDULE_ID = "channel_schedule_summary"
    const val CHANNEL_REMINDER_ID = "channel_task_exam_reminders"

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val scheduleChannel = NotificationChannel(
                CHANNEL_SCHEDULE_ID,
                "Jadwal Kuliah Harian",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Ringkasan jadwal kelas yang aktif setiap hari"
            }

            val reminderChannel = NotificationChannel(
                CHANNEL_REMINDER_ID,
                "Pengingat Tugas & Ujian",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Pengingat H-1 untuk tenggat tugas dan jadwal evaluasi/ujian"
            }

            notificationManager.createNotificationChannels(listOf(scheduleChannel, reminderChannel))
        }
    }

    fun showNotification(
        context: Context,
        notificationId: Int,
        channelId: String,
        title: String,
        content: String
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content))
            .setPriority(
                if (channelId == CHANNEL_REMINDER_ID) NotificationCompat.PRIORITY_HIGH
                else NotificationCompat.PRIORITY_DEFAULT
            )
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        runCatching {
            NotificationManagerCompat.from(context).notify(notificationId, builder.build())
        }
    }
}
