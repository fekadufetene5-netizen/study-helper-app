package com.fekadu.studycompanion

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getIntExtra("id", 0)
        val title = intent.getStringExtra("title") ?: "Study Companion"
        val body = intent.getStringExtra("body") ?: ""
        val repeatDaily = intent.getBooleanExtra("repeatDaily", false)

        showNotification(context, id, title, body)

        if (repeatDaily) {
            val hour = intent.getIntExtra("hour", 19)
            val minute = intent.getIntExtra("minute", 0)
            ReminderScheduler.scheduleDaily(context, id, hour, minute, title, body)
        } else {
            ReminderScheduler.forget(context, id)
        }
    }

    private fun showNotification(context: Context, id: Int, title: String, body: String) {
        val channelId = "study_reminders"
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Study reminders", NotificationManager.IMPORTANCE_HIGH)
            nm.createNotificationChannel(channel)
        }
        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pi = PendingIntent.getActivity(
            context, id, openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notif = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(pi)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
        try {
            nm.notify(id, notif)
        } catch (e: SecurityException) { /* notification permission not granted */ }
    }
}
