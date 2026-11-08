package cn.edu.sicnu.cardgame

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 模型层 Deck 的单元测试。
 * 验证一副牌是 52 张、每次抽到的牌都不同，并且可以按点数成组抽牌（三张配对发牌用）。
 */
class DeckTest {

    @Test
    fun newDeck_has52Cards() {
        assertEquals(52, Deck().size)
    }

    @Test
    fun draw52Cards_allCardsAreUnique() {
        val deck = Deck()
        val drawn = mutableSetOf<String>()
        repeat(52) {
            val card = deck.drawRandomCard()
            assertNotNull(card)
            drawn.add(card!!.toString())
        }

        assertEquals(52, drawn.size)
        assertEquals(0, deck.size)
    }

    @Test
    fun drawFromEmptyDeck_returnsNull() {
        val deck = Deck()
        repeat(52) { deck.drawRandomCard() }

        assertNull(deck.drawRandomCard())
    }

    @Test
    fun drawCardWithRank_returnsRequestedRankAndDecreasesSize() {
        val deck = Deck()

        val card = deck.drawCardWithRank("K")

        assertNotNull(card)
        assertEquals("K", card!!.rank)
        assertEquals(51, deck.size)
    }

    @Test
    fun drawCardWithRank_returnsNullWhenRankNotExists() {
        assertNull(Deck().drawCardWithRank("不是扑克牌的点数"))
    }

    @Test
    fun drawCardsWithSameRank_returnsThreeCardsWithSameRank() {
        val deck = Deck()

        val cards = deck.drawCardsWithSameRank(Card.CARDS_PER_MATCH)

        assertEquals(Card.CARDS_PER_MATCH, cards.size)
        assertEquals(1, cards.map { it.rank }.distinct().size)
        assertEquals(52 - Card.CARDS_PER_MATCH, deck.size)
    }

    @Test
    fun drawCardsWithSameRank_returnsEmptyWhenCountTooLarge() {
        val deck = Deck()

        // 每种点数最多 4 张，一次抽 5 张同点数的牌一定抽不出来
        val cards = deck.drawCardsWithSameRank(5)

        assertTrue(cards.isEmpty())
        assertEquals(52, deck.size)
    }

    @Test
    fun drawEightTriples_covers24CardsGroupedByRank() {
        val deck = Deck()
        val drawn = mutableListOf<Card>()

        repeat(8) {
            val triple = deck.drawCardsWithSameRank(Card.CARDS_PER_MATCH)
            assertEquals(Card.CARDS_PER_MATCH, triple.size)
            drawn.addAll(triple)
        }

        assertEquals(24, drawn.size)
        drawn.groupingBy { it.rank }.eachCount().values.forEach { count ->
            assertEquals(0, count % Card.CARDS_PER_MATCH)
        }
    }

    @Test
    fun allFourSuitsAreInDeck() {
        val deck = Deck()
        val suits = mutableSetOf<String>()
        repeat(52) { deck.drawRandomCard()?.let { suits.add(it.suit) } }

        assertTrue(suits.containsAll(Card.validSuits.toList()))
    }
}
