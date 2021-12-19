package action

import manager.GroupManager

class GroupActionManager : IActionsManager {
    override fun getAction(command: String?, chatId: Long): Action {
        when {
            GroupManager.hasAddState(chatId) -> {
                return when (command) {
                    "/end" -> Action.Group.Add.End(chatId)
                    "/createGroup" -> Action.Group.Add.New(chatId, command.orEmpty())
                    //"/next" -> Action.Group.Add.Next(chatId)
                    //"/previous" -> Action.Group.Add.Previous(chatId)
                    else -> Action.Group.Add.New(chatId, command.orEmpty())
                }
            }
            GroupManager.hasEditState(chatId) -> {
                return when (command) {
                    "/end" -> Action.Group.Edit.End(chatId)
                    "/editMembers" -> Action.Group.Edit.Members(chatId)
                    //"/next" -> Action.Group.Add.Next(chatId)
                    //"/previous" -> Action.Group.Add.Previous(chatId)
                    else -> Action.Group.Edit.Start(chatId, command.orEmpty())
                }
            }

            GroupManager.hasListState(chatId) -> {
                return when (command) {
                    "/end" -> Action.Group.List.End(chatId)
                    "/listGroup" -> Action.Group.List.Start(chatId)
                    //"/editMembers" -> Action.Group.Edit.Members(chatId)
                    //"/next" -> Action.Group.Add.Next(chatId)
                    //"/previous" -> Action.Group.Add.Previous(chatId)
                    else -> Action.Group.List.Start(chatId)
                }
            }
        }

        return Action.UndefinedAction(chatId)
    }

}