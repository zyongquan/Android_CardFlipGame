package cn.edu.sicnu.cardgame

/** 三张牌翻牌游戏 Fragment：24 张牌、4 列，三张同点数牌配对。 */
class ThreeCardGameFragment : BaseCardGameFragment() {
    override val cardsPerMatch: Int = 3
    override val cardCount: Int = 24
    override val columns: Int = 4
    override val dataFileName: String = "three_card_game.dat"
    override val titleRes: Int = R.string.game_title_three
}
