package no.hiof.groupone.habittracker.model

class PointsManager {

    private var currentPoints: Int = 0

    fun calculatePointsForHabit(habit: Habit): Int {
        val basePoints = habit.basePoints
        val multiplier = getMultiplierForHabit(habit)
        return basePoints * multiplier
    }

    private fun getMultiplierForHabit(habit: Habit): Int {
        // Logic to determine multiplier based on habit properties or other factors
        return 1 // Default multiplier if no special conditions apply
    }

    fun calculateStreakBonus(habit: Habit): Int {
        val streak = habit.currentStreak
        return when {
            streak >= 14 -> 10 // Bonus for 14-day streak or longer
            streak >= 7 -> 5  // Bonus for 7-day streak
            else -> 0
        }
    }

    fun addPoints(points: Int) {
        this.currentPoints += points
    }

    fun resetPoints() {
        currentPoints = 0
    }

    fun getPoints(): Int {
        return currentPoints
    }
}