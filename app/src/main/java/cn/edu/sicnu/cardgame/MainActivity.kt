package cn.edu.sicnu.cardgame

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.IntentCompat
import cn.edu.sicnu.cardgame.databinding.ActivityMainBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/**
 * 主界面（Controller + View）。
 *
 * 负责：读取 PlayerRepository 里的玩家信息并显示；
 *       提供「修改个人信息」「翻牌游戏」两个入口；
 *       增加选项菜单（实验要求），点击「关于」后弹出 Material 对话框。
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var player: Player

    /** 信息修改界面回传修改后的 Player，结果码为 RESULT_OK 时才更新 */
    private val personalInfoLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode != RESULT_OK) return@registerForActivityResult
        val updated = result.data?.let {
            IntentCompat.getSerializableExtra(it, PersonalInfoActivity.EXTRA_PLAYER, Player::class.java)
        } ?: return@registerForActivityResult
        // 最高分保存在主界面的数据里，修改个人信息时不能被覆盖掉
        updated.highScore = player.highScore
        PlayerRepository.save(updated)
        refreshPlayer()
    }

    /** 翻牌游戏界面结束时回传本局的最高分 */
    private val cardGameLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val best = result.data?.getIntExtra(CardGameActivity.EXTRA_HIGH_SCORE, player.highScore)
            ?: return@registerForActivityResult
        if (result.resultCode == RESULT_OK && best > player.highScore) {
            player.highScore = best
            PlayerRepository.save(player)
            render()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setSubtitle(R.string.subtitle_main)

        refreshPlayer()

        binding.btnEditInfo.setOnClickListener {
            val intent = Intent(this, PersonalInfoActivity::class.java)
                .putExtra(PersonalInfoActivity.EXTRA_PLAYER, player)
            personalInfoLauncher.launch(intent)
        }

        binding.btnStartGame.setOnClickListener {
            val intent = Intent(this, CardGameActivity::class.java)
                .putExtra(CardGameActivity.EXTRA_HIGH_SCORE, player.highScore)
            cardGameLauncher.launch(intent)
        }
    }

    /** 加载选项菜单：menu_main.xml 里只有一个「关于」菜单项 */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    /** 点击菜单项：弹出「关于」对话框 */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_about -> {
                showAboutDialog()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    /** 用 MaterialAlertDialogBuilder 显示应用名称、版本、作者与玩法说明 */
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

    override fun onResume() {
        super.onResume()
        // 从二级界面返回时重新取一次数据，保证界面与仓库一致
        refreshPlayer()
    }

    private fun refreshPlayer() {
        player = PlayerRepository.get(this)
        render()
    }

    /** 把模型里的数据同步到界面（Model → View） */
    private fun render() {
        binding.tvHeaderName.text = player.name
        binding.tvHeaderStudentId.text = player.studentId

        binding.tvName.text = player.name
        binding.tvGender.text = genderLabel(player.gender)
        binding.tvStudentId.text = player.studentId
        binding.tvPhone.text = player.phone.ifBlank { getString(R.string.value_empty) }
        binding.tvEmail.text = player.email.ifBlank { getString(R.string.value_empty) }
        binding.tvHobby.text = hobbyText(player.hobbies)
        binding.tvHighScore.text = getString(R.string.main_high_score_format, player.highScore)
    }

    private fun genderLabel(gender: Gender): String {
        return getString(
            when (gender) {
                Gender.MALE -> R.string.gender_male
                Gender.FEMALE -> R.string.gender_female
            }
        )
    }

    /** 把爱好集合转成「旅游、阅读」这样的文字，按枚举顺序显示 */
    private fun hobbyText(hobbies: Set<Hobby>): String {
        val labels = hobbies.sortedBy { it.ordinal }.map { hobbyLabel(it) }
        return if (labels.isEmpty()) getString(R.string.value_empty) else
            labels.joinToString(getString(R.string.hobby_separator))
    }

    private fun hobbyLabel(hobby: Hobby): String {
        return getString(
            when (hobby) {
                Hobby.TRAVEL -> R.string.hobby_travel
                Hobby.READING -> R.string.hobby_reading
                Hobby.GAME -> R.string.hobby_game
                Hobby.SHOPPING -> R.string.hobby_shopping
            }
        )
    }
}
