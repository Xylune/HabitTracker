package no.hiof.groupone.habittracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class HabitViewModelFactory(private val habitListViewModel: HabitListViewModel) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HabitViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HabitViewModel(habitListViewModel) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}