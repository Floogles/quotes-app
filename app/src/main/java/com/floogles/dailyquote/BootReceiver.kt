package com.floogles.dailyquote

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Re-schedules the daily alarm after the device reboots (alarms are cleared on
 * reboot).
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            AlarmScheduler.schedule(context)
            DailyQuoteWidget.updateAll(context)
        }
    }
}
