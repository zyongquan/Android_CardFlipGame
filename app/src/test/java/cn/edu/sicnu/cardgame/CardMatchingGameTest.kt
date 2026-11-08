package cn.edu.sicnu.cardgame

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 模型层 CardMatchingGame 的单元测试。
 * 覆盖：初始状态、三张一组发牌、翻牌扣分、撤销选择、三张配对 /
 * 配对失败、已配对牌不可再点、reset()、整局结束与非法牌数。
 */
class CardMatchingGameTest {

    @Test
    fun newGame_startsWithZeroScoreAndNoChosenCard() {
        val game = CardMatchingGame(24)

        assertEquals(0, game.score)
        assertEquals(24, game.cardCount)
        assertEquals(0, game.matchedCount)
        assertEquals(0, game.selectedCount)
        assertFalse(game.isGameOver)
        for (index in 0 until game.cardCount) {
            assertFalse(game.cardAtIndex(index).isChosen)
            assertFalse(game.cardAtIndex(index).isMatched)
        }
    }

    @Test
    fun dealIsGroupedByThree_everyRankCountIsMultipleOfThree() {
        val game = CardMatchingGame(24)
        val rankCounts = (0 until game.cardCount)
            .map { game.cardAtIndex(it).rank }
            .groupingBy { it }
            .eachCount()

        // 三张一组发牌：每种点数出现的次数一定是 3 的倍数，保证一局可以清空
        rankCounts.values.forEach { count ->
            assertEquals(0, count % Card.CARDS_PER_MATCH)
        }
    }

    @Test
    fun chooseFirstCard_costsOnePointAndIsChosen() {
        val game = CardMatchingGame(24)

        game.chooseCardAtIndex(0)

        assertEquals(-1, game.score)
        assertEquals(1, game.selectedCount)
        assertTrue(game.cardAtIndex(0).isChosen)
        assertFalse(game.cardAtIndex(0).isMatched)
    }

    @Test
    fun chooseSameCardTwice_cancelsSelectionWithoutExtraCost() {
        val game = CardMatchingGame(24)

        game.chooseCardAtIndex(0)
        game.chooseCardAtIndex(0)

        assertEquals(-1, game.score)
        assertEquals(0, game.selectedCount)
        assertFalse(game.cardAtIndex(0).isChosen)
    }

    @Test
    fun chooseThreeCardsWithSameRank_matchesThemAndAddsBonus() {
        val game = CardMatchingGame(24)
        val (first, second, third) = findTriple(game)

        game.chooseCardAtIndex(first)
        game.chooseCardAtIndex(second)
        game.chooseCardAtIndex(third)

        // 三张牌各扣 1 分，三张点数相同得 3 × MATCH_BONUS(4) = 12 分
        assertEquals(-3 + 12, game.score)
        assertTrue(game.cardAtIndex(first).isMatched)
        assertTrue(game.cardAtIndex(second).isMatched)
        assertTrue(game.cardAtIndex(third).isMatched)
        assertEquals(3, game.matchedCount)
        assertEquals(0, game.selectedCount)
    }

    @Test
    fun chooseThreeCardsWithDifferentRanks_penalisesAndFlipsAllBack() {
        val game = CardMatchingGame(24)
        val (first, second, third) = findMismatch(game)

        game.chooseCardAtIndex(first)
        game.chooseCardAtIndex(second)
        game.chooseCardAtIndex(third)

        // 三张牌各扣 1 分，配对失败再扣 MISMATCH_PENALTY(2)
        assertEquals(-3 - game.MISMATCH_PENALTY, game.score)
        assertFalse(game.cardAtIndex(first).isChosen)
        assertFalse(game.cardAtIndex(second).isChosen)
        assertFalse(game.cardAtIndex(third).isChosen)
        assertEquals(0, game.matchedCount)
        assertEquals(0, game.selectedCount)
    }

    @Test
    fun matchedCard_cannotBeChosenAgain() {
        val game = CardMatchingGame(24)
        val (first, second, third) = findTriple(game)
        game.chooseCardAtIndex(first)
        game.chooseCardAtIndex(second)
        game.chooseCardAtIndex(third)
        val scoreAfterMatch = game.score

        game.chooseCardAtIndex(first)

        assertEquals(scoreAfterMatch, game.score)
        assertTrue(game.cardAtIndex(first).isMatched)
    }

    @Test
    fun matchingAllTriples_endsTheGameWithFullScore() {
        val game = CardMatchingGame(24)

        while (!game.isGameOver) {
            val (first, second, third) = findTriple(game)
            game.chooseCardAtIndex(first)
            game.chooseCardAtIndex(second)
            game.chooseCardAtIndex(third)
        }

        // 24 张牌共 8 组，每组 -3 + 12 = +9，完美通关共得 96 - 24 = 72 分
        assertEquals(24, game.matchedCount)
        assertEquals(72, game.score)
        assertTrue(game.isGameOver)
    }

    @Test
    fun reset_clearsScoreAndMatchedState() {
        val game = CardMatchingGame(24)
        val (first, second, third) = findTriple(game)
        game.chooseCardAtIndex(first)
        game.chooseCardAtIndex(second)
        game.chooseCardAtIndex(third)
        assertTrue(game.score != 0)

        game.reset()

        assertEquals(0, game.score)
        assertEquals(24, game.cardCount)
        assertEquals(0, game.matchedCount)
        assertEquals(0, game.selectedCount)
        assertFalse(game.isGameOver)
        for (index in 0 until game.cardCount) {
            assertFalse(game.cardAtIndex(index).isChosen)
            assertFalse(game.cardAtIndex(index).isMatched)
        }
    }

    @Test
    fun reset_canChangeCardCountForLandscape() {
        val game = CardMatchingGame(24)

        game.reset(30)

        assertEquals(30, game.cardCount)
        assertEquals(0, game.score)
        assertEquals(0, game.matchedCount)
    }

    @Test
    fun invalidCardCount_throwsException() {
        // 牌数必须是 3 的倍数
        assertThrows(IllegalArgumentException::class.java) {
            CardMatchingGame(25)
        }
    }

    /** 在还没配对的牌里找三张点数相同的牌，返回它们的下标 */
    private fun findTriple(game: CardMatchingGame): Triple<Int, Int, Int> {
        val indexesByRank = mutableMapOf<String, MutableList<Int>>()
        for (index in 0 until game.cardCount) {
            if (game.cardAtIndex(index).isMatched) continue
            val rank = game.cardAtIndex(index).rank
            indexesByRank.getOrPut(rank) { mutableListOf() }.add(index)
        }
        val group = indexesByRank.values.firstOrNull { it.size >= Card.CARDS_PER_MATCH }
            ?: throw AssertionError("没有找到三张点数相同的牌，三张一组发牌逻辑有问题")
        return Triple(group[0], group[1], group[2])
    }

    /** 找三张点数互不相同的牌，用于测试配对失败 */
    private fun findMismatch(game: CardMatchingGame): Triple<Int, Int, Int> {
        val firstIndexByRank = mutableMapOf<String, Int>()
        for (index in 0 until game.cardCount) {
            firstIndexByRank.putIfAbsent(game.cardAtIndex(index).rank, index)
        }
        val indexes = firstIndexByRank.values.take(3)
        check(indexes.size == 3)
        return Triple(indexes[0], indexes[1], indexes[2])
    }
}
