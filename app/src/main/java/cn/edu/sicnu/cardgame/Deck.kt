package cn.edu.sicnu.cardgame

import java.util.Random

/**
 * 一副 52 张的扑克牌（Model 层）。
 *
 * 在实验附件的基础上增加了两个发牌辅助方法：
 *  - drawCardWithRank()：抽出指定点数的一张牌；
 *  - drawCardsWithSameRank()：一次抽出若干张点数相同的牌。
 *
 * 实验五的玩法是「三张同点数配对」，发牌时按三个一组发放，
 * 保证每一局的牌都能被全部清空，不会出现无法配对的“落单牌”。
 */
class Deck {
    private val cards = mutableListOf<Card>()
    private val r = Random()

    init {
        for (suit in Card.validSuits) {
            for (rank in Card.rankStrings) {
                val card = Card(suit = suit, rank = rank)
                cards.add(card)
            }
        }
    }

    /** 剩余牌数 */
    val size: Int
        get() = cards.size

    /** 随机抽一张牌，抽完即从牌堆中移除；牌堆为空时返回 null */
    fun drawRandomCard(): Card? {
        var randomCard: Card? = null
        if (cards.size > 0) {
            randomCard = cards.removeAt(r.nextInt(cards.size))
        }
        return randomCard
    }

    /** 抽出指定点数的一张牌，没有该点数时返回 null */
    fun drawCardWithRank(rank: String): Card? {
        val index = cards.indexOfFirst { it.rank == rank }
        return if (index >= 0) cards.removeAt(index) else null
    }

    /**
     * 连续抽出 [count] 张点数相同的牌，用于实验五的「三张配对」发牌。
     *
     * 只从剩余张数不少于 [count] 的点数中随机选择，因此抽出的牌一定可以配成一组；
     * 剩余牌不够时返回空列表。
     */
    fun drawCardsWithSameRank(count: Int): List<Card> {
        val candidateRanks = cards
            .groupingBy { it.rank }
            .eachCount()
            .filterValues { it >= count }
            .keys
        val rank = candidateRanks.randomOrNull() ?: return emptyList()

        val result = mutableListOf<Card>()
        repeat(count) {
            val card = drawCardWithRank(rank) ?: return emptyList()
            result.add(card)
        }
        return result
    }
}
