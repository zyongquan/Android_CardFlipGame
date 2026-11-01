package cn.edu.sicnu.cardgame

/**
 * 一张扑克牌（Model 层）。
 *
 * 与实验附件中的代码保持一致，只把花色 / 点数从 private var 改成 val：
 * 模型本身不需要再修改它们，而 View 层（牌面）需要读取花色和点数来显示与配色。
 */
class Card(
    val suit: String,
    val rank: String,
    var isChosen: Boolean = false,
    var isMatched: Boolean = false
) {
    companion object {
        val rankStrings = arrayOf("A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K")
        val validSuits = arrayOf("♥", "♦", "♠", "♣")
    }

    /** 牌面文字，例如 "♥A"，用于打印调试 */
    override fun toString(): String {
        return suit + rank
    }

    /** 红色花色（红桃、方块），界面据此决定牌面文字的颜色 */
    val isRed: Boolean
        get() = suit == "♥" || suit == "♦"

    /**
     * 与另一张牌比较：点数相同得 4 分，花色相同得 1 分，都不同得 0 分。
     * 计分规则来自实验附件，CardMatchingGame 会把它乘上 MATCH_BONUS 再加到总分里。
     */
    fun match(otherCards: Array<Card>): Int {
        var score = 0
        if (otherCards.size == 1) {
            val otherCard = otherCards[0]
            if (otherCard.rank == rank) {
                score = 4
            } else if (otherCard.suit == suit) {
                score = 1
            }
        }
        return score
    }
}
