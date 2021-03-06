package action

import manager.GroupManager
import manager.ParticipantManager

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
                    "/deleteGroup" -> Action.Group.Edit.Delete(chatId)
                    else -> Action.Group.Edit.Choice(chatId, command.orEmpty())
                }
            }

            GroupManager.hasChooseToEditState(chatId) -> {
                return when (command) {
                    "⬅️" -> Action.Group.Edit.Previous(chatId)
                    "➡️" -> Action.Group.Edit.Next(chatId)
                    "/back" -> Action.Group.Edit.Back(chatId)
                    else -> Action.Group.Edit.ChoiceOfGroup(chatId, command.orEmpty())
                }
            }
            GroupManager.hasRemoveState(chatId) -> {
                return when (command) {
                    "/end" -> Action.Group.Remove.End(chatId)
                    "➡️" -> Action.Group.Remove.Next(chatId)
                    "⬅️" -> Action.Group.Remove.Previous(chatId)
                    "/delete" -> Action.Group.Remove.Delete(chatId)
                    else -> Action.Group.Remove.Choice(chatId, command.orEmpty())
                }
            }

        }

        return Action.UndefinedAction(chatId)
    }

}