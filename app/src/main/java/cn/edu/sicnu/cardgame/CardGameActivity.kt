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
 *  1. 接收用户点击，调用模型 CardMatchingGame.chooseCardAtIndex() 改变模型状态；
 *  2. 操作完成后刷新界面（得分、进度、牌面），并根据本次操作弹出一条提示；
 *  3. 「重置游戏」重新 new 一个 CardMatchingGame，开始新的一局；
 *  4. 「退出」/ 返回键结束时，把本局最高分回传给主界面。
 */
class CardGameActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_HIGH_SCORE = "extra_high_score"

        /** 牌桌上的牌数：3 列 × 4 行 */
        const val CARD_COUNT = 12
        const val GRID_SPAN = 3
    }

    private lateinit var binding: ActivityCardGameBinding
    private lateinit var adapter: CardAdapter
    private var game: CardMatchingGame? = null
    private var bestScore = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCardGameBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setSubtitle(R.string.subtitle_card_game)

        bestScore = intent.getIntExtra(EXTRA_HIGH_SCORE, 0)

        binding.btnResetGame.setOnClickListener { startNewGame(showToast = true) }
        binding.btnExit.setOnClickListener { finishWithResult() }
        // 直接按返回键也要把最高分带回去，所以拦截返回事件
        onBackPressedDispatcher.addCallback(this) { finishWithResult() }

        startNewGame(showToast = false)
    }

    /** 开始新的一局：创建新的模型，重建 / 重置列表 */
    private fun startNewGame(showToast: Boolean) {
        val newGame = CardMatchingGame(CARD_COUNT)
        val isFirstGame = game == null
        game = newGame

        if (isFirstGame) {
            adapter = CardAdapter(newGame) { index -> onCardChosen(index) }
            binding.rvCards.layoutManager = GridLayoutManager(this, GRID_SPAN)
            binding.rvCards.adapter = adapter
            // 关闭默认的列表动画，翻牌动画由适配器自己控制
            binding.rvCards.itemAnimator = null
        } else {
            adapter.submitGame(newGame)
        }

        updateHud()
        if (showToast) {
            Toast.makeText(this, R.string.game_restart, Toast.LENGTH_SHORT).show()
        }
    }

    /** 用户点击了第 index 张牌：先改模型，再同步界面 */
    private fun onCardChosen(index: Int) {
        val game = game ?: return
        val scoreBefore = game.score
        val matchedBefore = game.matchedCount

        game.chooseCardAtIndex(index)
        adapter.refresh()
        updateHud()

        val delta = game.score - scoreBefore
        when {
            game.matchedCount > matchedBefore -> {
                Toast.makeText(this, getString(R.string.game_matched, delta), Toast.LENGTH_SHORT)
                    .show()
            }

            delta == -(game.MISMATCH_PENALTY + game.COST_TO_CHOOSE) -> {
                Toast.makeText(this, getString(R.string.game_mismatch, delta), Toast.LENGTH_SHORT)
                    .show()
            }
        }

        if (game.isGameOver) {
            Toast.makeText(this, getString(R.string.game_win, game.score), Toast.LENGTH_LONG).show()
        }
    }

    /** Model → View：刷新得分和配对进度 */
    private fun updateHud() {
        val game = game ?: return
        binding.tvScore.text = getString(R.string.game_score_format, game.score)
        binding.tvProgress.text =
            getString(R.string.game_progress_format, game.matchedCount, game.cardCount)
    }

    /** 结束本局：把最高分回传给主界面 */
    private fun finishWithResult() {
        val score = game?.score ?: 0
        val best = maxOf(bestScore, score)
        setResult(RESULT_OK, Intent().putExtra(EXTRA_HIGH_SCORE, best))
        finish()
    }
}
