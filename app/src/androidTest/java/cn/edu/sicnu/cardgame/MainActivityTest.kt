package cn.edu.sicnu.cardgame

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * 多 Fragment 主界面的 UI 测试。
 *
 * 覆盖：默认显示个人信息 Fragment、底部导航切换 2 张牌 / 3 张牌 Fragment、
 * 「关于」对话框、个人信息保存后重建恢复、2 张牌牌局重建恢复。
 */
@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        clearSavedFiles()
    }

    @After
    fun tearDown() {
        clearSavedFiles()
    }

    private fun clearSavedFiles() {
        context.deleteFile(PlayerStore.FILE_NAME)
        context.deleteFile("two_card_game.dat")
        context.deleteFile("three_card_game.dat")
    }

    @Test
    fun launch_showsPersonalInfoFragment() {
        ActivityScenario.launch(MainActivity::class.java).use {
            onView(withId(R.id.bottom_nav)).check(matches(isDisplayed()))
            onView(withId(R.id.et_name)).check(matches(isDisplayed()))
            onView(withId(R.id.et_name))
                .check(matches(withText(context.getString(R.string.author_name))))
        }
    }

    @Test
    fun bottomNav_switchesBetweenTwoAndThreeCardGames() {
        ActivityScenario.launch(MainActivity::class.java).use {
            onView(withId(R.id.nav_two_card)).perform(click())
            onView(withId(R.id.tv_game_title))
                .check(matches(withText(context.getString(R.string.game_title_two))))
            onView(withId(R.id.rv_cards)).check(matches(isDisplayed()))

            onView(withId(R.id.nav_three_card)).perform(click())
            onView(withId(R.id.tv_game_title))
                .check(matches(withText(context.getString(R.string.game_title_three))))
            onView(withId(R.id.rv_cards)).check(matches(isDisplayed()))

            onView(withId(R.id.nav_personal)).perform(click())
            onView(withId(R.id.et_name)).check(matches(isDisplayed()))
        }
    }

    @Test
    fun aboutMenu_showsAboutDialog() {
        ActivityScenario.launch(MainActivity::class.java).use {
            onView(withId(R.id.action_about)).perform(click())
            onView(withText(context.getString(R.string.about_title)))
                .check(matches(isDisplayed()))
            onView(withText(context.getString(R.string.about_ok))).perform(click())
            onView(withId(R.id.et_name)).check(matches(isDisplayed()))
        }
    }

    @Test
    fun personalInfo_savedAndRestoredAfterRecreate() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            onView(withId(R.id.et_name))
                .perform(replaceText("测试玩家"), closeSoftKeyboard())
            onView(withId(R.id.et_phone))
                .perform(replaceText("13900139000"), closeSoftKeyboard())
            onView(withId(R.id.btn_save)).perform(click())

            scenario.recreate()

            onView(withId(R.id.et_name)).check(matches(withText("测试玩家")))
            onView(withId(R.id.et_phone)).check(matches(withText("13900139000")))
            onView(withId(R.id.tv_status)).check(
                matches(withText(context.getString(R.string.personal_status_loaded)))
            )
        }
    }

    @Test
    fun twoCardGameState_restoredAfterRecreate() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            onView(withId(R.id.nav_two_card)).perform(click())
            onView(withId(R.id.rv_cards)).perform(
                RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(0, click())
            )
            onView(withId(R.id.tv_score))
                .check(matches(withText(context.getString(R.string.game_score_format, -1))))

            scenario.recreate()

            onView(withId(R.id.tv_game_title))
                .check(matches(withText(context.getString(R.string.game_title_two))))
            onView(withId(R.id.tv_score))
                .check(matches(withText(context.getString(R.string.game_score_format, -1))))
            onView(withId(R.id.tv_selection)).check(
                matches(
                    withText(
                        context.getString(R.string.game_selection_format, 1, 2)
                    )
                )
            )
        }
    }
}
