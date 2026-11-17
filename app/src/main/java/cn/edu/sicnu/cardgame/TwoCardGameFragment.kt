package cn.edu.sicnu.cardgame

/** 两张牌翻牌游戏 Fragment：12 张牌、4 列，一对同点数牌即可配对。 */
class TwoCardGameFragment : BaseCardGameFragment() {
    override val cardsPerMatch: Int = 2
    override val cardCount: Int = 12
    override val columns: Int = 4
    override val dataFileName: String = "two_card_game.dat"
    override val titleRes: Int = R.string.game_title_two
}
