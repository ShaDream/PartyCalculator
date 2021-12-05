package message

sealed class Message
(
    chatId: Long,
    buttonNames: List<String>
)
{
    data class Text(val message: String, val chatId: Long, val buttonNames: List<String>): Message(chatId, buttonNames)
}
