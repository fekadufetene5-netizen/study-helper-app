package com.fekadu.studycompanion

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import org.json.JSONArray
import java.util.Calendar

object Reminders {
    private const val PREFS = "reminders"
    private const val CHANNEL = "study"

    private fun items(ctx: Context): JSONArray =
        try {
            JSONArray(ctx.getSharedPreferences(PREFS, 0).getString("json", "[]"))
        } catch (e: Exception) {
            JSONArray()
        }

    private fun pending(ctx: Context, i: Int): PendingIntent =
        PendingIntent.getBroadcast(
            ctx, i,
            Intent(ctx, AlarmReceiver::class.java).putExtra("i", i),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

    fun set(ctx: Context, json: String) {
        val am = ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        for (i in 0 until items(ctx).length()) am.cancel(pending(ctx, i))
        val arr = try { JSONArray(json) } catch (e: Exception) { JSONArray() }
        ctx.getSharedPreferences(PREFS, 0).edit().putString("json", arr.toString()).apply()
        scheduleAll(ctx)
    }

    fun scheduleAll(ctx: Context) {
        for (i in 0 until items(ctx).length()) schedule(ctx, i)
    }

    private fun schedule(ctx: Context, i: Int) {
        val o = items(ctx).optJSONObject(i) ?: return
        val d = o.optInt("d", -1)
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, o.optInt("h"))
            set(Calendar.MINUTE, o.optInt("m"))
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val now = System.currentTimeMillis()
        while (cal.timeInMillis <= now || (d >= 0 && cal.get(Calendar.DAY_OF_WEEK) != d + 1)) {
            cal.add(Calendar.DAY_OF_MONTH, 1)
        }
        val am = ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.timeInMillis, pending(ctx, i))
    }

    fun fire(ctx: Context, i: Int) {
        val o = items(ctx).optJSONObject(i) ?: return
        val nm = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.createNotificationChannel(
            NotificationChannel(CHANNEL, "የጥናት ማሳሰቢያ", NotificationManager.IMPORTANCE_HIGH)
        )
        val open = PendingIntent.getActivity(
            ctx, 0, Intent(ctx, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val n = Notification.Builder(ctx, CHANNEL)
            .setSmallIcon(R.drawable.ic_stat_book)
            .setContentTitle(o.optString("t"))
            .setContentText(o.optString("x"))
            .setContentIntent(open)
            .setAutoCancel(true)
            .build()
        nm.notify(i, n)
        schedule(ctx, i)
    }
}

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Reminders.fire(context, intent.getIntExtra("i", -1))
    }
}

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) Reminders.scheduleAll(context)
    }
}
