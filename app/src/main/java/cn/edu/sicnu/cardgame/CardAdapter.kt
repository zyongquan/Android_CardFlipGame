package cn.edu.sicnu.cardgame

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import cn.edu.sicnu.cardgame.databinding.ItemCardBinding

/**
 * 牌桌适配器（View 层）。
 *
 * 继承自 RecyclerView.Adapter，重写了 getItemCount()、onCreateViewHolder()、
 * onBindViewHolder() 三个方法，把模型里每张牌的 isChosen / isMatched 状态
 * 同步成界面上的牌背 / 牌面 / 已配对样式，并在状态变化时播放翻牌动画。
 *
 * 用户点击某张牌时，通过 [OnCardClickListener] 回调把位置交给控制器（Activity），
 * 由控制器调用模型并刷新界面，实现 View 与 Model 的解耦。
 *
 * 牌的高度由控制器根据屏幕方向、牌数与可用空间计算后通过 [itemHeight] 设置，
 * 这样竖屏 24 张、横屏 30 张都能一屏完整显示，不需要滚动。
 */
class CardAdapter(
    private var game: CardMatchingGame,
    private val onCardClick: OnCardClickListener
) : RecyclerView.Adapter<CardAdapter.CardViewHolder>() {

    /** 点击某张牌的回调接口（实验要求：适配器中实现对回调的处理） */
    fun interface OnCardClickListener {
        fun onCardClick(position: Int)
    }

    /** 由控制器计算出的单张牌高度（px），0 表示使用布局里的默认高度 */
    var itemHeight: Int = 0
        set(value) {
            if (field == value) return
            field = value
            notifyItemRangeChanged(0, itemCount)
        }

    /** 记录每个位置“当前显示的是不是牌面”，用于判断是否需要播放翻牌动画 */
    private var faceShown = BooleanArray(game.cardCount)

    /** 重新开始一局：换掉模型并让所有牌回到牌背 */
    fun submitGame(newGame: CardMatchingGame) {
        game = newGame
        faceShown = BooleanArray(newGame.cardCount)
        notifyDataSetChanged()
    }

    /** 一次操作之后刷新所有牌的状态（牌数很少，直接整表刷新即可） */
    fun refresh() {
        if (faceShown.size != game.cardCount) {
            faceShown = BooleanArray(game.cardCount)
        }
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = game.cardCount

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val binding = ItemCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return CardViewHolder(binding, onCardClick)
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        val card = game.cardAtIndex(position)
        val faceUp = card.isChosen || card.isMatched
        val wasFaceUp = if (position < faceShown.size) faceShown[position] else false
        holder.bind(
            card, position, faceUp, animate = wasFaceUp != faceUp, itemHeight = itemHeight
        )
        if (position < faceShown.size) {
            faceShown[position] = faceUp
        }
    }

    /** 单张牌的 ViewHolder：绑定牌面数据、翻牌动画与点击回调 */
    class CardViewHolder(
        private val binding: ItemCardBinding,
        private val onCardClick: OnCardClickListener
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            // 绕 Y 轴旋转时要设置相机距离，否则透视会非常夸张
            binding.flipContainer.cameraDistance =
                8000f * binding.root.resources.displayMetrics.density
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onCardClick.onCardClick(position)
                }
            }
        }

        fun bind(card: Card, position: Int, faceUp: Boolean, animate: Boolean, itemHeight: Int) {
            val context = binding.root.context

            // 牌的高度由控制器按屏幕空间计算，保证 24 / 30 张牌都能完整显示
            if (itemHeight > 0 && itemView.layoutParams.height != itemHeight) {
                itemView.layoutParams.height = itemHeight
            }
            applyTextSize(itemHeight)

            val suitColor = ContextCompat.getColor(
                context,
                if (card.isRed) R.color.suit_red else R.color.suit_black
            )
            binding.tvRank.text = card.rank
            binding.tvSuit.text = card.suit
            binding.tvRank.setTextColor(suitColor)
            binding.tvSuit.setTextColor(suitColor)
            // 已经配对的牌换成浅绿色底，方便区分
            binding.cardFront.setBackgroundResource(
                if (card.isMatched) R.drawable.bg_card_matched else R.drawable.bg_card_face
            )
            binding.root.contentDescription = if (faceUp) {
                context.getString(R.string.cd_card_face, position + 1, card.toString())
            } else {
                context.getString(R.string.cd_card_back, position + 1)
            }
            flip(faceUp, animate)
        }

        /** 小牌桌上缩小牌面字号，避免文字超出牌面 */
        private fun applyTextSize(itemHeight: Int) {
            val height = if (itemHeight > 0) itemHeight else itemView.layoutParams.height
            val heightDp = height / binding.root.resources.displayMetrics.density
            binding.tvRank.textSize = (heightDp * 0.32f).coerceIn(12f, 28f)
            binding.tvSuit.textSize = (heightDp * 0.24f).coerceIn(10f, 22f)
        }

        /** 翻到牌面 / 翻回牌背；animate = false 时直接切换（用于列表初次绑定） */
        private fun flip(faceUp: Boolean, animate: Boolean) {
            binding.flipContainer.animate().cancel()
            if (!animate) {
                binding.flipContainer.rotationY = 0f
                applyFace(faceUp)
                return
            }
            binding.flipContainer.animate()
                .rotationY(90f)
                .setDuration(FLIP_HALF_DURATION)
                .withEndAction {
                    // 转到 90° 时正好看不见，此时切换正反面再转回来
                    applyFace(faceUp)
                    binding.flipContainer.rotationY = -90f
                    binding.flipContainer.animate()
                        .rotationY(0f)
                        .setDuration(FLIP_HALF_DURATION)
                        .start()
                }
                .start()
        }

        private fun applyFace(faceUp: Boolean) {
            binding.cardBack.isVisible = !faceUp
            binding.cardFront.isVisible = faceUp
        }

        private companion object {
            /** 半程动画时长（毫秒），来回一共约 220ms */
            const val FLIP_HALF_DURATION = 110L
        }
    }
}
