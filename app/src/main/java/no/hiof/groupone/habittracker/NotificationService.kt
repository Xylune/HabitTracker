package no.hiof.groupone.habittracker

import android.app.NotificationManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.annotation.DrawableRes
import androidx.core.app.NotificationCompat
import kotlin.random.Random

class NotificationService(
    private val context: Context
) {
    private val notificationManager = context.getSystemService(NotificationManager::class.java)

    fun showNotification() {
        val notification = NotificationCompat.Builder(context, "notification_channel_id")
            .setContentTitle("Reminder")
            .setContentText("Do something")
            .setSmallIcon(R.drawable.notification)
            .setPriority(NotificationManager.IMPORTANCE_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(Random.nextInt(), notification)
    }

    fun showExpandableNotification() {
        val image = context.bitmapFromResource(R.drawable.notification)
        val notification = NotificationCompat.Builder(context, "notification_channel_id")
            .setContentTitle("Reminder")
            .setContentText("Do something")
            .setSmallIcon(R.drawable.notification)
            .setPriority(NotificationManager.IMPORTANCE_HIGH)
            .setLargeIcon(image)
            .setStyle(
                NotificationCompat.BigPictureStyle()
                    .bigPicture(image)
                    .bigLargeIcon(null as Bitmap?)
            )
            .setAutoCancel(true)
            .build()

        notificationManager.notify(Random.nextInt(), notification)
    }

    fun showExpandableNotificationWithText() {
        val notification = NotificationCompat.Builder(context, "notification_channel_id")
            .setContentTitle("Reminder")
            .setContentText("Do something")
            .setSmallIcon(R.drawable.notification)
            .setPriority(NotificationManager.IMPORTANCE_HIGH)
            .setStyle(
                NotificationCompat
                    .BigTextStyle()
                    .bigText("This is a big text")
            )
            .setAutoCancel(true)
            .build()

        notificationManager.notify(Random.nextInt(), notification)
    }

    fun showInboxStyleNotification() {
        val notification = NotificationCompat.Builder(context, "notification_channel_id")
            .setContentTitle("Reminder")
            .setContentText("Do something")
            .setSmallIcon(R.drawable.notification)
            .setPriority(NotificationManager.IMPORTANCE_HIGH)
            .setStyle(
                NotificationCompat
                    .InboxStyle()
                    .addLine("This is a big text")
                    .addLine("This is a big text")
                    .addLine("This is a big text")
            )
            .setAutoCancel(true)
            .build()

        notificationManager.notify(Random.nextInt(), notification)
    }

    fun showNotificationGroup() {
        val groupId = "notification_group"
        val summaryId = 0

        val notification1 = NotificationCompat.Builder(context, "notification_channel_id")
            .setContentTitle("Reminder")
            .setContentText("Do something")
            .setSmallIcon(R.drawable.notification)
            .setPriority(NotificationManager.IMPORTANCE_HIGH)
            .setStyle(
                NotificationCompat
                    .InboxStyle()
                    .addLine("Text 1")
            )
            .setAutoCancel(true)
            .setGroup(groupId)
            .build()

        val notification2 = NotificationCompat.Builder(context, "notification_channel_id")
            .setContentTitle("Reminder")
            .setContentText("Do something")
            .setSmallIcon(R.drawable.notification)
            .setPriority(NotificationManager.IMPORTANCE_HIGH)
            .setStyle(
                NotificationCompat
                    .InboxStyle()
                    .addLine("This is a big text")
                    .addLine("This is a big text")
            )
            .setAutoCancel(true)
            .setGroup(groupId)
            .build()

        val summmaryNotification = NotificationCompat.Builder(context, "notification_channel_id")
            .setContentTitle("Reminder")
            .setContentText("Do something")
            .setSmallIcon(R.drawable.notification)
            .setPriority(NotificationManager.IMPORTANCE_HIGH)
            .setStyle(
                NotificationCompat
                    .InboxStyle()
                    .setSummaryText("Summary")
                    .setBigContentTitle("Reminders")
            )
            .setAutoCancel(true)
            .setGroup(groupId)
            .setGroupSummary(true)
            .build()

        notificationManager.notify(Random.nextInt(), notification1)
        notificationManager.notify(Random.nextInt(), notification2)
        notificationManager.notify(summaryId, summmaryNotification)
    }

    private fun Context.bitmapFromResource(
        @DrawableRes resId: Int
    ) = BitmapFactory.decodeResource(
        resources,
        resId
    )

}