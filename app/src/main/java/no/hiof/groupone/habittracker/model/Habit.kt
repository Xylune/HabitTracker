package no.hiof.groupone.habittracker.model

import android.content.Context
import no.hiof.groupone.habittracker.R

data class Habit(
    val id: String = "",
    val name: String,
    val description: String?,
    val frequency: Frequency? = null,
    val startTime: Long? = System.currentTimeMillis(),
    val endTime: Long? = null,
    val basePoints: Int = 0,
    val currentStreak: Int = 0,
    val isCompleted: Boolean = false,
    val completedDates: List<Long> = emptyList(),
    val category: HabitCategory? = null
) {

    constructor() : this(
        id = "",
        name = "",
        description = null,
        frequency = null,
        startTime = System.currentTimeMillis(),
        endTime = null,
        basePoints = 0,
        currentStreak = 0,
        isCompleted = false,
        completedDates = emptyList(),
        category = null
    )
}

enum class Frequency {
    DAILY,
    WEEKLY,
    MONTHLY
}

enum class HabitCategory(private val resourceId: Int) {
    HEALTH(R.string.lbl_health),
    FITNESS(R.string.lbl_fitness),
    WORK(R.string.lbl_work),
    STUDY(R.string.lbl_study),
    PERSONAL_DEVELOPMENT(R.string.lbl_personal_development),
    HOBBY(R.string.lbl_hobby),
    OTHER(R.string.lbl_other);

    // Function to get the display name from the string resource
    fun getDisplayName(context: Context): String {
        return context.getString(resourceId)
    }
}