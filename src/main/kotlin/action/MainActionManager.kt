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
            "/editGroups" -> Action.Group.Edit.Start(chatId)
            "/listGroups" -> Action.Group.List(chatId)
            "/removeGroups" -> Action.Group.Remove.Start(chatId)

            "/createPeople" -> Action.Participant.Add.Start(chatId)
            "/listPeople" -> Action.Participant.List(chatId)
            "/deletePeople" -> Action.Participant.Remove.Start(chatId)

            "/addReceipt" -> Action.Receipt.Add.Start(chatId)
            "/listReceipts" -> Action.Receipt.List(chatId)
            "/removeReceipt" -> Action.Receipt.Remove.Start(chatId)
            "/calculateReceipts" -> Action.Receipt.Calculate(chatId)

            else -> Action.UndefinedAction(chatId)
        }
    }
}