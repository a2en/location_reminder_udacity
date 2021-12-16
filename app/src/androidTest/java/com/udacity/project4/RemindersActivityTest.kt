package com.udacity.project4

import android.app.Activity
import android.app.Application
import android.view.View
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.core.app.launchActivity
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.ViewAssertion
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.activityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.GrantPermissionRule
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.monitorActivity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get
import java.util.regex.Pattern.matches
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.not
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed

import androidx.test.espresso.matcher.RootMatchers.withDecorView

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.matcher.RootMatchers
import com.udacity.project4.utils.EspressoIdlingResource


@RunWith(AndroidJUnit4::class)
@LargeTest
//END TO END test to black box test the app
class RemindersActivityTest :
    AutoCloseKoinTest() {// Extended Koin Test - embed autoclose @after method to close Koin after every test

    private lateinit var repository: ReminderDataSource
    private lateinit var appContext: Application
    private val dataBindingIdlingResource = DataBindingIdlingResource()

    @get:Rule
    var permissionRule = GrantPermissionRule.grant(android.Manifest.permission.ACCESS_FINE_LOCATION,android.Manifest.permission.ACCESS_BACKGROUND_LOCATION)


    // get activity context
    private fun getActivity(activityScenario: ActivityScenario<RemindersActivity>): Activity? {
        var activity: Activity? = null
        activityScenario.onActivity {
            activity = it
        }
        return activity
    }

    /**
     * As we use Koin as a Service Locator Library to develop our code, we'll also use Koin to test our code.
     * at this step we will initialize Koin related code to be able to use it in out testing.
     */
    @Before
    fun init() {
        stopKoin()//stop the original app koin
        appContext = getApplicationContext()
        val myModule = module {
            viewModel {
                RemindersListViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single {
                SaveReminderViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single { RemindersLocalRepository(get()) as ReminderDataSource }
            single { LocalDB.createRemindersDao(appContext) }
        }
        //declare a new koin module
        startKoin {
            modules(listOf(myModule))
        }
        //Get our real repository
        repository = get()

        //clear the data to start fresh
        runBlocking {
            repository.deleteAllReminders()
        }
    }


    @After
    fun stop() {
        stopKoin()
    }

    @Before
    fun registerIdlingResource() {
        IdlingRegistry.getInstance().register(dataBindingIdlingResource)
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
    }

    @After
    fun unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(dataBindingIdlingResource)
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
    }

    // Toast added to end to end test
    @Test
    fun check_add_reminder_end_to_end_with_toast() {

        val activityScenario = launchActivity<RemindersActivity>()
        dataBindingIdlingResource.monitorActivity(activityScenario)
        val activity = getActivity(activityScenario)

        Espresso.onView(ViewMatchers.withId(R.id.addReminderFAB)).perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withId(R.id.reminderTitle))
            .perform(ViewActions.typeText("t1"), ViewActions.closeSoftKeyboard())
        Espresso.onView(ViewMatchers.withText("t1"))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))

        Espresso.onView(ViewMatchers.withId(R.id.reminderDescription))
            .perform(ViewActions.typeText("d1"), ViewActions.closeSoftKeyboard())
        Espresso.onView(ViewMatchers.withText("d1"))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))

        Espresso.onView(ViewMatchers.withId(R.id.selectLocation)).perform(ViewActions.click())

        Thread.sleep(2000);

        Espresso.onView(ViewMatchers.withId(R.id.map)).perform(ViewActions.longClick())


        Thread.sleep(2000);

        Espresso.onView(withId(R.id.pick_location)).perform(ViewActions.click())

        Thread.sleep(2000);


        Espresso.onView(ViewMatchers.withId(R.id.saveReminder)).perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withText("t1"))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        Espresso.onView(ViewMatchers.withText("d1"))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))

        onView(withText(R.string.reminder_saved)).inRoot(withDecorView(not(`is`(activity?.window?.decorView)))
        ).check(ViewAssertions.matches(isDisplayed()))

        activityScenario.close()
    }


    @Test
    fun check_add_reminder_snack_bar(){
        val activityScenario = launchActivity<RemindersActivity>()
        dataBindingIdlingResource.monitorActivity(activityScenario)

        onView(withId(R.id.addReminderFAB)).perform(ViewActions.click())

        onView(withId(R.id.saveReminder)).perform(ViewActions.click())

        onView(withId(com.google.android.material.R.id.snackbar_text))
            .check(ViewAssertions.matches(withText(R.string.err_enter_title)))
    }

}
