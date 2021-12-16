package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource(var tasks: MutableList<ReminderDTO>? = mutableListOf()) : ReminderDataSource {

    private var shouldReturnError = false

    fun setShouldReturnError(shouldReturn: Boolean) {
        this.shouldReturnError = shouldReturn
    }

    override suspend fun getReminders(): Result<List<ReminderDTO>> {

        if (shouldReturnError) {
            return Result.Error("No reminders to return", 404)
        }

        tasks?.let { return Result.Success(it) }
        return Result.Error("No reminders to return")
    }

    override suspend fun saveReminder(reminder: ReminderDTO){
        tasks?.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        if(shouldReturnError) {
            return Result.Error("Error")
        }

        tasks?.firstOrNull { it.id == id }?.let { return Result.Success(it) }
        return Result.Error("No reminder found")
    }

    override suspend fun deleteAllReminders() {
        tasks = mutableListOf()
    }




}