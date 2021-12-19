package action

class MainActionManager : IActionsManager {
    override fun getAction(command: String?, chatId: Long): Action {
        return when (command) {
            "/start" -> Action.Main.Start(chatId)
            "/help" -> Action.Main.Help(chatId)
            "/people" -> Action.Main.People(chatId)
            "/receipt" -> Action.Main.Receipt(chatId)
            "/group" -> Action.Main.Group(chatId)

            "/createGroup" -> Action.Group.Add.Start(chatId)

            "/createPeople" -> Action.Participant.Add.Start(chatId)
            "/listPeople" -> Action.Participant.List(chatId)
            "/deletePeople" -> Action.Participant.Remove.Start(chatId)

            "/addReceipt" -> Action.Receipt.Add.Start(chatId)
            else -> Action.UndefinedAction(chatId)
        }
    }
}