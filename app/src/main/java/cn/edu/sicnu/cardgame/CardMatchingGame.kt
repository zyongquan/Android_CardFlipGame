package cn.edu.sicnu.cardgame

import java.io.Serializable

/**
 * 翻牌游戏模型（Model 层）。
 *
 * 实验六在实验五的三张配对基础上做了通用化：构造时通过 cardsPerMatch 指定一局配对
 * 需要几张牌，2 表示两张玩法，3 表示三张玩法。两种玩法共用同一套计分和状态管理。
 *
 * 规则：
 *  - 每翻开一张牌扣 COST_TO_CHOOSE（1 分）；
 *  - 本轮翻开 cardsPerMatch 张牌后判定：点数全部相同加分并标记已配对；否则扣
 *    MISMATCH_PENALTY（2 分）并把本轮所有牌盖回；
 *  - 点击本轮已经翻开的牌可以撤销选择，不额外扣分。
 *
 * CardMatchingGame 实现 Serializable，Fragment 在 onStop() 中把整个对象写入文件，
 * 再次打开应用时可以恢复牌面、得分和选择状态。
 */
class CardMatchingGame(
    count: Int,
    val cardsPerMatch: Int = Card.CARDS_PER_MATCH
) : Serializable {

    companion object {
        private const val serialVersionUID = 1L

        /** 单局最多牌数（三张玩法）：13 种点数 × 每种 3 张 = 39 张 */
        const val MAX_CARD_COUNT = 39
    }

    var score = 0
        private set

    /** 当前牌数 */
    var cardCount = count
        private set

    private val cards = mutableListOf<Card>()

    /** 本轮已经翻开、还没有完成一次判定的牌，按翻转顺序保存 */
    private val chosenCards = mutableListOf<Card>()

    init {
        requireValidCount(count)
        deal(count)
    }

    private fun requireValidCount(count: Int) {
        require(cardsPerMatch in 2..3) { "cardsPerMatch must be 2 or 3" }
        val maxCount = Card.rankStrings.size * cardsPerMatch
        require(count in cardsPerMatch..maxCount) {
            "card count must be between $cardsPerMatch and $maxCount"
        }
        require(count % cardsPerMatch == 0) {
            "card count must be a multiple of $cardsPerMatch"
        }
    }

    /** 发牌：每次从牌堆里抽出一组点数相同的牌 */
    private fun deal(count: Int) {
        cards.clear()
        val deck = Deck()
        repeat(count / cardsPerMatch) {
            val group = deck.drawCardsWithSameRank(cardsPerMatch)
            check(group.isNotEmpty()) { "deck does not have enough cards" }
            cards.addAll(group)
        }
    }

    fun cardAtIndex(index: Int): Card {
        return cards[index]
    }

    /** 已经配对成功的牌数 */
    val matchedCount: Int
        get() = cards.count { it.isMatched }

    /** 本轮已经翻开的牌数（0 ~ cardsPerMatch） */
    val selectedCount: Int
        get() = chosenCards.size

    /** 全部牌都配对完成，本局结束 */
    val isGameOver: Boolean
        get() = cards.isNotEmpty() && cards.all { it.isMatched }

    val MISMATCH_PENALTY = 2
    val MATCH_BONUS = 4
    val COST_TO_CHOOSE = 1

    /**
     * 重新开始一局：清零得分、清空选择并重新发牌。
     *
     * @param count 新一局的牌数，默认沿用当前牌数
     */
    fun reset(count: Int = cardCount) {
        requireValidCount(count)
        cardCount = count
        score = 0
        chosenCards.clear()
        deal(count)
    }

    /**
     * 翻开 / 选中第 [index] 张牌。
     *
     * 已配对的牌直接忽略；再次点击本轮已翻开的牌则撤销选择；否则扣 1 分并加入
     * 本轮选择。累计到 cardsPerMatch 张时调用 judgeChosenCards() 判定。
     */
    fun chooseCardAtIndex(index: Int) {
        val card = cardAtIndex(index)
        if (card.isMatched) {
            return
        }

        if (card.isChosen) {
            card.isChosen = false
            chosenCards.remove(card)
            return
        }

        card.isChosen = true
        chosenCards.add(card)
        score -= COST_TO_CHOOSE

        if (chosenCards.size == cardsPerMatch) {
            judgeChosenCards()
        }
    }

    /** 对本轮翻开的牌做一次配对判定 */
    private fun judgeChosenCards() {
        val picked = chosenCards.toList()
        val matchScore = picked[0].match(picked.drop(1).toTypedArray())

        if (matchScore > 0) {
            score += matchScore * MATCH_BONUS
            picked.forEach { it.isMatched = true }
        } else {
            score -= MISMATCH_PENALTY
            picked.forEach { it.isChosen = false }
        }

        chosenCards.clear()
    }
}
