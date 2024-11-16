package no.hiof.groupone.habittracker.model


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
    val completedDates: List<Long> = emptyList()
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
        completedDates = emptyList()
    )
}

enum class Frequency {
    DAILY,
    WEEKLY,
    MONTHLY
}