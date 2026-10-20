package com.fekadu.studycompanion

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import org.json.JSONArray
import java.util.Calendar

object Reminders {

    private const val CHANNEL = "study_reminders"
    private const val MAX = 100

    private fun load(ctx: Context): JSONArray {
        return try {
            JSONArray(
                ctx.getSharedPreferences("sc", Context.MODE_PRIVATE)
                    .getString("rem", "[]")
            )
        } catch (_: Exception) {
            JSONArray()
        }
    }

    private fun pending(
        ctx: Context,
        id: Int
    ): PendingIntent {
        val intent = Intent(
            ctx,
            ReminderReceiver::class.java
        ).putExtra("id", id)

        return PendingIntent.getBroadcast(
            ctx,
            id,
            intent,
            PendingIntent.FLAG_IMMUTABLE or
                PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    fun canNotify(ctx: Context): Boolean {
        return Build.VERSION.SDK_INT < 33 ||
            ContextCompat.checkSelfPermission(
                ctx,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
    }

    fun schedule(ctx: Context) {
        val alarmManager =
            ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Cancel previously scheduled alarms managed by this class.
        for (id in 0 until MAX) {
            alarmManager.cancel(pending(ctx, id))
        }

        val reminders = load(ctx)
        val now = System.currentTimeMillis()

        for (id in 0 until minOf(reminders.length(), MAX)) {
            val item = reminders.optJSONObject(id) ?: continue

            val hour = item.optInt("h", 18).coerceIn(0, 23)
            val minute = item.optInt("m", 0).coerceIn(0, 59)
            val day = item.optInt("d", -1)

            val alarmTime = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            if (day in 0..6) {
                // JavaScript weekday: Sunday=0 ... Saturday=6.
                // Calendar weekday: Sunday=1 ... Saturday=7.
                val targetDay = day + 1
                var delta =
                    (targetDay - alarmTime.get(Calendar.DAY_OF_WEEK) + 7) % 7

                if (delta == 0 && alarmTime.timeInMillis <= now) {
                    delta = 7
                }

                alarmTime.add(Calendar.DAY_OF_YEAR, delta)
            } else {
                // d = -1 means repeat daily.
                if (alarmTime.timeInMillis <= now) {
                    alarmTime.add(Calendar.DAY_OF_YEAR, 1)
                }
            }

            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                alarmTime.timeInMillis,
                pending(ctx, id)
            )
        }
    }

    fun show(ctx: Context, id: Int) {
        if (!canNotify(ctx)) return

        val item = load(ctx).optJSONObject(id) ?: return

        val manager = ctx.getSystemService(
            Context.NOTIFICATION_SERVICE
        ) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL,
                    "Study reminders",
                    NotificationManager.IMPORTANCE_DEFAULT
                )
            )
        }

        val openApp = PendingIntent.getActivity(
            ctx,
            1000 + id,
            Intent(ctx, MainActivity::class.java).apply {
                flags =
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_IMMUTABLE or
                PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(ctx, CHANNEL)
            .setSmallIcon(R.drawable.ic_notif)
            .setContentTitle(
                item.optString("title", "የጥናት ማሳሰቢያ")
            )
            .setContentText(
                item.optString("text", "የጥናት ጊዜው ደርሷል")
            )
            .setContentIntent(openApp)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        manager.notify(id, notification)
    }
}

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getIntExtra("id", -1)
        if (id < 0) return

        Reminders.show(context, id)

        // Schedule the next daily or weekly occurrence.
        Reminders.schedule(context)
    }
}

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED -> {
                Reminders.schedule(context)
            }
        }
    }
}
