package cn.edu.sicnu.cardgame

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isChecked
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * 信息修改界面的 UI 测试。
 *
 * 覆盖：表单回填、保存后写回仓库、姓名为空时不允许保存、取消不修改数据。
 */
@RunWith(AndroidJUnit4::class)
class PersonalInfoActivityTest {

    private lateinit var context: Context
    private lateinit var player: Player

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        player = Player(
            name = context.getString(R.string.author_name),
            studentId = context.getString(R.string.author_student_id),
            phone = "",
            email = "",
            gender = Gender.MALE,
            hobbies = mutableSetOf(Hobby.READING, Hobby.GAME)
        )
        PlayerRepository.save(player)
    }

    private fun launch(): ActivityScenario<PersonalInfoActivity> {
        val intent = Intent(context, PersonalInfoActivity::class.java)
            .putExtra(PersonalInfoActivity.EXTRA_PLAYER, player)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return ActivityScenario.launch(intent)
    }

    @Test
    fun formIsFilledWithCurrentPlayer() {
        launch().use {
            onView(withId(R.id.et_name))
                .check(matches(withText(context.getString(R.string.author_name))))
            onView(withId(R.id.rb_male)).check(matches(isChecked()))
            onView(withId(R.id.cb_reading)).check(matches(isChecked()))
        }
    }

    @Test
    fun save_writesBackToRepository() {
        launch().use {
            onView(withId(R.id.et_name)).perform(replaceText("王小红"), closeSoftKeyboard())
            onView(withId(R.id.et_phone)).perform(replaceText("13900139000"), closeSoftKeyboard())
            onView(withId(R.id.rb_female)).perform(click())
            // 小屏上「爱好」在可滚动区域的下方，先滚动到可见位置再点击
            onView(withId(R.id.cb_travel)).perform(scrollTo(), click())
            onView(withId(R.id.btn_save)).perform(click())
        }

        val saved = PlayerRepository.get(context)
        assertEquals("王小红", saved.name)
        assertEquals("13900139000", saved.phone)
        assertEquals(Gender.FEMALE, saved.gender)
        assertTrue(saved.hobbies.contains(Hobby.TRAVEL))
    }

    @Test
    fun saveWithEmptyName_doesNotLeaveTheScreen() {
        launch().use {
            onView(withId(R.id.et_name)).perform(replaceText(""), closeSoftKeyboard())
            onView(withId(R.id.btn_save)).perform(click())

            // 姓名不能为空：界面仍然停留在信息修改页
            onView(withId(R.id.et_name)).check(matches(isDisplayed()))
        }
    }

    @Test
    fun cancel_doesNotChangeRepository() {
        launch().use {
            onView(withId(R.id.et_name)).perform(replaceText("不应保存"), closeSoftKeyboard())
            onView(withId(R.id.btn_cancel)).perform(click())
        }

        assertEquals(
            context.getString(R.string.author_name),
            PlayerRepository.get(context).name
        )
    }
}
