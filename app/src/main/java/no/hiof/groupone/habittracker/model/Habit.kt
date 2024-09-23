package no.hiof.groupone.habittracker.model

import org.threeten.bp.LocalDateTime

class Habit(
    val id: Int,
    val name: String,
    val description: String?,
    val frequency: Frequency? = null,
    val startTime: LocalDateTime? = LocalDateTime.now(),
    val endTime: LocalDateTime? = null,
    val basePoints: Int = 0,
) {

}

enum class Frequency {
    DAILY,
    WEEKLY,
    MONTHLY
}