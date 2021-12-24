package action

import manager.GroupManager

class GroupActionManager : IActionsManager {
    override fun getAction(command: String?, chatId: Long): Action {
        when {
            GroupManager.hasAddState(chatId) -> {
                return when (command) {
                    "/discard" -> Action.Group.Add.Discard(chatId)
                    else -> Action.Group.Add.New(chatId, command.orEmpty())
                }
            }
            GroupManager.hasEditState(chatId) -> {
                return when (command) {
                    "/end" -> Action.Group.Edit.End(chatId)
                    "/editMembers" -> Action.Group.Edit.EditMembers(chatId)
                    "⬅️" -> Action.Group.Edit.PreviousUsersInGroup(chatId)
                    "➡️" -> Action.Group.Edit.NextUsersInGroup(chatId)
                    "▶️" -> Action.Group.Edit.NextUsersNotInGroup(chatId)
                    "◀️" -> Action.Group.Edit.PreviousUsersNotInGroup(chatId)
                    "/apply" -> Action.Group.Edit.Apply(chatId)
                    "/back" -> Action.Group.Edit.Back(chatId)
                    else -> Action.Group.Edit.Choice(chatId, command.orEmpty())
                }
            }
            
        }

        return Action.UndefinedAction(chatId)
    }

}