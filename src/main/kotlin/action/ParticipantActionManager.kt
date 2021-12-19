package action

import manager.ParticipantManager

class ParticipantActionManager : IActionsManager {
    override fun getAction(command: String?, chatId: Long): Action {
        when {
            ParticipantManager.hasAddState(chatId) -> {
                return when (command) {
                    "/end" -> Action.Participant.Add.End(chatId)
                    else -> Action.Participant.Add.New(chatId, command.orEmpty())
                }
            }

            ParticipantManager.hasRemoveState(chatId) -> {
                return when (command) {
                    "/end" -> Action.Participant.Remove.End(chatId)
                    "➡️" -> Action.Participant.Remove.Next(chatId)
                    "⬅️" -> Action.Participant.Remove.Previous(chatId)
                    "/delete" -> Action.Participant.Remove.Delete(chatId)
                    else -> Action.Participant.Remove.Choice(chatId, command.orEmpty())
                }
            }
        }


        return Action.UndefinedAction(chatId)
    }
}