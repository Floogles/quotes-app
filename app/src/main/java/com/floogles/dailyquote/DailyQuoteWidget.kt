package com.floogles.dailyquote

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews

class DailyQuoteWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (id in appWidgetIds) {
            updateWidget(context, appWidgetManager, id)
        }
    }

    override fun onEnabled(context: Context) {
        // First widget added: make sure the daily alarm is running.
        AlarmScheduler.schedule(context)
    }

    companion object {
        fun updateAll(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(
                ComponentName(context, DailyQuoteWidget::class.java)
            )
            for (id in ids) {
                updateWidget(context, manager, id)
            }
        }

        private fun updateWidget(
            context: Context,
            manager: AppWidgetManager,
            widgetId: Int
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_daily_quote)
            val quote = QuoteStore.quoteOfDay(context)

            if (quote == null) {
                views.setTextViewText(
                    R.id.widget_quote,
                    context.getString(R.string.widget_empty)
                )
                views.setViewVisibility(R.id.widget_author, View.GONE)
            } else {
                views.setTextViewText(R.id.widget_quote, quote.text)
                if (quote.author.isNotBlank()) {
                    views.setTextViewText(R.id.widget_author, "— ${quote.author}")
                    views.setViewVisibility(R.id.widget_author, View.VISIBLE)
                } else {
                    views.setViewVisibility(R.id.widget_author, View.GONE)
                }
            }

            val openIntent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)

            manager.updateAppWidget(widgetId, views)
        }
    }
}
