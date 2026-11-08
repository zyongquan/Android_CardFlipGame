package cn.edu.sicnu.cardgame

/**
 * 翻牌游戏模型（Model 层）。
 *
 * 实验五在实验四的基础上把「两张配对」改成「三张配对」，规则如下：
 *  - 每翻开一张牌扣 COST_TO_CHOOSE（1 分）；
 *  - 本轮累计翻开 3 张牌后判定：三张点数相同得 3 × MATCH_BONUS（12 分），
 *    三张牌进入 isMatched 状态；
 *  - 三张点数不同则扣 MISMATCH_PENALTY（2 分），三张牌全部重新盖上；
 *  - 点击本轮已经翻开的牌可以撤销选择，不额外扣分。
 *
 * 为界面补充的成员：
 *  - reset()：重新发牌并清零得分（实验要求）；
 *  - cardCount / matchedCount / isGameOver：控制器刷新“已配对 n / m”与胜利提示；
 *  - selectedCount：控制器显示“本轮已选 n / 3 张”。
 *
 * 发牌按“三张同点数”为一组抽取，保证一局一定能被清空。
 * 横屏、竖屏通过构造参数传入不同的牌数（24 / 30）。
 */
class CardMatchingGame(count: Int) {
    companion object {
        /** 单局最多牌数：13 种点数 × 每种 3 张 = 39 张 */
        const val MAX_CARD_COUNT = 39
    }

    var score = 0
        private set

    /** 当前牌数；reset(count) 可以在换屏幕方向导致牌数变化时重新指定 */
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
        require(count in Card.CARDS_PER_MATCH..MAX_CARD_COUNT) {
            "card count must be between ${Card.CARDS_PER_MATCH} and $MAX_CARD_COUNT"
        }
        require(count % Card.CARDS_PER_MATCH == 0) {
            "card count must be a multiple of ${Card.CARDS_PER_MATCH}"
        }
    }

    /** 发牌：每次从牌堆里抽出一组点数相同的三张牌 */
    private fun deal(count: Int) {
        cards.clear()
        val deck = Deck()
        repeat(count / Card.CARDS_PER_MATCH) {
            val group = deck.drawCardsWithSameRank(Card.CARDS_PER_MATCH)
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

    /** 本轮已经翻开的牌数（0 ~ 3） */
    val selectedCount: Int
        get() = chosenCards.size

    /** 全部牌都配对完成，本局结束 */
    val isGameOver: Boolean
        get() = cards.isNotEmpty() && cards.all { it.isMatched }

    val MISMATCH_PENALTY = 2
    val MATCH_BONUS = 4
    val COST_TO_CHOOSE = 1

    /**
     * 重新开始一局（实验要求新增的 reset 方法）。
     *
     * @param count 新一局的牌数，默认沿用当前牌数；横竖屏切换时也可以传入新的牌数。
     */
    fun reset(count: Int = cardCount) {
        requireValidCount(count)
        cardCount = count
        score = 0
        chosenCards.clear()
        deal(count)
    }

    /**
     * 翻开 / 选中第 [index] 张牌（3 张配对的核心逻辑）。
     *
     * 选中第 3 张牌时调用 Card.match() 进行判定：
     *  - 配对成功：三张牌标记为 isMatched，并加 MATCH_BONUS × 3 分；
     *  - 配对失败：三张牌重新盖上，并扣 MISMATCH_PENALTY 分。
     */
    fun chooseCardAtIndex(index: Int) {
        val card = cardAtIndex(index)
        if (card.isMatched) {
            return
        }

        // 再次点击本轮已翻开的牌：撤销选择，不额外扣分
        if (card.isChosen) {
            card.isChosen = false
            chosenCards.remove(card)
            return
        }

        card.isChosen = true
        chosenCards.add(card)
        score -= COST_TO_CHOOSE

        if (chosenCards.size == Card.CARDS_PER_MATCH) {
            judgeChosenCards()
        }
    }

    /** 对本轮翻开的三张牌做一次配对判定 */
    private fun judgeChosenCards() {
        val picked = chosenCards.toList()
        val matchScore = picked[0].match(arrayOf(picked[1], picked[2]))

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
