package com.udacity.project4.locationreminders.reminderslist

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.not
import org.hamcrest.MatcherAssert
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {

    private lateinit var reminderListViewModel: RemindersListViewModel

    private lateinit var fakeDataSource: FakeDataSource

    // provide testing to the RemindersListViewModel and its live data objects
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()


    private val reminderList = listOf(
        ReminderDTO("title1", "description1", "location1", 0.0, 0.0),
        ReminderDTO(
            "title2",
            "description2",
            "location2",
            0.0, 0.0
        ),
        ReminderDTO(
            "title3",
            "description3",
            "location3",
            0.0, 0.0
        ),
    )

    @After
    fun clear() {
        stopKoin()
    }

    @Test
    fun check_list_reminders() {
        val remindersList = mutableListOf(reminderList[0],reminderList[1])
        fakeDataSource = FakeDataSource(remindersList)
        reminderListViewModel = RemindersListViewModel(
            ApplicationProvider.getApplicationContext(),
            fakeDataSource)
        reminderListViewModel.loadReminders()


        assertThat(reminderListViewModel.remindersList.getOrAwaitValue(), (not(emptyList())))
        assertThat(reminderListViewModel.remindersList.getOrAwaitValue().size, `is`(remindersList.size))
    }

    @Test
    fun check_loading() {
        fakeDataSource = FakeDataSource(mutableListOf())
        reminderListViewModel = RemindersListViewModel(ApplicationProvider.getApplicationContext(),
            fakeDataSource)
        mainCoroutineRule.pauseDispatcher()
        reminderListViewModel.loadReminders()
        assertThat(
            reminderListViewModel.showLoading.getOrAwaitValue(),
            `is`(true)
        )
    }

}