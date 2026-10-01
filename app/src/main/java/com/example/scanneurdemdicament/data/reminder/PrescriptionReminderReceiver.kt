package com.example.scanneurdemdicament.data.reminder

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.scanneurdemdicament.MainActivity
import com.example.scanneurdemdicament.R

class PrescriptionReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val prescriptionId = intent.getLongExtra(EXTRA_PRESCRIPTION_ID, -1L)
        val prescriptionTitle = intent.getStringExtra(EXTRA_PRESCRIPTION_TITLE) ?: "Vos médicaments"

        showNotification(context, prescriptionId, prescriptionTitle)
    }

    private fun showNotification(context: Context, prescriptionId: Long, title: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "prescription_reminders_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Rappels de Pharmacie",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications pour les rappels de passage en pharmacie"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val mainIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            prescriptionId.toInt(),
            mainIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Rappel Pharmacie 💊")
            .setContentText("Pensez à aller chercher vos médicaments : $title")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(prescriptionId.toInt(), notification)
    }

    companion object {
        const val EXTRA_PRESCRIPTION_ID = "extra_prescription_id"
        const val EXTRA_PRESCRIPTION_TITLE = "extra_prescription_title"
    }
}