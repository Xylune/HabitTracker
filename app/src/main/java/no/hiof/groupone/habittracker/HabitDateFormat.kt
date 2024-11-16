package no.hiof.groupone.habittracker

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun formatTime(timestamp: Long?): String {
    return when (timestamp) {
        null -> "N/A"
        else -> {
            val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
            sdf.format(Date(timestamp))
        }
    }
}