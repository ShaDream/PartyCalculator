package action

import manager.ReceiptManager

class ReceiptActionManager : IActionsManager {
    override fun getAction(command: String?, chatId: Long): Action {
        when {
            ReceiptManager.hasAddState(chatId) -> {
                return when (command) {
                    "/discard" -> Action.Receipt.Add.Discard(chatId)
                    "/back" -> Action.Receipt.Add.Back(chatId)
                    "⬅️" -> Action.Receipt.Add.Previous(chatId)
                    "➡️" -> Action.Receipt.Add.Next(chatId)
                    "▶️" -> Action.Receipt.Add.NextGroup(chatId)
                    "◀️" -> Action.Receipt.Add.PreviousGroup(chatId)
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

            ReceiptManager.hasRemoveState(chatId) -> {
                return when (command) {
                    "/end" -> Action.Receipt.Remove.End(chatId)
                    "➡️" -> Action.Receipt.Remove.Next(chatId)
                    "⬅️" -> Action.Receipt.Remove.Previous(chatId)
                    "/delete" -> Action.Receipt.Remove.Delete(chatId)
                    else -> Action.Receipt.Remove.Choice(chatId, command.orEmpty())
                }
            }
        }

        return Action.UndefinedAction(chatId)
    }
}