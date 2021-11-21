package message

sealed class Message(chatId: Long) {

    data class Text(val message: String, val chatId: Long): Message(chatId)

}
