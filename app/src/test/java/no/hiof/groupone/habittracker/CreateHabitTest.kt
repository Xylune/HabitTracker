package no.hiof.groupone.habittracker

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import no.hiof.groupone.habittracker.model.Habit
import no.hiof.groupone.habittracker.viewmodel.HabitListViewModel
import no.hiof.groupone.habittracker.viewmodel.HabitViewModel
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description

class CreateHabitTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var habitListViewModel: HabitListViewModel
    private lateinit var habitViewModel: HabitViewModel
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    @Before
    fun setup() {
        mockkStatic(android.util.Log::class)
        mockkStatic(FirebaseFirestore::class)
        mockkStatic(FirebaseAuth::class)

        every { Log.d(any(), any()) } returns 0
        every { Log.e(any(), any()) } returns 0

        firestore = mockk(relaxed = true)
        auth = mockk(relaxed = true)

        every { FirebaseFirestore.getInstance() } returns firestore
        every { FirebaseAuth.getInstance() } returns auth

        habitListViewModel = mockk(relaxed = true)
        habitViewModel = HabitViewModel(habitListViewModel)
    }

    @Test
    fun `when updating habit name, state is updated correctly`() {
        habitViewModel.updateHabitName("Exercise")
        assertEquals("Exercise", habitViewModel.habitName.value)
    }

    @Test
    fun `when updating frequency, state is updated correctly`() {
        habitViewModel.updateFrequency("Daily")
        assertEquals("Daily", habitViewModel.frequency.value)
    }

    @Test
    fun `createHabit should add habit to Firebase and update list`() = runTest {
        val habit = Habit(
            name = "Exercise",
            description = "Daily workout"
        )

        val result = habitViewModel.createHabit(habit) {}

        assertTrue(result)
        coVerify { habitListViewModel.addNewHabit(any()) }
        verify { habitListViewModel.refreshHabits() }
    }

    @After
    fun tearDown() {
        unmockkAll()
    }
}

class MainDispatcherRule : TestWatcher() {
    @OptIn(ExperimentalCoroutinesApi::class)
    private val testDispatcher = UnconfinedTestDispatcher()

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun starting(description: Description) {
        Dispatchers.setMain(testDispatcher)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}
