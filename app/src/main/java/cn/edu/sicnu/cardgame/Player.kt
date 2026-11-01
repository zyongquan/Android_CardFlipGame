package cn.edu.sicnu.cardgame

/** 性别（Model 层只保存枚举，显示文字由 View / Controller 通过字符串资源映射） */
enum class Gender { MALE, FEMALE }

/** 爱好（可多选） */
enum class Hobby { TRAVEL, READING, GAME, SHOPPING }

/**
 * 玩家信息（Model 层）。
 *
 * 实验要求(2)：修改后的信息无需保存到文件中，因此这里只把数据放在内存里的
 * PlayerRepository 单例中，3 个 Activity 通过它共享同一份数据。
 *
 * 实现 Serializable 是为了能直接放进 Intent，在 Activity 之间传递。
 */
data class Player(
    var name: String,
    var studentId: String,
    var phone: String,
    var email: String,
    var gender: Gender,
    val hobbies: MutableSet<Hobby>,
    /** 游戏最高分，随对局结果更新 */
    var highScore: Int = 0
) : java.io.Serializable
