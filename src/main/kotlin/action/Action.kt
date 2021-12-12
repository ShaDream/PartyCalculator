package action

sealed class Action(chatId: Long) {

    sealed class Main(chatId: Long) : Action(chatId) {
        data class Start(val chatId: Long) : Main(chatId)
        data class Help(val chatId: Long) : Main(chatId)
        data class People(val chatId: Long) : Main(chatId)
        data class Receipt(val chatId: Long) : Main(chatId)
        data class Group(val chatId: Long) : Main(chatId)
    }

    data class UndefinedAction(val chatId: Long) : Action(chatId)

}