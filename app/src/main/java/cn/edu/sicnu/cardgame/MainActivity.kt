package cn.edu.sicnu.cardgame

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import cn.edu.sicnu.cardgame.databinding.ActivityMainBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/**
 * 主界面（Fragment 容器 + Controller）。
 *
 * 实验六把原来的 3 个 Activity 改造成 3 个 Fragment：
 *  - PersonalInfoFragment：个人信息，使用 Player 对象序列化保存；
 *  - TwoCardGameFragment：两张牌翻牌游戏，保存完整 CardMatchingGame 对象；
 *  - ThreeCardGameFragment：三张牌翻牌游戏，保存完整 CardMatchingGame 对象。
 *
 * MainActivity 只负责 BottomNavigationView 的切换和「关于」菜单，
 * 具体界面与业务逻辑分别放在各个 Fragment 中。
 */
class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG_PERSONAL = "fragment_personal"
        private const val TAG_TWO_CARD = "fragment_two_card"
        private const val TAG_THREE_CARD = "fragment_three_card"
        private const val KEY_CURRENT_TAG = "key_current_tag"
    }

    private lateinit var binding: ActivityMainBinding
    private var currentTag = TAG_PERSONAL

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setSubtitle(R.string.subtitle_main)

        currentTag = savedInstanceState?.getString(KEY_CURRENT_TAG) ?: TAG_PERSONAL

        // 首次创建时显示个人信息 Fragment；Activity 重建时 FragmentManager 会恢复原 Fragment
        if (savedInstanceState == null ||
            supportFragmentManager.findFragmentById(R.id.fragment_container) == null
        ) {
            showFragment(currentTag)
        }

        // 先设置选中项，再注册监听，避免恢复状态时重复 replace Fragment
        binding.bottomNav.selectedItemId = navIdFor(currentTag)
        binding.bottomNav.setOnItemSelectedListener { item ->
            val tag = tagFor(item.itemId)
            if (tag != null && tag != currentTag) {
                showFragment(tag)
            }
            true
        }
    }

    private fun showFragment(tag: String) {
        val fragment = when (tag) {
            TAG_TWO_CARD -> TwoCardGameFragment()
            TAG_THREE_CARD -> ThreeCardGameFragment()
            else -> PersonalInfoFragment()
        }
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment, tag)
            .commit()
        currentTag = tag
        supportActionBar?.setSubtitle(subtitleFor(tag))
    }

    private fun navIdFor(tag: String): Int {
        return when (tag) {
            TAG_TWO_CARD -> R.id.nav_two_card
            TAG_THREE_CARD -> R.id.nav_three_card
            else -> R.id.nav_personal
        }
    }

    private fun tagFor(itemId: Int): String? {
        return when (itemId) {
            R.id.nav_personal -> TAG_PERSONAL
            R.id.nav_two_card -> TAG_TWO_CARD
            R.id.nav_three_card -> TAG_THREE_CARD
            else -> null
        }
    }

    private fun subtitleFor(tag: String): Int {
        return when (tag) {
            TAG_TWO_CARD -> R.string.subtitle_two_card
            TAG_THREE_CARD -> R.string.subtitle_three_card
            else -> R.string.subtitle_personal_info
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(KEY_CURRENT_TAG, currentTag)
        super.onSaveInstanceState(outState)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_about -> {
                showAboutDialog()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    /** 「关于」对话框，内容全部来自字符串资源 */
    private fun showAboutDialog() {
        MaterialAlertDialogBuilder(this)
            .setIcon(R.drawable.ic_info)
            .setTitle(R.string.about_title)
            .setMessage(
                getString(
                    R.string.about_message,
                    getString(R.string.app_name),
                    getString(R.string.app_version),
                    getString(R.string.author_name),
                    getString(R.string.author_student_id)
                )
            )
            .setPositiveButton(R.string.about_ok, null)
            .show()
    }
}
