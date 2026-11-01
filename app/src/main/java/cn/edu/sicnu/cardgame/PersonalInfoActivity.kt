package cn.edu.sicnu.cardgame

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.IntentCompat
import cn.edu.sicnu.cardgame.databinding.ActivityPersonalInfoBinding

/**
 * 信息修改界面（Controller + View）。
 *
 * 打开时把传入的 Player 填进表单；点「保存」把表单内容写回 Player，
 * 通过 setResult() 把结果回传给主界面；点「取消」则回传 RESULT_CANCELED。
 */
class PersonalInfoActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_PLAYER = "extra_player"
    }

    private lateinit var binding: ActivityPersonalInfoBinding
    private lateinit var player: Player

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPersonalInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setSubtitle(R.string.subtitle_personal_info)

        player = IntentCompat.getSerializableExtra(intent, EXTRA_PLAYER, Player::class.java)
            ?: PlayerRepository.get(this)

        fillForm()

        binding.btnSave.setOnClickListener { saveAndFinish() }
        binding.btnCancel.setOnClickListener { cancelAndFinish() }
    }

    /** Model → View：把玩家信息填进各个输入控件 */
    private fun fillForm() {
        binding.etName.setText(player.name)
        binding.etStudentId.setText(player.studentId)
        binding.etPhone.setText(player.phone)
        binding.etEmail.setText(player.email)

        when (player.gender) {
            Gender.MALE -> binding.rbMale.isChecked = true
            Gender.FEMALE -> binding.rbFemale.isChecked = true
        }

        binding.cbTravel.isChecked = Hobby.TRAVEL in player.hobbies
        binding.cbReading.isChecked = Hobby.READING in player.hobbies
        binding.cbGame.isChecked = Hobby.GAME in player.hobbies
        binding.cbShopping.isChecked = Hobby.SHOPPING in player.hobbies
    }

    /** View → Model：读取表单内容写回 Player，并把结果回传给主界面 */
    private fun saveAndFinish() {
        val name = binding.etName.text.toString().trim()
        if (name.isEmpty()) {
            Toast.makeText(this, R.string.toast_name_empty, Toast.LENGTH_SHORT).show()
            return
        }

        player.name = name
        player.studentId = binding.etStudentId.text.toString().trim()
        player.phone = binding.etPhone.text.toString().trim()
        player.email = binding.etEmail.text.toString().trim()
        player.gender = if (binding.rbFemale.isChecked) Gender.FEMALE else Gender.MALE

        player.hobbies.clear()
        if (binding.cbTravel.isChecked) player.hobbies.add(Hobby.TRAVEL)
        if (binding.cbReading.isChecked) player.hobbies.add(Hobby.READING)
        if (binding.cbGame.isChecked) player.hobbies.add(Hobby.GAME)
        if (binding.cbShopping.isChecked) player.hobbies.add(Hobby.SHOPPING)

        // 实验要求(2)：修改后的信息无需保存到文件，只要在应用内共享即可
        PlayerRepository.save(player)

        Toast.makeText(this, R.string.toast_saved, Toast.LENGTH_SHORT).show()
        setResult(RESULT_OK, Intent().putExtra(EXTRA_PLAYER, player))
        finish()
    }

    private fun cancelAndFinish() {
        setResult(RESULT_CANCELED)
        finish()
    }
}
