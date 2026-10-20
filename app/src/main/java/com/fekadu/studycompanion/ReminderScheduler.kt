package com.fekadu.studycompanion

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import org.json.JSONObject
import java.util.Calendar

object ReminderScheduler {
    private const val PREFS = "sc_reminders"

    fun schedule(
        context: Context, id: Int, whenMillis: Long, title: String, body: String,
        repeatDaily: Boolean, hour: Int = 0, minute: Int = 0
    ) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("id", id)
            putExtra("title", title)
            putExtra("body", body)
            putExtra("repeatDaily", repeatDaily)
            putExtra("hour", hour)
            putExtra("minute", minute)
        }
        val pi = PendingIntent.getBroadcast(
            context, id, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        try {
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, whenMillis, pi)
        } catch (e: Exception) { }
        remember(context, id, whenMillis, title, body, repeatDaily, hour, minute)
    }

    fun scheduleDaily(context: Context, id: Int, hour: Int, minute: Int, title: String, body: String) {
        val now = Calendar.getInstance()
        val next = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (!after(now)) add(Calendar.DAY_OF_YEAR, 1)
        }
        schedule(context, id, next.timeInMillis, title, body, true, hour, minute)
    }

    fun cancel(context: Context, id: Int) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java)
        val pi = PendingIntent.getBroadcast(
            context, id, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        am.cancel(pi)
        forget(context, id)
    }

    fun forget(context: Context, id: Int) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs.edit().remove("r_$id").apply()
    }

    private fun remember(
        context: Context, id: Int, whenMillis: Long, title: String, body: String,
        repeatDaily: Boolean, hour: Int, minute: Int
    ) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val obj = JSONObject()
        obj.put("when", whenMillis)
        obj.put("title", title)
        obj.put("body", body)
        obj.put("repeatDaily", repeatDaily)
        obj.put("hour", hour)
        obj.put("minute", minute)
        prefs.edit().putString("r_$id", obj.toString()).apply()
    }

    fun rescheduleAll(context: Context) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val now = System.currentTimeMillis()
        val snapshot = HashMap(prefs.all)
        for ((key, value) in snapshot) {
            if (!key.startsWith("r_") || value !is String) continue
            val id = key.removePrefix("r_").toIntOrNull() ?: continue
            try {
                val obj = JSONObject(value)
                val title = obj.getString("title")
                val body = obj.getString("body")
                val repeatDaily = obj.getBoolean("repeatDaily")
                val hour = obj.optInt("hour", 0)
                val minute = obj.optInt("minute", 0)
                if (repeatDaily) {
                    scheduleDaily(context, id, hour, minute, title, body)
                } else {
                    val whenMillis = obj.getLong("when")
                    if (whenMillis > now) {
                        schedule(context, id, whenMillis, title, body, false)
                    } else {
                        forget(context, id)
                    }
                }
            } catch (e: Exception) { }
        }
    }
}
