package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.util.MainCoroutineRule
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.`is`
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

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    private val reminderList =  listOf(ReminderDTO("title1", "description1",
        "location1",0.0,0.0),
        ReminderDTO("title1", "description2",
            "location2",0.0,0.0))



    private lateinit var fakeRemindersDao: FakeReminderDao
    private lateinit var remindersLocalRepository: RemindersLocalRepository

    @Before
    fun setup() {
        fakeRemindersDao = FakeReminderDao()
        remindersLocalRepository = RemindersLocalRepository(
            fakeRemindersDao, Dispatchers.Unconfined
        )
    }

    @Test
    fun check_save_to_local() = runBlockingTest {
        var tempList = mutableListOf<ReminderDTO>()
        tempList.addAll(fakeRemindersDao.remindersServiceData.values)
        assertThat(tempList).doesNotContain(reminderList[0])
        assertThat((remindersLocalRepository.getReminders() as? Result.Success)?.data).doesNotContain(
            reminderList[0]
        )
        assertThat((remindersLocalRepository.getReminders() as? Result.Success)?.data).doesNotContain(
            reminderList[1]
        )

        remindersLocalRepository.saveReminder(reminderList[0])
        remindersLocalRepository.saveReminder(reminderList[1])

        tempList = mutableListOf()
        tempList.addAll(fakeRemindersDao.remindersServiceData.values)
        assertThat(tempList).contains(reminderList[0])
        assertThat(tempList).contains(reminderList[1])

        val result = remindersLocalRepository.getReminders() as? Result.Success
        assertThat(result?.data).contains(reminderList[0])
        assertThat(result?.data).contains(reminderList[1])
    }

    @Test
    fun check_delete_all_fetch_empty() = runBlockingTest {
        assertThat((remindersLocalRepository.getReminders() as? Result.Success)?.data).isEmpty()

        fakeRemindersDao.remindersServiceData[reminderList[0].id] = reminderList[0]
        fakeRemindersDao.remindersServiceData[reminderList[1].id] = reminderList[1]

        assertThat((remindersLocalRepository.getReminders() as? Result.Success)?.data).isNotEmpty()
        remindersLocalRepository.deleteAllReminders()

        assertThat((remindersLocalRepository.getReminders() as? Result.Success)?.data).isEmpty()
    }

    @Test
    fun check_existing_id() = runBlockingTest {
        assertThat((remindersLocalRepository.getReminder(reminderList[0].id) as? Result.Error)?.message)
            .isEqualTo(
            "Reminder not found!")

        fakeRemindersDao.remindersServiceData[reminderList[0].id] = reminderList[0]

        val loadedReminder = (remindersLocalRepository.getReminder(reminderList[0].id) as? Result.Success)?.data

        Assert.assertThat<ReminderDTO>(loadedReminder as ReminderDTO, CoreMatchers.notNullValue())
        Assert.assertThat(loadedReminder.id, `is`(reminderList[0].id))
        Assert.assertThat(loadedReminder.title, `is`(reminderList[0].title))
        Assert.assertThat(loadedReminder.description, `is`(reminderList[0].description))
        Assert.assertThat(loadedReminder.location, `is`(reminderList[0].location))
        Assert.assertThat(loadedReminder.latitude, `is`(reminderList[0].latitude))
        Assert.assertThat(loadedReminder.longitude, `is`(reminderList[0].longitude))
    }

    @Test
    fun check_reminder_not_found() = runBlockingTest {
        val message = (remindersLocalRepository.getReminder(reminderList[0].id) as? Result.Error)?.message
        Assert.assertThat<String>(message, CoreMatchers.notNullValue())
        assertThat(message).isEqualTo("Reminder not found!")

    }

}