package cn.edu.sicnu.cardgame

import java.io.Serializable

/** 性别（Model 层只保存枚举，显示文字由 View 层通过字符串资源映射） */
enum class Gender { MALE, FEMALE }

/** 爱好（可多选） */
enum class Hobby { TRAVEL, READING, GAME, SHOPPING }

/**
 * 玩家信息（Model 层）。
 *
 * 实验六要求个人信息也采用序列化方式保存：PersonalInfoFragment 使用
 * ObjectOutputStream 把 Player 写入 player.dat，再次打开应用时用
 * ObjectInputStream 读回，从而恢复上次保存的姓名、学号、电话、邮箱、性别和爱好。
 */
data class Player(
    var name: String,
    var studentId: String,
    var phone: String,
    var email: String,
    var gender: Gender,
    val hobbies: MutableSet<Hobby>,
    /** 兼容实验四、实验五保留的最高分字段 */
    var highScore: Int = 0
) : Serializable {

    companion object {
        private const val serialVersionUID = 1L
    }
}
