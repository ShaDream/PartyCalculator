package message

import com.github.kotlintelegrambot.entities.KeyboardReplyMarkup
import com.github.kotlintelegrambot.entities.keyboard.KeyboardButton

data class Buttons(private val values: List<List<KeyboardButton>>) {

    companion object {
        fun from(matrix: List<List<String>>): Buttons {
            return Buttons(matrix.map { it.map { str -> KeyboardButton(str) } })
        }
    }

    fun toKeyboardReplyMarkup(): KeyboardReplyMarkup? {
        if (values.isEmpty()) return null
        // Ещё можно стереть клавиатуру, отправив обратно ReplyKeyboardRemove()
        return KeyboardReplyMarkup(
            keyboard = values,
            resizeKeyboard = true
        )
    }
}
