package message

import com.github.kotlintelegrambot.entities.KeyboardReplyMarkup

sealed class Message(chatId: Long, buttons: Buttons) {
    data class Text(val message: String, val chatId: Long, val buttons: Buttons) : Message(chatId, buttons)
}
