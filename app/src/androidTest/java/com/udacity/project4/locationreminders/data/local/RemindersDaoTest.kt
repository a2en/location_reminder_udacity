package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import junit.framework.Assert
import junit.framework.Assert.assertNull

import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;

import kotlinx.coroutines.ExperimentalCoroutinesApi;
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Test
import java.util.*

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {

    private lateinit var database: RemindersDatabase

    @Before
    fun initDb() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java).build()
    }

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @After
    fun closeDb() = database.close()


    @Test
    fun check_select_reminders() = runBlockingTest {
        val reminder = ReminderDTO("title1", "description1","location1",
            0.0,0.0)
        database.reminderDao().saveReminder(reminder)

        val reminders = database.reminderDao().getReminders()

        assertThat(reminders.size, `is`(1))
        assertThat(reminders[0].id, `is`(reminder.id))
        assertThat(reminders[0].title, `is`(reminder.title))
        assertThat(reminders[0].description, `is`(reminder.description))
        assertThat(reminders[0].location, `is`(reminder.location))
        assertThat(reminders[0].latitude, `is`(reminder.latitude))
        assertThat(reminders[0].longitude, `is`(reminder.longitude))
    }


    @Test
    fun check_insert_reminder() = runBlockingTest {
        val reminder = ReminderDTO("title 1", "description 1",
            "location 1",0.0,0.0)
        database.reminderDao().saveReminder(reminder)

        val loaded = database.reminderDao().getReminderById(reminder.id)

        assertThat<ReminderDTO>(loaded as ReminderDTO, notNullValue())
        assertThat(loaded.id, `is`(reminder.id))
        assertThat(loaded.title, `is`(reminder.title))
        assertThat(loaded.description, `is`(reminder.description))
        assertThat(loaded.location, `is`(reminder.location))
        assertThat(loaded.latitude, `is`(reminder.latitude))
        assertThat(loaded.longitude, `is`(reminder.longitude))
    }

    @Test
    fun check_delete_reminders() = runBlockingTest {
        val remindersList = listOf<ReminderDTO>(ReminderDTO("title1", "description1",
            "location1",0.0,0.0),
            ReminderDTO("title2", "description2",
                "location2",0.0,0.0))
        remindersList.forEach {
            database.reminderDao().saveReminder(it)
        }

        database.reminderDao().deleteAllReminders()

        val reminders = database.reminderDao().getReminders()
        assertThat(reminders.isEmpty(), `is`(true))
    }

    @Test
    fun check_reminder_nil_existence() = runBlockingTest {
        val reminderId = UUID.randomUUID().toString()

        val loaded = database.reminderDao().getReminderById(reminderId)

        assertNull(loaded)
    }



}