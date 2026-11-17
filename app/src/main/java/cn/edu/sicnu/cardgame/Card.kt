package cn.edu.sicnu.cardgame

import java.io.Serializable

/**
 * 一张扑克牌（Model 层）。
 *
 * 实验六同时支持「两张配对」和「三张配对」两种玩法，所以 match() 允许传入 1 张或 2 张
 * 其它牌：点数全部相同才算配对，返回该组牌的基础分（两张为 4，三张为 3）。
 * 花色不参与配对，保证一局发出的同点数牌都能被清空。
 *
 * Card 实现 Serializable，随 CardMatchingGame 一起写入文件，退出程序后可以恢复牌局。
 */
class Card(
    val suit: String,
    val rank: String,
    var isChosen: Boolean = false,
    var isMatched: Boolean = false
) : Serializable {

    companion object {
        private const val serialVersionUID = 1L

        /** 实验五留下的常量，表示三张配对；实验六的模型改用 cardsPerMatch 实例字段 */
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
     * 与其它牌比较：这些牌的点数都与本牌相同才算配对成功。
     *
     * @param otherCards 另外 1 张（两张玩法）或 2 张（三张玩法）牌
     * @return 两张玩法返回 4，三张玩法返回 3；点数不同返回 0
     */
    fun match(otherCards: Array<Card>): Int {
        if (otherCards.isEmpty() || otherCards.size > 2) {
            return 0
        }
        if (!otherCards.all { it.rank == rank }) {
            return 0
        }
        return when (otherCards.size) {
            1 -> 4
            2 -> 3
            else -> 0
        }
    }
}
