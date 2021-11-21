package action

sealed class Action(chatId: Long) {

    data class Start(val chatId: Long) : Action(chatId)

    data class UndefinedAction(val chatId: Long): Action(chatId)

}