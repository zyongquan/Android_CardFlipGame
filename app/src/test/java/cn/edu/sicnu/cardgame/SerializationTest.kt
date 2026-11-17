package cn.edu.sicnu.cardgame

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

/**
 * 对象序列化单元测试。
 *
 * 实验六要求个人信息和翻牌游戏都用 ObjectOutputStream / ObjectInputStream 保存。
 * 这里在 JVM 上模拟同样的字节流读写，验证模型对象可以完整往返。
 */
class SerializationTest {

    private fun <T> roundTrip(value: T): T {
        val bytes = ByteArrayOutputStream().use { buffer ->
            ObjectOutputStream(buffer).use { it.writeObject(value) }
            buffer.toByteArray()
        }
        return ObjectInputStream(ByteArrayInputStream(bytes)).use {
            @Suppress("UNCHECKED_CAST")
            it.readObject() as T
        }
    }

    @Test
    fun twoCardGame_survivesSerialization() {
        val game = CardMatchingGame(12, 2)
        game.chooseCardAtIndex(0)
        val scoreBefore = game.score

        val restored = roundTrip(game)

        assertEquals(12, restored.cardCount)
        assertEquals(2, restored.cardsPerMatch)
        assertEquals(scoreBefore, restored.score)
        assertEquals(1, restored.selectedCount)
        assertTrue(restored.cardAtIndex(0).isChosen)
        assertEquals(game.cardAtIndex(0).toString(), restored.cardAtIndex(0).toString())
    }

    @Test
    fun threeCardGame_survivesSerialization() {
        val game = CardMatchingGame(24, 3)
        val restored = roundTrip(game)

        assertEquals(24, restored.cardCount)
        assertEquals(3, restored.cardsPerMatch)
        assertEquals(0, restored.score)
        assertFalse(restored.isGameOver)
        assertNotNull(restored.cardAtIndex(23))
    }

    @Test
    fun player_survivesSerialization() {
        val player = Player(
            name = "周勇权",
            studentId = "2024180750",
            phone = "13800000000",
            email = "test@example.com",
            gender = Gender.MALE,
            hobbies = mutableSetOf(Hobby.READING, Hobby.GAME)
        )

        val restored = roundTrip(player)

        assertEquals(player, restored)
        assertEquals("2024180750", restored.studentId)
        assertTrue(restored.hobbies.contains(Hobby.GAME))
    }
}
