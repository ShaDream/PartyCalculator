package action

sealed class Action(chatId: Long) {

    sealed class Main(chatId: Long) : Action(chatId) {
        data class Start(val chatId: Long) : Main(chatId)
        data class Help(val chatId: Long) : Main(chatId)
        data class People(val chatId: Long) : Main(chatId)
        data class Receipt(val chatId: Long) : Main(chatId)
        data class Group(val chatId: Long) : Main(chatId)
    }

    sealed class Participant(chatId: Long) : Action(chatId) {
        sealed class Add(chatId: Long) : Participant(chatId) {
            data class Start(val chatId: Long) : Add(chatId)
            data class End(val chatId: Long) : Add(chatId)
            data class New(val chatId: Long, val message: String) : Add(chatId)
        }

        sealed class Remove(chatId: Long): Action(chatId){
            data class Start(val chatId: Long): Remove(chatId)
            data class End(val chatId: Long): Remove(chatId)
            data class Next(val chatId: Long): Remove(chatId)
            data class Previous(val chatId: Long): Remove(chatId)
            data class Choice(val chatId: Long, val message: String) : Remove(chatId)
            data class Delete(val chatId: Long) : Remove(chatId)
        }

        data class List(val chatId: Long) : Participant(chatId)
    }

    data class UndefinedAction(val chatId: Long) : Action(chatId)

}