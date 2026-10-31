package com.example.tasktracker.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.tasktracker.QuickLogActivity

object NotificationHelper {
    const val CHANNEL_ID = "quick_log_channel"
    const val NOTIFICATION_ID = 1001

    fun createNotificationChannel(context: Context) {
        val name = "Acceso Rápido de Tareas"
        val descriptionText = "Notificación persistente para registrar tareas rápidamente"
        val importance = NotificationManager.IMPORTANCE_LOW // Discreta, sin sonido ni vibración
        val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
            description = descriptionText
            setSound(null, null)
            enableVibration(false)
        }
        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    fun showPersistentNotification(context: Context) {
        createNotificationChannel(context)

        val intent = Intent(context, QuickLogActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_today)
            .setContentTitle("Mis Tareas Ocasionales")
            .setContentText("Toca para registrar 'Lo hice' o 'No lo hice'")
            .setPriority(NotificationCompat.PRIORITY_LOW) // Discreta
            .setOngoing(true) // Persistente pero descartable por el usuario
            .setContentIntent(pendingIntent)
            .setAutoCancel(false)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, builder.build())
    }

    fun cancelNotification(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(NOTIFICATION_ID)
    }
}
