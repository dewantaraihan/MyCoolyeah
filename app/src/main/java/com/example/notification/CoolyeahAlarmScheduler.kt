package com.example.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import java.util.Calendar

object CoolyeahAlarmScheduler {
    const val ACTION_DAILY_SCHEDULE = "com.example.mycoolyeah.ACTION_DAILY_SCHEDULE"
    const val ACTION_TASK_REMINDER = "com.example.mycoolyeah.ACTION_TASK_REMINDER"
    const val ACTION_EXAM_REMINDER = "com.example.mycoolyeah.ACTION_EXAM_REMINDER"

    const val EXTRA_TASK_ID = "extra_task_id"
    const val EXTRA_TASK_TITLE = "extra_task_title"
    const val EXTRA_EXAM_ID = "extra_exam_id"
    const val EXTRA_EXAM_TITLE = "extra_exam_title"

    private const val DAILY_ALARM_REQ_CODE = 9001

    fun scheduleDailyScheduleAlarm(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        val intent = Intent(context, CoolyeahNotificationReceiver::class.java).apply {
            action = ACTION_DAILY_SCHEDULE
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            DAILY_ALARM_REQ_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Calculate next 00:00:00 (calendar transition for new day)
        val now = Calendar.getInstance()
        val nextMidnight = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= now.timeInMillis) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        setAlarmSafely(alarmManager, nextMidnight.timeInMillis, pendingIntent)
    }

    fun scheduleTaskReminder(context: Context, taskId: Long, title: String, deadlineMillis: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        // H-1 at 19:00 (7 PM the day before deadline)
        val deadlineCal = Calendar.getInstance().apply { timeInMillis = deadlineMillis }
        val reminderCal = Calendar.getInstance().apply {
            set(Calendar.YEAR, deadlineCal.get(Calendar.YEAR))
            set(Calendar.DAY_OF_YEAR, deadlineCal.get(Calendar.DAY_OF_YEAR) - 1)
            set(Calendar.HOUR_OF_DAY, 19)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        if (reminderCal.timeInMillis <= System.currentTimeMillis()) return

        val intent = Intent(context, CoolyeahNotificationReceiver::class.java).apply {
            action = ACTION_TASK_REMINDER
            putExtra(EXTRA_TASK_ID, taskId)
            putExtra(EXTRA_TASK_TITLE, title)
        }
        val reqCode = (10000 + taskId).toInt()
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reqCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        setAlarmSafely(alarmManager, reminderCal.timeInMillis, pendingIntent)
    }

    fun cancelTaskReminder(context: Context, taskId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, CoolyeahNotificationReceiver::class.java).apply {
            action = ACTION_TASK_REMINDER
        }
        val reqCode = (10000 + taskId).toInt()
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reqCode,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    fun scheduleExamReminder(context: Context, examId: Long, title: String, tanggalMillis: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        // H-1 at 19:00 (7 PM the day before exam date)
        val examCal = Calendar.getInstance().apply { timeInMillis = tanggalMillis }
        val reminderCal = Calendar.getInstance().apply {
            set(Calendar.YEAR, examCal.get(Calendar.YEAR))
            set(Calendar.DAY_OF_YEAR, examCal.get(Calendar.DAY_OF_YEAR) - 1)
            set(Calendar.HOUR_OF_DAY, 19)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        if (reminderCal.timeInMillis <= System.currentTimeMillis()) return

        val intent = Intent(context, CoolyeahNotificationReceiver::class.java).apply {
            action = ACTION_EXAM_REMINDER
            putExtra(EXTRA_EXAM_ID, examId)
            putExtra(EXTRA_EXAM_TITLE, title)
        }
        val reqCode = (20000 + examId).toInt()
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reqCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        setAlarmSafely(alarmManager, reminderCal.timeInMillis, pendingIntent)
    }

    fun cancelExamReminder(context: Context, examId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, CoolyeahNotificationReceiver::class.java).apply {
            action = ACTION_EXAM_REMINDER
        }
        val reqCode = (20000 + examId).toInt()
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reqCode,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    private fun setAlarmSafely(alarmManager: AlarmManager, triggerAtMillis: Long, pendingIntent: PendingIntent) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
                } else {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            }
        } catch (_: Exception) {
            runCatching {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
                } else {
                    alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
                }
            }
        }
    }
}
