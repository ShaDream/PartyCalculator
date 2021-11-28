package action

sealed class Action(chatId: Long) {

    data class Start(val chatId: Long) : Action(chatId)
    data class Help(val chatId: Long) : Action(chatId)
    data class People(val chatId: Long) : Action(chatId)
    data class Receipt(val chatId: Long) : Action(chatId)
    data class Group(val chatId: Long) : Action(chatId)

    data class UndefinedAction(val chatId: Long) : Action(chatId)

}