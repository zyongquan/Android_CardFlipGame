package cn.edu.sicnu.cardgame

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Model 层 Card 的单元测试。
 *
 * 实验六同时支持两张配对和三张配对，重点验证 match() 对 1 张 / 2 张其它牌的返回值。
 */
class CardTest {

    @Test
    fun twoCardsSameRank_scoresFour() {
        val aceOfHeart = Card(suit = "♥", rank = "A")
        val aceOfSpade = Card(suit = "♠", rank = "A")

        // 两张玩法：同点数返回 4，控制器乘以 MATCH_BONUS(4) 得到 16 分
        assertEquals(4, aceOfHeart.match(arrayOf(aceOfSpade)))
    }

    @Test
    fun threeCardsSameRank_scoresThree() {
        val aceOfHeart = Card(suit = "♥", rank = "A")
        val aceOfSpade = Card(suit = "♠", rank = "A")
        val aceOfDiamond = Card(suit = "♦", rank = "A")

        // 三张玩法：同点数返回 3，控制器乘以 MATCH_BONUS(4) 得到 12 分
        assertEquals(3, aceOfHeart.match(arrayOf(aceOfSpade, aceOfDiamond)))
    }

    @Test
    fun threeCardsOneDifferent_scoresZero() {
        val ace = Card(suit = "♥", rank = "A")
        val otherAce = Card(suit = "♠", rank = "A")
        val king = Card(suit = "♦", rank = "K")

        assertEquals(0, ace.match(arrayOf(otherAce, king)))
    }

    @Test
    fun sameSuitDifferentRank_scoresZero() {
        val aceOfHeart = Card(suit = "♥", rank = "A")
        val kingOfHeart = Card(suit = "♥", rank = "K")
        val queenOfHeart = Card(suit = "♥", rank = "Q")

        // 花色不参与配对，避免出现无法清空的“同花但不同点数”组合
        assertEquals(0, aceOfHeart.match(arrayOf(kingOfHeart, queenOfHeart)))
    }

    @Test
    fun emptyOtherCards_scoresZero() {
        assertEquals(0, Card(suit = "♥", rank = "A").match(emptyArray()))
    }

    @Test
    fun toString_returnsSuitPlusRank() {
        assertEquals("♠10", Card(suit = "♠", rank = "10").toString())
    }

    @Test
    fun isRed_onlyForHeartAndDiamond() {
        assertTrue(Card(suit = "♥", rank = "A").isRed)
        assertTrue(Card(suit = "♦", rank = "A").isRed)
        assertFalse(Card(suit = "♠", rank = "A").isRed)
        assertFalse(Card(suit = "♣", rank = "A").isRed)
    }

    @Test
    fun newCard_isNotChosenNorMatched() {
        val card = Card(suit = "♥", rank = "A")

        assertFalse(card.isChosen)
        assertFalse(card.isMatched)
    }
}
