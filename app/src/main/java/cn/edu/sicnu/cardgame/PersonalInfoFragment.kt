package cn.edu.sicnu.cardgame

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import cn.edu.sicnu.cardgame.databinding.FragmentPersonalInfoBinding

/**
 * 个人信息 Fragment（View + Controller）。
 *
 * 使用 PlayerStore 在应用私有目录读写 player.dat：
 *  - 打开时通过 ObjectInputStream 恢复上次保存的 Player；
 *  - 点击「保存」或 Fragment 停止时通过 ObjectOutputStream 写入 Player。
 * 这样退出应用再重新打开，姓名、学号、电话、邮箱、性别和爱好都能恢复。
 */
class PersonalInfoFragment : Fragment() {

    private var _binding: FragmentPersonalInfoBinding? = null
    private val binding get() = _binding!!

    private lateinit var player: Player

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPersonalInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val hasSavedFile = PlayerStore.exists(requireContext())
        player = PlayerStore.load(requireContext())
            ?: PlayerStore.defaultPlayer(requireContext())
        fillForm(player)
        binding.tvStatus.text = getString(
            if (hasSavedFile) R.string.personal_status_loaded else R.string.personal_status_default
        )

        binding.btnSave.setOnClickListener {
            savePlayer(showToast = true)
        }
    }

    override fun onResume() {
        super.onResume()
        (activity as? AppCompatActivity)?.supportActionBar?.setSubtitle(R.string.subtitle_personal_info)
    }

    override fun onStop() {
        // 用户没有主动点保存时，退出/切换 Fragment 也保存一次，保证下次打开能恢复
        savePlayer(showToast = false)
        super.onStop()
    }

    private fun fillForm(player: Player) {
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

    /** View → Model：读取表单，序列化保存到 player.dat */
    private fun savePlayer(showToast: Boolean): Boolean {
        val name = binding.etName.text.toString().trim()
        if (name.isEmpty()) {
            if (showToast) {
                Toast.makeText(requireContext(), R.string.toast_name_empty, Toast.LENGTH_SHORT).show()
            }
            return false
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

        val saved = PlayerStore.save(requireContext(), player)
        if (saved) {
            binding.tvStatus.text = getString(R.string.personal_status_saved, PlayerStore.FILE_NAME)
        }
        if (showToast) {
            val message = if (saved) R.string.toast_saved else R.string.toast_save_failed
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }
        return saved
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
