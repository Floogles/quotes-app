package com.floogles.dailyquote

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Fires once a day. Posts the daily-quote notification and refreshes the widget
 * so both reflect the same "quote of the day".
 */
class DailyAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val quote = QuoteStore.quoteOfDay(context)
        if (quote != null) {
            Notifications.showDailyQuote(context, quote)
        }
        DailyQuoteWidget.updateAll(context)
    }
}
