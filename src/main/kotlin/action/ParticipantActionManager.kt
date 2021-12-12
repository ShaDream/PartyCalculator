package action

import manager.ParticipantManager

class ParticipantActionManager: IActionsManager {
    override fun getAction(command: String?, chatId: Long): Action {
        if (ParticipantManager.hasInParticipantState(chatId)){
            return when (command){
                "/end" -> Action.Participant.Add.End(chatId)
                else -> Action.Participant.Add.New(chatId, command.orEmpty())
            }
        }

        return Action.UndefinedAction(chatId)
    }
}