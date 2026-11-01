package cn.edu.sicnu.cardgame

/**
 * 翻牌游戏模型（Model 层）。
 *
 * 规则与实验附件完全一致：
 *  - 每翻开一张牌扣 COST_TO_CHOOSE 分；
 *  - 连续翻开两张牌，若点数相同得 MATCH_BONUS × 4 分并进入 isMatched 状态；
 *  - 点数不同则扣 MISMATCH_PENALTY 分，并把先前那张牌重新盖上。
 *
 * 这里只补充了四处便于界面使用的成员：
 *  - 发牌时成对抽取（保证一局可以清空）；
 *  - cardCount / matchedCount / isGameOver 供控制器刷新“已配对 n / m”提示；
 *  - chosenCount 供控制器判断本次操作是“翻第一张”还是“翻第二张”。
 */
class CardMatchingGame(count: Int) {
    var score = 0
        private set

    private val cards: MutableList<Card>

    init {
        val deck = Deck()
        cards = mutableListOf()
        // 成对发牌：先随机抽一张，再补一张同点数的牌，凑成一对
        var i = 0
        while (i < count) {
            val card: Card = deck.drawRandomCard() ?: break
            cards.add(card)
            i++
            if (i < count) {
                val mate = deck.drawCardWithRank(card.rank) ?: continue
                cards.add(mate)
                i++
            }
        }
    }

    fun cardAtIndex(index: Int): Card {
        return cards.get(index)
    }

    /** 牌的总数 */
    val cardCount: Int
        get() = cards.size

    /** 已经配对成功的牌数 */
    val matchedCount: Int
        get() = cards.count { it.isMatched }

    /** 全部牌都配对完成，本局结束 */
    val isGameOver: Boolean
        get() = cards.isNotEmpty() && cards.all { it.isMatched }

    val MISMATCH_PENALTY = 2
    val MATCH_BONUS = 4
    val COST_TO_CHOOSE = 1

    fun chooseCardAtIndex(index: Int) {
        val card = cardAtIndex(index)
        if (!card.isMatched) {
            if (card.isChosen) {
                card.isChosen = false
            } else {
                for (otherCard in cards) {
                    if (otherCard.isChosen && !otherCard.isMatched) {
                        val matchScore = card.match(arrayOf(otherCard))
                        if (matchScore > 0) {
                            score += matchScore * MATCH_BONUS
                            otherCard.isMatched = true
                            card.isMatched = true
                        } else {
                            score -= MISMATCH_PENALTY
                            otherCard.isChosen = false
                        }
                        break
                    }
                }
                score -= COST_TO_CHOOSE
                card.isChosen = true
            }
        }
    }
}
