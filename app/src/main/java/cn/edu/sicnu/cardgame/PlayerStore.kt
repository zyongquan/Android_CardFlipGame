package cn.edu.sicnu.cardgame

import android.content.Context
import android.util.Log
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

/**
 * 个人信息文件存储（Model / Repository 层）。
 *
 * 严格按照实验说明使用 Context.openFileOutput / openFileInput，
 * 并用 ObjectOutputStream / ObjectInputStream 读写 Serializable 的 Player 对象。
 * 文件名 player.dat 保存在应用私有目录中，卸载应用后才会删除。
 */
object PlayerStore {

    const val FILE_NAME = "player.dat"
    private const val TAG = "PlayerStore"

    /** 把 Player 序列化写入应用私有文件 */
    fun save(context: Context, player: Player): Boolean {
        return try {
            context.openFileOutput(FILE_NAME, Context.MODE_PRIVATE).use { file ->
                BufferedOutputStream(file).use { buffer ->
                    ObjectOutputStream(buffer).use { output ->
                        output.writeObject(player)
                    }
                }
            }
            true
        } catch (e: Exception) {
            Log.w(TAG, "保存个人信息失败", e)
            false
        }
    }

    /** 从应用私有文件反序列化读取 Player；文件不存在或损坏时返回 null */
    fun load(context: Context): Player? {
        return try {
            context.openFileInput(FILE_NAME).use { file ->
                BufferedInputStream(file).use { buffer ->
                    ObjectInputStream(buffer).use { input ->
                        input.readObject() as? Player
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "读取个人信息失败，使用默认值", e)
            null
        }
    }

    fun exists(context: Context): Boolean {
        return context.getFileStreamPath(FILE_NAME).exists()
    }

    fun clear(context: Context) {
        context.deleteFile(FILE_NAME)
    }

    /** 第一次使用或文件损坏时使用的默认玩家信息 */
    fun defaultPlayer(context: Context): Player {
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
