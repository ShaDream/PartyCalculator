package action

class MainActionManager : IActionsManager {
    override fun getAction(command: String?, chatId: Long): Action {
        return when (command) {
            "/start" -> Action.Main.Start(chatId)
            "/help" -> Action.Main.Help(chatId)
            "/people" -> Action.Main.People(chatId)
            "/receipt" -> Action.Main.Receipt(chatId)
            "/group" -> Action.Main.Group(chatId)
            else -> Action.UndefinedAction(chatId)
        }
    }
}