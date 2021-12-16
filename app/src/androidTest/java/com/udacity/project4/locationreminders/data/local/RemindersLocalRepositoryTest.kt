package com.udacity.project4.locationreminders.data.local

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.util.MainCoroutineRule
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var application: Application
    private lateinit var remindersDatabase: RemindersDatabase

    @Before
    fun setupDatabase() {
        application = ApplicationProvider.getApplicationContext()
        remindersDatabase = Room.inMemoryDatabaseBuilder(application, RemindersDatabase::class.java)
            .allowMainThreadQueries().build()
    }

    @Test
    fun insertEqualsRetrieve() = runBlocking {
        val reminder = ReminderDTO("Title", "Description", "Location", 0.0, 0.0)

        remindersDatabase.reminderDao().saveReminder(reminder)
        val reminder2: ReminderDTO? = remindersDatabase.reminderDao().getReminderById(reminder.id)

        MatcherAssert.assertThat(reminder2, `is`(reminder))
    }

    @Test
    fun noReminderForDeleted() = runBlocking {
        val reminder = ReminderDTO("Title", "Description", "Location", 0.0, 0.0)
        val id = reminder.id
        remindersDatabase.reminderDao().saveReminder(reminder)
        remindersDatabase.reminderDao().deleteAllReminders()

        val result = remindersDatabase.reminderDao().getReminderById(id)

        MatcherAssert.assertThat(result, `is`(CoreMatchers.nullValue()))
    }
}