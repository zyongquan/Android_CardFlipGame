package cn.edu.sicnu.cardgame

/**
 * 一张扑克牌（Model 层）。
 *
 * 实验五把玩法从「两张配对」改成「三张配对」，所以 match() 改为接收另外两张牌：
 * 只有三张牌的点数完全相同时才返回 [CARDS_PER_MATCH]（即每张牌记 1 分），
 * 其余情况返回 0。花色不参与配对，这样每一局发出的同点数三张牌一定能被清空。
 *
 * 花色 / 点数保持 val：模型不需要再修改它们，而 View 层需要读取它们来显示牌面与配色。
 */
class Card(
    val suit: String,
    val rank: String,
    var isChosen: Boolean = false,
    var isMatched: Boolean = false
) {
    companion object {
        /** 一局配对所需的牌数：实验五为 3 张 */
        const val CARDS_PER_MATCH = 3

        val rankStrings = arrayOf(
            "A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K"
        )
        val validSuits = arrayOf("♥", "♦", "♠", "♣")
    }

    /** 牌面文字，例如 "♥A"，用于打印调试与无障碍描述 */
    override fun toString(): String {
        return suit + rank
    }

    /** 红色花色（红桃、方块），界面据此决定牌面文字的颜色 */
    val isRed: Boolean
        get() = suit == "♥" || suit == "♦"

    /**
     * 与另外两张牌比较：三张牌点数都相同才算配对成功。
     *
     * 返回值是“每张牌的基础分”，控制器再乘以 [CardMatchingGame.MATCH_BONUS] 加到总分：
     *  - 三张点数相同：返回 3（3 × 4 = 12 分）；
     *  - 其它情况：返回 0。
     *
     * 实验要求只修改本方法与 CardMatchingGame.chooseCardAtIndex() 即可完成 3 张牌玩法。
     */
    fun match(otherCards: Array<Card>): Int {
        if (otherCards.size != CARDS_PER_MATCH - 1) {
            return 0
        }
        val allSameRank = otherCards.all { it.rank == rank }
        return if (allSameRank) CARDS_PER_MATCH else 0
    }
}
