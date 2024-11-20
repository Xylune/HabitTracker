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

enum class HabitCategory(val displayName: String) {
    HEALTH("Health"),
    FITNESS("Fitness"),
    WORK("Work"),
    STUDY("Study"),
    PERSONAL_DEVELOPMENT("Personal Development"),
    HOBBY("Hobby"),
    OTHER("Other");

    override fun toString(): String = displayName
}