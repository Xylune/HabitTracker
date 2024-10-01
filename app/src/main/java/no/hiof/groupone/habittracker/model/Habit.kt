package no.hiof.groupone.habittracker.model


class Habit(
    val id: Int = generateId(),
    val name: String,
    val description: String?,
    val frequency: Frequency? = null,
    val startTime: Long? = System.currentTimeMillis(),  // Store as Long (timestamp)
    val endTime: Long? = null,
    val basePoints: Int = 0,
    val currentStreak: Int = 0
) {
    companion object {
        private var currentId = 0

        // Method to generate the next ID
        fun generateId(): Int {
            return ++currentId
        }
    }

    // No-argument constructor
    constructor() : this(
        id = generateId(),
        name = "",
        description = null,
        frequency = null,
        startTime = System.currentTimeMillis(),
        endTime = null,
        basePoints = 0,
        currentStreak = 0
    )
}

enum class Frequency {
    DAILY,
    WEEKLY,
    MONTHLY
}