package com.example.scanneurdemdicament.data.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build

object ReminderScheduler {

    fun scheduleReminder(
        context: Context,
        prescriptionId: Long,
        prescriptionTitle: String,
        reminderTimestamp: Long
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, PrescriptionReminderReceiver::class.java).apply {
            putExtra(PrescriptionReminderReceiver.EXTRA_PRESCRIPTION_ID, prescriptionId)
            putExtra(PrescriptionReminderReceiver.EXTRA_PRESCRIPTION_TITLE, prescriptionTitle)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            prescriptionId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        reminderTimestamp,
                        pendingIntent
                    )
                } else {
                    alarmManager.set(
                        AlarmManager.RTC_WAKEUP,
                        reminderTimestamp,
                        pendingIntent
                    )
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    reminderTimestamp,
                    pendingIntent
                )
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                reminderTimestamp,
                pendingIntent
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun cancelReminder(context: Context, prescriptionId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, PrescriptionReminderReceiver::class.java)

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            prescriptionId.toInt(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )

        pendingIntent?.let {
            alarmManager.cancel(it)
            it.cancel()
        }
    }
}