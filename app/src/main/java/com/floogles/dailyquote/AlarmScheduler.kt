package com.floogles.dailyquote

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.util.Calendar

/**
 * Schedules a once-a-day alarm at 08:00 local time. Uses an inexact repeating
 * alarm so it needs no special "exact alarm" permission — a few minutes of drift
 * is fine for a daily quote.
 */
object AlarmScheduler {

    private const val REQUEST_CODE = 100
    const val NOTIFY_HOUR = 8
    const val NOTIFY_MINUTE = 0

    fun schedule(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            Intent(context, DailyAlarmReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            nextTriggerTime(),
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }

    private fun nextTriggerTime(): Long {
        val now = Calendar.getInstance()
        val next = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, NOTIFY_HOUR)
            set(Calendar.MINUTE, NOTIFY_MINUTE)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (!next.after(now)) {
            next.add(Calendar.DAY_OF_YEAR, 1)
        }
        return next.timeInMillis
    }
}
