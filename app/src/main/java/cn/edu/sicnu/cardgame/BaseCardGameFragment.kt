package cn.edu.sicnu.cardgame

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import cn.edu.sicnu.cardgame.databinding.FragmentCardGameBinding
import kotlin.math.ceil

/**
 * 翻牌游戏 Fragment 的公共基类。
 *
 * 两个子类只负责给出「几张牌配对、牌数、列数、存档文件名」，
 * 其余的逻辑（RecyclerView、点击回调、计分、自动保存）都在这里复用。
 *
 * 游戏状态通过 GameStore 序列化保存：
 *  - 每次翻牌、重新开始和 onStop() 都写入文件；
 *  - Fragment 创建时读回 CardMatchingGame，恢复牌面、得分和选择状态。
 */
abstract class BaseCardGameFragment : Fragment(), CardAdapter.OnCardClickListener {

    /** 一局配对需要几张牌：2 或 3 */
    protected abstract val cardsPerMatch: Int

    /** 牌桌总牌数 */
    protected abstract val cardCount: Int

    /** GridLayoutManager 的列数 */
    protected abstract val columns: Int

    /** 存档文件名 */
    protected abstract val dataFileName: String

    /** 标题字符串资源，例如「2 张牌翻牌游戏」 */
    protected abstract val titleRes: Int

    private var _binding: FragmentCardGameBinding? = null
    protected val binding get() = _binding!!

    protected lateinit var game: CardMatchingGame
        private set

    private lateinit var adapter: CardAdapter
    private var restored = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCardGameBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val savedGame = GameStore.load(requireContext(), dataFileName)
        restored = savedGame != null &&
            savedGame.cardsPerMatch == cardsPerMatch &&
            savedGame.cardCount == cardCount

        game = if (restored) {
            savedGame!!
        } else {
            CardMatchingGame(cardCount, cardsPerMatch)
        }

        binding.tvGameTitle.text = getString(titleRes)
        binding.tvStatus.text = getString(
            if (restored) R.string.game_restored else R.string.game_autosave
        )

        adapter = CardAdapter(game, this)
        binding.rvCards.layoutManager = GridLayoutManager(requireContext(), columns)
        binding.rvCards.adapter = adapter
        binding.rvCards.itemAnimator = null
        binding.rvCards.setHasFixedSize(true)

        binding.btnResetGame.setOnClickListener { resetGame() }
        binding.rvCards.addOnLayoutChangeListener { _, left, top, right, bottom,
            oldLeft, oldTop, oldRight, oldBottom ->
            val sizeChanged = (right - left) != (oldRight - oldLeft) ||
                (bottom - top) != (oldBottom - oldTop)
            if (sizeChanged) {
                binding.rvCards.post { updateCardSize() }
            }
        }
        binding.rvCards.post { updateCardSize() }

        updateHud()
    }

    override fun onResume() {
        super.onResume()
        (activity as? AppCompatActivity)?.supportActionBar?.setSubtitle(titleRes)
    }

    override fun onStop() {
        saveGame()
        super.onStop()
    }

    /** 根据牌数、列数和 RecyclerView 可用高度计算牌高，保证一屏完整显示 */
    private fun updateCardSize() {
        val recyclerView = binding.rvCards
        val rows = ceil(cardCount.toDouble() / columns).toInt()
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
        saveGame()

        val matchedDelta = game.matchedCount - matchedBefore
        when {
            matchedDelta > 0 -> {
                // 两张玩法同点数得 16 分，三张玩法同点数得 12 分
                val bonus = if (cardsPerMatch == 2) {
                    4 * game.MATCH_BONUS
                } else {
                    cardsPerMatch * game.MATCH_BONUS
                }
                Toast.makeText(
                    requireContext(),
                    getString(R.string.game_matched, bonus),
                    Toast.LENGTH_SHORT
                ).show()
            }

            selectedBefore == cardsPerMatch - 1 && game.selectedCount == 0 -> {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.game_mismatch, -game.MISMATCH_PENALTY),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        if (game.isGameOver) {
            Toast.makeText(
                requireContext(),
                getString(R.string.game_win, game.score),
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun resetGame() {
        game.reset()
        adapter.refresh()
        updateHud()
        saveGame()
        Toast.makeText(requireContext(), R.string.game_restart, Toast.LENGTH_SHORT).show()
    }

    private fun updateHud() {
        binding.tvScore.text = getString(R.string.game_score_format, game.score)
        binding.tvProgress.text =
            getString(R.string.game_progress_format, game.matchedCount, game.cardCount)
        binding.tvSelection.text = when {
            game.isGameOver -> getString(R.string.game_selection_finished)
            game.selectedCount == 0 -> getString(R.string.game_selection_hint_format, cardsPerMatch)
            else -> getString(
                R.string.game_selection_format,
                game.selectedCount,
                cardsPerMatch
            )
        }
    }

    private fun saveGame() {
        if (::game.isInitialized) {
            GameStore.save(requireContext(), dataFileName, game)
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
