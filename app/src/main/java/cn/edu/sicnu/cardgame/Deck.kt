package cn.edu.sicnu.cardgame

import java.util.Random

/**
 * 一副 52 张的扑克牌（Model 层）。
 *
 * 在实验附件的基础上增加了一个 drawCardWithRank()：
 * 发牌时先随机抽一张、再抽一张同点数的牌，保证每一局发到的牌都能两两配对清空，
 * 否则随机发 12 张很可能存在无法配对的“落单牌”，游戏永远无法结束。
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

    /** 抽出指定点数的一张牌（用于成对发牌），没有该点数时返回 null */
    fun drawCardWithRank(rank: String): Card? {
        val index = cards.indexOfFirst { it.rank == rank }
        return if (index >= 0) cards.removeAt(index) else null
    }
}
