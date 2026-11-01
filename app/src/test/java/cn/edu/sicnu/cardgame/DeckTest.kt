package cn.edu.sicnu.cardgame

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 模型层 Deck 的单元测试。
 * 验证一副牌是 52 张、每次抽到的牌都不同，并且可以按点数抽牌（成对发牌用）。
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
    fun allFourSuitsAreInDeck() {
        val deck = Deck()
        val suits = mutableSetOf<String>()
        repeat(52) { deck.drawRandomCard()?.let { suits.add(it.suit) } }

        assertTrue(suits.containsAll(Card.validSuits.toList()))
    }
}
