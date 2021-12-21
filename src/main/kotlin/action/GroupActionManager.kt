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
                    "/addMembers" -> Action.Group.Edit.AddMembers(chatId)
                    //"/deleteMembers" -> Action.Group.Edit.AddMembers(chatId)
                    "⬅️" -> Action.Group.Edit.Previous(chatId)
                    "➡️" -> Action.Group.Edit.Next(chatId)
                    "/apply" -> Action.Group.Edit.Apply(chatId)
                    else -> Action.Group.Edit.Choice(chatId, command.orEmpty())
                }
            }

            GroupManager.hasListState(chatId) -> {
                return when (command) {
                    "/end" -> Action.Group.List.End(chatId)
                    "/listGroup" -> Action.Group.List.Start(chatId)
                    else -> Action.Group.List.Start(chatId)
                }
            }
        }

        return Action.UndefinedAction(chatId)
    }

}