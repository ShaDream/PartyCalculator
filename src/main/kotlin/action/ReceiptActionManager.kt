package action

import manager.ReceiptManager

class ReceiptActionManager : IActionsManager {
    override fun getAction(command: String?, chatId: Long): Action {
        when {
            ReceiptManager.hasAddState(chatId) -> {
                return when (command) {
                    "/discard" -> Action.Receipt.Add.Discard(chatId)
                    "/back" -> Action.Receipt.Add.Back(chatId)
                    "/previous" -> Action.Receipt.Add.Previous(chatId)
                    "/next" -> Action.Receipt.Add.Next(chatId)
                    "/nextGroup" -> Action.Receipt.Add.NextGroup(chatId)
                    "/previousGroup" -> Action.Receipt.Add.PreviousGroup(chatId)
                    "/apply" -> Action.Receipt.Add.Apply(chatId)
                    else -> {
                        val number = command.orEmpty().toFloatOrNull()
                        if (number != null) {
                            Action.Receipt.Add.Number(chatId, number)
                        } else {
                            Action.Receipt.Add.Choice(chatId, command.orEmpty())
                        }
                    }
                }
            }
        }

        return Action.UndefinedAction(chatId)
    }
}