package cn.edu.sicnu.cardgame

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Model 层 CardMatchingGame 的单元测试。
 *
 * 覆盖三张玩法、两张玩法、发牌、撤销选择、配对/失败、reset()、整局结束和非法牌数。
 */
class CardMatchingGameTest {

    // ==================== 三张牌玩法 ====================

    @Test
    fun newThreeCardGame_startsWithZeroScore() {
        val game = CardMatchingGame(24, 3)

        assertEquals(0, game.score)
        assertEquals(24, game.cardCount)
        assertEquals(3, game.cardsPerMatch)
        assertEquals(0, game.matchedCount)
        assertEquals(0, game.selectedCount)
        assertFalse(game.isGameOver)
        for (index in 0 until game.cardCount) {
            assertFalse(game.cardAtIndex(index).isChosen)
            assertFalse(game.cardAtIndex(index).isMatched)
        }
    }

    @Test
    fun threeCardDeal_everyRankCountIsMultipleOfThree() {
        val game = CardMatchingGame(24, 3)
        val rankCounts = (0 until game.cardCount)
            .map { game.cardAtIndex(it).rank }
            .groupingBy { it }
            .eachCount()

        rankCounts.values.forEach { count ->
            assertEquals(0, count % 3)
        }
    }

    @Test
    fun chooseFirstCard_costsOnePoint() {
        val game = CardMatchingGame(24, 3)

        game.chooseCardAtIndex(0)

        assertEquals(-1, game.score)
        assertEquals(1, game.selectedCount)
        assertTrue(game.cardAtIndex(0).isChosen)
    }

    @Test
    fun chooseSameCardTwice_cancelsSelection() {
        val game = CardMatchingGame(24, 3)

        game.chooseCardAtIndex(0)
        game.chooseCardAtIndex(0)

        assertEquals(-1, game.score)
        assertEquals(0, game.selectedCount)
        assertFalse(game.cardAtIndex(0).isChosen)
    }

    @Test
    fun chooseThreeSameRank_scoresTwelve() {
        val game = CardMatchingGame(24, 3)
        val (first, second, third) = findTriple(game)

        game.chooseCardAtIndex(first)
        game.chooseCardAtIndex(second)
        game.chooseCardAtIndex(third)

        // 三张各扣 1 分，三张同点数加 3 × 4 = 12 分
        assertEquals(9, game.score)
        assertEquals(3, game.matchedCount)
        assertEquals(0, game.selectedCount)
    }

    @Test
    fun chooseThreeDifferentRanks_resetsAllAndPenalises() {
        val game = CardMatchingGame(24, 3)
        val (first, second, third) = findThreeDifferentRanks(game)

        game.chooseCardAtIndex(first)
        game.chooseCardAtIndex(second)
        game.chooseCardAtIndex(third)

        assertEquals(-5, game.score)
        assertEquals(0, game.matchedCount)
        assertEquals(0, game.selectedCount)
        assertFalse(game.cardAtIndex(first).isChosen)
        assertFalse(game.cardAtIndex(second).isChosen)
        assertFalse(game.cardAtIndex(third).isChosen)
    }

    @Test
    fun matchedCard_cannotBeChosenAgain() {
        val game = CardMatchingGame(24, 3)
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
    fun matchingAllTriples_endsGameWith72() {
        val game = CardMatchingGame(24, 3)

        while (!game.isGameOver) {
            val (first, second, third) = findTriple(game)
            game.chooseCardAtIndex(first)
            game.chooseCardAtIndex(second)
            game.chooseCardAtIndex(third)
        }

        assertEquals(24, game.matchedCount)
        assertEquals(72, game.score)
        assertTrue(game.isGameOver)
    }

    @Test
    fun reset_clearsScoreAndMatchedState() {
        val game = CardMatchingGame(24, 3)
        val (first, second, third) = findTriple(game)
        game.chooseCardAtIndex(first)
        game.chooseCardAtIndex(second)
        game.chooseCardAtIndex(third)
        assertTrue(game.score != 0)

        game.reset()

        assertEquals(0, game.score)
        assertEquals(24, game.cardCount)
        assertEquals(3, game.cardsPerMatch)
        assertEquals(0, game.matchedCount)
        assertEquals(0, game.selectedCount)
        assertFalse(game.isGameOver)
    }

    // ==================== 两张牌玩法 ====================

    @Test
    fun newTwoCardGame_has12CardsAndCardsPerMatchTwo() {
        val game = CardMatchingGame(12, 2)

        assertEquals(12, game.cardCount)
        assertEquals(2, game.cardsPerMatch)
        assertEquals(0, game.score)
        assertEquals(0, game.matchedCount)
    }

    @Test
    fun twoCardDeal_everyRankCountIsMultipleOfTwo() {
        val game = CardMatchingGame(12, 2)
        val rankCounts = (0 until game.cardCount)
            .map { game.cardAtIndex(it).rank }
            .groupingBy { it }
            .eachCount()

        rankCounts.values.forEach { count ->
            assertEquals(0, count % 2)
        }
    }

    @Test
    fun chooseTwoSameRank_scoresSixteen() {
        val game = CardMatchingGame(12, 2)
        val (first, second) = findPair(game)

        game.chooseCardAtIndex(first)
        game.chooseCardAtIndex(second)

        // 两张各扣 1 分，同点数加 4 × 4 = 16 分
        assertEquals(14, game.score)
        assertEquals(2, game.matchedCount)
        assertEquals(0, game.selectedCount)
    }

    @Test
    fun chooseTwoDifferentRanks_flipsBothBackAndPenalises() {
        val game = CardMatchingGame(12, 2)
        val (first, second) = findDifferentPair(game)

        game.chooseCardAtIndex(first)
        game.chooseCardAtIndex(second)

        assertEquals(-4, game.score)
        assertEquals(0, game.matchedCount)
        assertEquals(0, game.selectedCount)
        assertFalse(game.cardAtIndex(first).isChosen)
        assertFalse(game.cardAtIndex(second).isChosen)
    }

    @Test
    fun matchingAllPairs_endsGameWith84() {
        val game = CardMatchingGame(12, 2)

        while (!game.isGameOver) {
            val (first, second) = findPair(game)
            game.chooseCardAtIndex(first)
            game.chooseCardAtIndex(second)
        }

        assertEquals(12, game.matchedCount)
        assertEquals(84, game.score)
        assertTrue(game.isGameOver)
    }

    @Test
    fun invalidCardCount_throwsException() {
        assertThrows(IllegalArgumentException::class.java) {
            CardMatchingGame(13, 2)
        }
        assertThrows(IllegalArgumentException::class.java) {
            CardMatchingGame(25, 3)
        }
        assertThrows(IllegalArgumentException::class.java) {
            CardMatchingGame(12, 4)
        }
    }

    // ==================== 测试辅助 ====================

    private fun findTriple(game: CardMatchingGame): Triple<Int, Int, Int> {
        val indexesByRank = mutableMapOf<String, MutableList<Int>>()
        for (index in 0 until game.cardCount) {
            if (game.cardAtIndex(index).isMatched) continue
            val rank = game.cardAtIndex(index).rank
            indexesByRank.getOrPut(rank) { mutableListOf() }.add(index)
        }
        val group = indexesByRank.values.firstOrNull { it.size >= 3 }
            ?: throw AssertionError("没有找到三张点数相同的牌")
        return Triple(group[0], group[1], group[2])
    }

    private fun findThreeDifferentRanks(game: CardMatchingGame): Triple<Int, Int, Int> {
        val firstByRank = mutableMapOf<String, Int>()
        for (index in 0 until game.cardCount) {
            firstByRank.putIfAbsent(game.cardAtIndex(index).rank, index)
        }
        val indexes = firstByRank.values.take(3)
        check(indexes.size == 3)
        return Triple(indexes[0], indexes[1], indexes[2])
    }

    private fun findPair(game: CardMatchingGame): Pair<Int, Int> {
        val indexesByRank = mutableMapOf<String, MutableList<Int>>()
        for (index in 0 until game.cardCount) {
            if (game.cardAtIndex(index).isMatched) continue
            val rank = game.cardAtIndex(index).rank
            indexesByRank.getOrPut(rank) { mutableListOf() }.add(index)
        }
        val group = indexesByRank.values.firstOrNull { it.size >= 2 }
            ?: throw AssertionError("没有找到两张点数相同的牌")
        return group[0] to group[1]
    }

    private fun findDifferentPair(game: CardMatchingGame): Pair<Int, Int> {
        val firstByRank = mutableMapOf<String, Int>()
        for (index in 0 until game.cardCount) {
            firstByRank.putIfAbsent(game.cardAtIndex(index).rank, index)
        }
        val indexes = firstByRank.values.take(2)
        check(indexes.size == 2)
        return indexes[0] to indexes[1]
    }
}
