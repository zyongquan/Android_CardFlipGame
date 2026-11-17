package cn.edu.sicnu.cardgame

import android.content.Context
import android.util.Log
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

/**
 * 翻牌游戏状态的文件存储。
 *
 * 两个游戏 Fragment 分别使用 two_card_game.dat 和 three_card_game.dat。
 * 每次翻牌、重新开始和 onStop() 时把整个 CardMatchingGame 对象序列化写入文件，
 * 再次打开应用时读回，从而恢复牌面、得分、已配对状态和本轮选择。
 */
object GameStore {

    private const val TAG = "GameStore"

    fun save(context: Context, fileName: String, game: CardMatchingGame): Boolean {
        return try {
            context.openFileOutput(fileName, Context.MODE_PRIVATE).use { file ->
                BufferedOutputStream(file).use { buffer ->
                    ObjectOutputStream(buffer).use { output ->
                        output.writeObject(game)
                    }
                }
            }
            true
        } catch (e: Exception) {
            Log.w(TAG, "保存牌局失败：$fileName", e)
            false
        }
    }

    /** 读取牌局；文件不存在、损坏或类型不匹配时返回 null */
    fun load(context: Context, fileName: String): CardMatchingGame? {
        return try {
            context.openFileInput(fileName).use { file ->
                BufferedInputStream(file).use { buffer ->
                    ObjectInputStream(buffer).use { input ->
                        input.readObject() as? CardMatchingGame
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "读取牌局失败：$fileName", e)
            null
        }
    }

    fun exists(context: Context, fileName: String): Boolean {
        return context.getFileStreamPath(fileName).exists()
    }

    fun clear(context: Context, fileName: String) {
        context.deleteFile(fileName)
    }
}
