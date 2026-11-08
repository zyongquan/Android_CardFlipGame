package cn.edu.sicnu.cardgame

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import cn.edu.sicnu.cardgame.databinding.ActivityCardGameBinding

/**
 * 翻牌游戏界面（Controller + View）。
 *
 * Controller 的职责：
 *  1. 读取当前屏幕方向对应的牌数 / 列数（values 与 values-land 里的整数资源）；
 *  2. 接收适配器回调的点击，调用 CardMatchingGame.chooseCardAtIndex() 改变模型；
 *  3. 操作完成后刷新界面（得分、进度、本轮选择数、牌面），并提示本次判定结果；
 *  4. 「重新开始」调用模型新增的 reset() 方法；
 *  5. 旋转屏幕时 Activity 重建，自动按横屏 / 竖屏读取不同的牌数（24 / 30）。
 */
class CardGameActivity : AppCompatActivity(), CardAdapter.OnCardClickListener {

    companion object {
        const val EXTRA_HIGH_SCORE = "extra_high_score"
    }

    private lateinit var binding: ActivityCardGameBinding
    private lateinit var adapter: CardAdapter
    private lateinit var game: CardMatchingGame
    private var bestScore = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCardGameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bestScore = intent.getIntExtra(EXTRA_HIGH_SCORE, 0)

        // 竖屏 values/integers.xml：24 张、4 列；横屏 values-land/integers.xml：30 张、6 列
        val cardCount = resources.getInteger(R.integer.card_count)
        val columns = resources.getInteger(R.integer.card_columns)
        game = CardMatchingGame(cardCount)
        adapter = CardAdapter(game, this)

        binding.rvCards.layoutManager = GridLayoutManager(this, columns)
        binding.rvCards.adapter = adapter
        // 关闭默认的列表动画，翻牌动画由适配器自己控制
        binding.rvCards.itemAnimator = null
        binding.rvCards.setHasFixedSize(true)

        binding.btnResetGame.setOnClickListener { resetGame() }
        binding.btnExit.setOnClickListener { finishWithResult() }
        // 直接按返回键也要把最高分带回去，所以拦截返回事件
        onBackPressedDispatcher.addCallback(this) { finishWithResult() }

        // 牌数由屏幕方向决定：旋转后 Activity 重建，这里会重新读取 values-land 中的 30
        binding.rvCards.addOnLayoutChangeListener { _, left, top, right, bottom,
            oldLeft, oldTop, oldRight, oldBottom ->
            val sizeChanged = (right - left) != (oldRight - oldLeft) ||
                (bottom - top) != (oldBottom - oldTop)
            if (sizeChanged) {
                binding.rvCards.post { updateCardSize() }
            }
        }
        binding.rvCards.post { updateCardSize() }

        supportActionBar?.setSubtitle(getString(R.string.subtitle_card_game_format, cardCount))
        updateHud()

        if (savedInstanceState != null) {
            Toast.makeText(
                this,
                getString(R.string.game_orientation_changed, cardCount),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    /**
     * 根据当前牌数、列数和 RecyclerView 的可用高度计算每张牌的高度，
     * 让 24 张（竖屏）或 30 张（横屏）牌都能在一屏内完整显示。
     */
    private fun updateCardSize() {
        val recyclerView = binding.rvCards
        val columns = resources.getInteger(R.integer.card_columns)
        val rows = (game.cardCount + columns - 1) / columns
        val availableHeight =
            recyclerView.height - recyclerView.paddingTop - recyclerView.paddingBottom
        if (availableHeight <= 0 || rows <= 0) {
            return
        }
        adapter.itemHeight = availableHeight / rows
    }

    override fun onCardClick(position: Int) {
        val selectedBefore = game.selectedCount
        val matchedBefore = game.matchedCount

        game.chooseCardAtIndex(position)
        adapter.refresh()
        updateHud()

        when {
            // 第三次翻牌后三张牌配对成功
            game.matchedCount > matchedBefore -> {
                val bonus = (game.matchedCount - matchedBefore) * game.MATCH_BONUS
                Toast.makeText(
                    this,
                    getString(R.string.game_matched, bonus),
                    Toast.LENGTH_SHORT
                ).show()
            }

            // 第三次翻牌后三张牌点数不同：全部盖回并扣分
            selectedBefore == Card.CARDS_PER_MATCH - 1 && game.selectedCount == 0 -> {
                Toast.makeText(
                    this,
                    getString(R.string.game_mismatch, -game.MISMATCH_PENALTY),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        if (game.isGameOver) {
            Toast.makeText(
                this, getString(R.string.game_win, game.score), Toast.LENGTH_LONG
            ).show()
        }
    }

    /** 重新开始一局：调用模型新增的 reset() 方法，得分清零、重新发牌 */
    private fun resetGame() {
        game.reset()
        adapter.refresh()
        updateHud()
        Toast.makeText(this, R.string.game_restart, Toast.LENGTH_SHORT).show()
    }

    /** Model → View：刷新得分、配对进度和本轮选择数 */
    private fun updateHud() {
        binding.tvScore.text = getString(R.string.game_score_format, game.score)
        binding.tvProgress.text =
            getString(R.string.game_progress_format, game.matchedCount, game.cardCount)
        binding.tvSelection.text = when {
            game.isGameOver -> getString(R.string.game_selection_finished)
            game.selectedCount == 0 -> getString(R.string.game_selection_hint)
            else -> getString(
                R.string.game_selection_format,
                game.selectedCount,
                Card.CARDS_PER_MATCH
            )
        }
    }

    /** 结束本局：把最高分回传给主界面 */
    private fun finishWithResult() {
        val best = maxOf(bestScore, game.score)
        setResult(RESULT_OK, Intent().putExtra(EXTRA_HIGH_SCORE, best))
        finish()
    }
}
