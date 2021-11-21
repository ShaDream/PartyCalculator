package action

class ActionsManager {
    fun getAction(command: String, chatId: Long): Action = when (command) {
        "/start" -> Action.Start(chatId)
        else -> Action.UndefinedAction(chatId)
    }
}