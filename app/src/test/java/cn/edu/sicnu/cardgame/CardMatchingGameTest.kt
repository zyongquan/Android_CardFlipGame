package cn.edu.sicnu.cardgame

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 模型层 CardMatchingGame 的单元测试。
 * 覆盖：初始状态、翻第一张牌扣分、再点一次取消选择、配对得分、配对失败扣分、整局结束。
 */
class CardMatchingGameTest {

    @Test
    fun newGame_startsWithZeroScoreAndNoChosenCard() {
        val game = CardMatchingGame(12)

        assertEquals(0, game.score)
        assertEquals(12, game.cardCount)
        assertEquals(0, game.matchedCount)
        assertFalse(game.isGameOver)
        for (index in 0 until game.cardCount) {
            assertFalse(game.cardAtIndex(index).isChosen)
            assertFalse(game.cardAtIndex(index).isMatched)
        }
    }

    @Test
    fun dealIsPaired_everyRankHasAnEvenCount() {
        // 成对发牌：每种点数出现的次数一定是偶数，保证一局可以清空
        val game = CardMatchingGame(12)
        val rankCounts = (0 until game.cardCount)
            .map { game.cardAtIndex(it).rank }
            .groupingBy { it }
            .eachCount()

        rankCounts.values.forEach { count ->
            assertEquals(0, count % 2)
        }
    }

    @Test
    fun chooseFirstCard_costsOnePointAndIsChosen() {
        val game = CardMatchingGame(12)

        game.chooseCardAtIndex(0)

        assertEquals(-1, game.score)
        assertTrue(game.cardAtIndex(0).isChosen)
        assertFalse(game.cardAtIndex(0).isMatched)
    }

    @Test
    fun chooseSameCardTwice_cancelsSelectionWithoutExtraCost() {
        val game = CardMatchingGame(12)

        game.chooseCardAtIndex(0)
        game.chooseCardAtIndex(0)

        assertEquals(-1, game.score)
        assertFalse(game.cardAtIndex(0).isChosen)
    }

    @Test
    fun chooseTwoCardsWithSameRank_matchesBothAndAddsBonus() {
        val game = CardMatchingGame(12)
        val (first, second) = findPairWithSameRank(game)

        game.chooseCardAtIndex(first)
        game.chooseCardAtIndex(second)

        // 第一张 -1，第二张：点数相同 4 × MATCH_BONUS(4) = 16，再扣 1 分
        assertEquals(-1 + 16 - 1, game.score)
        assertTrue(game.cardAtIndex(first).isMatched)
        assertTrue(game.cardAtIndex(second).isMatched)
        assertEquals(2, game.matchedCount)
    }

    @Test
    fun chooseTwoCardsWithDifferentRankAndSuit_penalisesAndFlipsFirstCardBack() {
        val game = CardMatchingGame(12)
        val (first, second) = findPairWithDifferentRankAndSuit(game)

        game.chooseCardAtIndex(first)
        game.chooseCardAtIndex(second)

        // 第一张 -1；第二张：配对失败 -MISMATCH_PENALTY(2)，再扣 COST_TO_CHOOSE(1)
        assertEquals(-1 - 2 - 1, game.score)
        assertFalse(game.cardAtIndex(first).isChosen)
        assertTrue(game.cardAtIndex(second).isChosen)
        assertEquals(0, game.matchedCount)
    }

    @Test
    fun matchedCard_cannotBeChosenAgain() {
        val game = CardMatchingGame(12)
        val (first, second) = findPairWithSameRank(game)
        game.chooseCardAtIndex(first)
        game.chooseCardAtIndex(second)
        val scoreAfterMatch = game.score

        game.chooseCardAtIndex(first)

        assertEquals(scoreAfterMatch, game.score)
        assertTrue(game.cardAtIndex(first).isMatched)
    }

    @Test
    fun matchingAllPairs_endsTheGame() {
        val game = CardMatchingGame(12)

        while (!game.isGameOver) {
            val (first, second) = findPairWithSameRank(game)
            game.chooseCardAtIndex(first)
            game.chooseCardAtIndex(second)
        }

        assertEquals(12, game.matchedCount)
        assertTrue(game.isGameOver)
    }

    /** 在还没配对的牌里找一对点数相同的牌，返回它们的下标 */
    private fun findPairWithSameRank(game: CardMatchingGame): Pair<Int, Int> {
        for (i in 0 until game.cardCount) {
            if (game.cardAtIndex(i).isMatched) continue
            for (j in i + 1 until game.cardCount) {
                if (game.cardAtIndex(j).isMatched) continue
                if (game.cardAtIndex(i).rank == game.cardAtIndex(j).rank) return i to j
            }
        }
        throw AssertionError("没有找到点数相同的牌，成对发牌逻辑有问题")
    }

    /** 找两张点数、花色都不相同的牌，用于测试配对失败 */
    private fun findPairWithDifferentRankAndSuit(game: CardMatchingGame): Pair<Int, Int> {
        for (i in 0 until game.cardCount) {
            for (j in i + 1 until game.cardCount) {
                val a = game.cardAtIndex(i)
                val b = game.cardAtIndex(j)
                if (a.rank != b.rank && a.suit != b.suit) return i to j
            }
        }
        throw AssertionError("没有找到点数与花色都不同的牌")
    }
}
