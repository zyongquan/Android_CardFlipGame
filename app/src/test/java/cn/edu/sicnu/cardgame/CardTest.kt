package cn.edu.sicnu.cardgame

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 模型层 Card 的单元测试（JVM 本地测试）。
 * 实验五改为三张配对，重点验证 Card.match() 的新规则。
 */
class CardTest {

    @Test
    fun threeCardsSameRank_scoresThree() {
        val aceOfHeart = Card(suit = "♥", rank = "A")
        val aceOfSpade = Card(suit = "♠", rank = "A")
        val aceOfDiamond = Card(suit = "♦", rank = "A")

        // 三张点数相同：返回 3，控制器再乘以 MATCH_BONUS(4) 得到 12 分
        assertEquals(3, aceOfHeart.match(arrayOf(aceOfSpade, aceOfDiamond)))
    }

    @Test
    fun threeCardsDifferentRank_scoresZero() {
        val ace = Card(suit = "♥", rank = "A")
        val king = Card(suit = "♠", rank = "K")
        val queen = Card(suit = "♦", rank = "Q")

        assertEquals(0, ace.match(arrayOf(king, queen)))
    }

    @Test
    fun onlyTwoCards_scoresZero() {
        val aceOfHeart = Card(suit = "♥", rank = "A")
        val aceOfSpade = Card(suit = "♠", rank = "A")

        // 实验五是三张配对，只传入一张其它牌时不能算配对
        assertEquals(0, aceOfHeart.match(arrayOf(aceOfSpade)))
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
