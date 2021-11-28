package action

class MainActionManager : IActionsManager {
    override fun getAction(command: String?, chatId: Long): Action {
        return when (command) {
            "/start" -> Action.Start(chatId)
            "/help" -> Action.Help(chatId)
            "/people" -> Action.People(chatId)
            "/receipt" -> Action.Receipt(chatId)
            "/group" -> Action.Group(chatId)
            else -> Action.UndefinedAction(chatId)
        }
    }
}