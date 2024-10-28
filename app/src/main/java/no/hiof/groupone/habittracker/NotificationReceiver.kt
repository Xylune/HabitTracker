package no.hiof.groupone.habittracker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import no.hiof.groupone.habittracker.AppConstants.NotificationKeys.RMNDR_NOTI_TITLE_KEY

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val scheduleNotificationService = context?.let { NotificationService(it) }
        val title: String? = intent?.getStringExtra(RMNDR_NOTI_TITLE_KEY)
        scheduleNotificationService?.showNotification(title)
    }
}