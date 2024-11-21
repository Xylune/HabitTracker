package no.hiof.groupone.habittracker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.MutableLiveData
import no.hiof.groupone.habittracker.AppConstants.NotificationKeys.RMNDR_NOTI_TITLE_KEY

class NotificationReceiver : BroadcastReceiver() {
    companion object {
        val notificationCount = MutableLiveData(0)
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        val scheduleNotificationService = context?.let { NotificationService(it) }
        val title: String? = intent?.getStringExtra(RMNDR_NOTI_TITLE_KEY)
        scheduleNotificationService?.showNotification(title)

        notificationCount.value = notificationCount.value?.plus(1) ?: 1
    }
}