package cn.edu.sicnu.cardgame

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * 翻牌游戏界面的 UI 测试。
 *
 * 覆盖：初始得分、RecyclerView 牌数、翻第一张牌扣 1 分、
 * 再点一次取消选择、重新开始后得分清零。
 */
@RunWith(AndroidJUnit4::class)
class CardGameActivityTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
    }

    /** 当前屏幕方向对应的牌数：竖屏 24、横屏 30 */
    private fun expectedCardCount(): Int = context.resources.getInteger(R.integer.card_count)

    @Test
    fun newGame_showsZeroScore() {
        ActivityScenario.launch(CardGameActivity::class.java).use {
            onView(withId(R.id.tv_score))
                .check(matches(withText(context.getString(R.string.game_score_format, 0))))
            onView(withId(R.id.tv_progress)).check(
                matches(
                    withText(
                        context.getString(
                            R.string.game_progress_format,
                            0,
                            expectedCardCount()
                        )
                    )
                )
            )
            onView(withId(R.id.tv_selection))
                .check(matches(withText(context.getString(R.string.game_selection_hint))))
        }
    }

    @Test
    fun recyclerView_hasCardsForCurrentOrientation() {
        ActivityScenario.launch(CardGameActivity::class.java).use {
            onView(withId(R.id.rv_cards)).check { view, _ ->
                val recyclerView = view as RecyclerView
                assertEquals(expectedCardCount(), recyclerView.adapter?.itemCount)
            }
        }
    }

    @Test
    fun flipFirstCard_costsOnePoint() {
        ActivityScenario.launch(CardGameActivity::class.java).use {
            onView(withId(R.id.rv_cards)).check(matches(isDisplayed()))
            onView(withId(R.id.rv_cards))
                .perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(0, click()))

            onView(withId(R.id.tv_score))
                .check(matches(withText(context.getString(R.string.game_score_format, -1))))
        }
    }

    @Test
    fun flipSameCardTwice_keepsScoreAtMinusOne() {
        ActivityScenario.launch(CardGameActivity::class.java).use {
            onView(withId(R.id.rv_cards))
                .perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(0, click()))
            onView(withId(R.id.rv_cards))
                .perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(0, click()))

            // 第二次点击是取消选择，不再扣分
            onView(withId(R.id.tv_score))
                .check(matches(withText(context.getString(R.string.game_score_format, -1))))
        }
    }

    @Test
    fun resetGame_clearsScore() {
        ActivityScenario.launch(CardGameActivity::class.java).use {
            onView(withId(R.id.rv_cards))
                .perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(0, click()))
            onView(withId(R.id.btn_reset_game)).perform(click())

            onView(withId(R.id.tv_score))
                .check(matches(withText(context.getString(R.string.game_score_format, 0))))
            onView(withId(R.id.tv_progress)).check(
                matches(
                    withText(
                        context.getString(
                            R.string.game_progress_format,
                            0,
                            expectedCardCount()
                        )
                    )
                )
            )
        }
    }
}
