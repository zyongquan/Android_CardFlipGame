package cn.edu.sicnu.cardgame

import android.content.Context

/**
 * 玩家信息的“仓库”（Model 层的单例）。
 *
 * 实验要求(2)说明修改后的个人信息无需写入文件，所以这里用一个进程内单例保存数据：
 *  - 3 个 Activity 读写同一份 Player，主界面、信息修改、翻牌游戏之间自动同步；
 *  - 旋转屏幕导致 Activity 重建时数据也不会丢（单例独立于 Activity 生命周期）。
 */
object PlayerRepository {

    private var player: Player? = null

    /** 取出当前玩家，第一次访问时用字符串资源里的默认值初始化 */
    fun get(context: Context): Player {
        return player ?: defaultPlayer(context).also { player = it }
    }

    /** 覆盖保存当前玩家（信息修改界面点“保存”后调用） */
    fun save(newPlayer: Player) {
        player = newPlayer
    }

    /** 用默认值构造玩家：姓名、学号取自字符串资源，同样是“姓名+学号”这个学号 */
    private fun defaultPlayer(context: Context): Player {
        return Player(
            name = context.getString(R.string.author_name),
            studentId = context.getString(R.string.author_student_id),
            phone = "",
            email = "",
            gender = Gender.MALE,
            hobbies = mutableSetOf(Hobby.READING, Hobby.GAME)
        )
    }
}
