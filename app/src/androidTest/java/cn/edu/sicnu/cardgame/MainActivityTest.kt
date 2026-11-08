package cn.edu.sicnu.cardgame

import android.content.Context
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * 主界面的 UI 测试（仪器化测试，需要模拟器 / 真机）。
 *
 * 覆盖：玩家信息显示、菜单「关于」对话框、进入信息修改界面修改后回传、
 * 取消修改、进入并退出翻牌游戏界面。
 */
@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        // 每个用例开始前把玩家资料重置为默认值，避免用例之间互相影响
        PlayerRepository.save(
            Player(
                name = context.getString(R.string.author_name),
                studentId = context.getString(R.string.author_student_id),
                phone = "",
                email = "",
                gender = Gender.MALE,
                hobbies = mutableSetOf(Hobby.READING, Hobby.GAME)
            )
        )
    }

    @Test
    fun showsPlayerInfoFromRepository() {
        ActivityScenario.launch(MainActivity::class.java).use {
            onView(withId(R.id.tv_name))
                .check(matches(withText(context.getString(R.string.author_name))))
            onView(withId(R.id.tv_student_id))
                .check(matches(withText(context.getString(R.string.author_student_id))))
            onView(withId(R.id.tv_gender))
                .check(matches(withText(context.getString(R.string.gender_male))))
            onView(withId(R.id.tv_high_score))
                .check(
                    matches(withText(context.getString(R.string.main_high_score_format, 0)))
                )
        }
    }

    @Test
    fun aboutMenu_showsAboutDialog() {
        ActivityScenario.launch(MainActivity::class.java).use {
            // 点击 ActionBar 上的「关于」菜单项
            onView(withId(R.id.action_about)).perform(click())

            onView(withText(context.getString(R.string.about_title)))
                .check(matches(isDisplayed()))
            onView(withText(context.getString(R.string.about_ok))).perform(click())
            onView(withId(R.id.tv_name)).check(matches(isDisplayed()))
        }
    }

    @Test
    fun editInfo_savesNewNameAndGenderBackToMain() {
        ActivityScenario.launch(MainActivity::class.java).use {
            onView(withId(R.id.btn_edit_info)).perform(click())

            // 进入信息修改界面
            onView(withId(R.id.et_name)).check(matches(isDisplayed()))
            onView(withId(R.id.et_name)).perform(replaceText("李小明"), closeSoftKeyboard())
            onView(withId(R.id.rb_female)).perform(click())
            onView(withId(R.id.btn_save)).perform(click())

            // 回到主界面，界面上已经是修改后的内容
            onView(withId(R.id.tv_name)).check(matches(withText("李小明")))
            onView(withId(R.id.tv_gender))
                .check(matches(withText(context.getString(R.string.gender_female))))
            // 数据也写回了内存中的仓库
            onView(withId(R.id.btn_start_game)).check(matches(isDisplayed()))
        }
    }

    @Test
    fun cancelEdit_keepsOriginalInfo() {
        ActivityScenario.launch(MainActivity::class.java).use {
            onView(withId(R.id.btn_edit_info)).perform(click())
            onView(withId(R.id.et_name)).perform(replaceText("不应保存"), closeSoftKeyboard())
            onView(withId(R.id.btn_cancel)).perform(click())

            onView(withId(R.id.tv_name))
                .check(matches(withText(context.getString(R.string.author_name))))
        }
    }

    @Test
    fun openCardGameAndExit_returnsToMain() {
        ActivityScenario.launch(MainActivity::class.java).use {
            onView(withId(R.id.btn_start_game)).perform(click())
            onView(withId(R.id.rv_cards)).check(matches(isDisplayed()))
            onView(withId(R.id.btn_exit)).perform(click())

            onView(withId(R.id.btn_edit_info)).check(matches(isDisplayed()))
        }
    }
}
