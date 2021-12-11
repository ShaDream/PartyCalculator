package message

import com.github.kotlintelegrambot.entities.KeyboardReplyMarkup
import com.github.kotlintelegrambot.entities.keyboard.KeyboardButton

data class Buttons(private val values: List<KeyboardButton>) {

    companion object {
        fun from(source: List<String>): Buttons {
            return Buttons(source.map { KeyboardButton(it) })
        }
    }

    fun toKeyboardReplyMarkup(): KeyboardReplyMarkup? {
        if (values.isEmpty()) return null
        // Ещё можно стереть клавиатуру, отправив обратно ReplyKeyboardRemove()
        return KeyboardReplyMarkup(
            keyboard = values.map { listOf(it) },
            resizeKeyboard = true
        )
    }
}
