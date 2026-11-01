package cn.edu.sicnu.cardgame

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 模型层 Card 的单元测试（JVM 本地测试）。
 * 主要验证实验附件里给出的配对计分规则。
 */
class CardTest {

    @Test
    fun sameRankDifferentSuit_scoresFour() {
        val aceOfHeart = Card(suit = "♥", rank = "A")
        val aceOfSpade = Card(suit = "♠", rank = "A")

        assertEquals(4, aceOfHeart.match(arrayOf(aceOfSpade)))
    }

    @Test
    fun sameSuitDifferentRank_scoresOne() {
        val aceOfHeart = Card(suit = "♥", rank = "A")
        val kingOfHeart = Card(suit = "♥", rank = "K")

        assertEquals(1, aceOfHeart.match(arrayOf(kingOfHeart)))
    }

    @Test
    fun differentRankAndSuit_scoresZero() {
        val aceOfHeart = Card(suit = "♥", rank = "A")
        val kingOfSpade = Card(suit = "♠", rank = "K")

        assertEquals(0, aceOfHeart.match(arrayOf(kingOfSpade)))
    }

    @Test
    fun moreThanOneOtherCard_scoresZero() {
        val card = Card(suit = "♥", rank = "A")
        val otherCards = arrayOf(
            Card(suit = "♥", rank = "A"),
            Card(suit = "♠", rank = "A")
        )

        assertEquals(0, card.match(otherCards))
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
