package feature.group

import action.Action
import feature.IFeature
import helper.NameChecker
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import manager.*
import message.Buttons
import message.Message
import repository.*
import state.State
import state.StateManager


class GroupEditFeature(private val groupRepo: GroupRepo, private val participantsRepo: ParticipantsRepo) : IFeature {
    private fun getParticipantsButtons(groupParticipants: GroupUsersEditState): List<List<String>> {
        return listOf(
            groupParticipants.usersInGroup.getButtons({ it.name }, "⬅️", "➡️"),
            groupParticipants.usersNotInGroup.getButtons({ it.name }, "◀️", "▶️"),
            listOf("/back", "/discard", "/apply")
        )
    }

    private fun initializeParticipants(chatId: Long, participants: List<User>, groupMembersIds: List<UserId>){

        GroupManager.removeUsersChoiceManager(chatId)
        val participantsInGroup: MutableList<User> = mutableListOf()
        val participantsNotInGroup: MutableList<User> = mutableListOf()

        participants.forEach {
            if (groupMembersIds.contains(it.id)) {
                participantsInGroup.add(it)
            } else {
                participantsNotInGroup.add(it)
            }
        }

        GroupManager.createUsersChoiceManager(chatId, participantsInGroup, participantsNotInGroup)
    }

    override fun bind(actions: Observable<Action>): Observable<Message> {
        return actions.observeOn(Schedulers.computation())
            .filter { it is Action.Group.Edit }
            .map { action ->
                when (action) {
                    is Action.Group.Edit.EditMembers -> {
                        if(!GroupManager.hasEditState(action.chatId))
                        {
                            return@map Message.Text(
                                message = "Вы не в режиме редактирования группы.",
                                chatId = action.chatId,
                                buttons = Buttons.from(listOf())
                            )
                        }

                        val participants = participantsRepo.getUsersByChatId(action.chatId)
                        val groupMembersIds = groupRepo.getGroup(action.chatId, GroupManager.getEditGroupName(action.chatId)).users

                        GroupManager.removeUsersChoiceManager(action.chatId)
                        val participantsInGroup: MutableList<User> = mutableListOf()
                        val participantsNotInGroup: MutableList<User> = mutableListOf()

                        participants.forEach {
                            if (groupMembersIds.contains(it.id)) {
                                participantsInGroup.add(it)
                            } else {
                                participantsNotInGroup.add(it)
                            }
                        }

                        GroupManager.createUsersChoiceManager(action.chatId, participantsInGroup, participantsNotInGroup)

                        Message.Text(
                            message = "Выберите пользователей. \n" +
                                    "Верхний список - пользователи не в группе. Нижний - в группе:",
                            chatId = action.chatId,
                            buttons = Buttons.from(getParticipantsButtons(GroupManager.getUsersChoiceManager(action.chatId)))
                        )
                    }

                    is Action.Group.Edit.PreviousUsersInGroup -> {
                        val cM = GroupManager.getUsersChoiceManager(action.chatId)
                        cM.usersInGroup.previousPage()

                        Message.Text(
                            message = "Предыдущая страница:",
                            chatId = action.chatId,
                            buttons = Buttons.from(getParticipantsButtons(cM))
                        )
                    }

                    is Action.Group.Edit.NextUsersInGroup -> {
                        val cM = GroupManager.getUsersChoiceManager(action.chatId)
                        cM.usersInGroup.nextPage()

                        Message.Text(
                            message = "Следующая страница:",
                            chatId = action.chatId,
                            buttons = Buttons.from(getParticipantsButtons(cM))
                        )
                    }

                    is Action.Group.Edit.PreviousUsersNotInGroup -> {
                        val cM = GroupManager.getUsersChoiceManager(action.chatId)
                        cM.usersNotInGroup.previousPage()

                        Message.Text(
                            message = "Предыдущая страница:",
                            chatId = action.chatId,
                            buttons = Buttons.from(getParticipantsButtons(cM))
                        )
                    }

                    is Action.Group.Edit.NextUsersNotInGroup -> {
                        val cM = GroupManager.getUsersChoiceManager(action.chatId)
                        cM.usersNotInGroup.nextPage()

                        Message.Text(
                            message = "Следующая страница:",
                            chatId = action.chatId,
                            buttons = Buttons.from(getParticipantsButtons(cM))
                        )
                    }

                    is Action.Group.Edit.Choice -> {
                        val value = NameChecker.getNameWithoutCheckSymbol(action.message)
                        val cM = GroupManager.getUsersChoiceManager(action.chatId)
                        val isUserIn = cM.usersInGroup.elems.firstOrNull { it.name == value }
                        val isUserNotIn = cM.usersNotInGroup.elems.firstOrNull { it.name == value }

                        if (isUserIn == null && isUserNotIn == null) {
                            return@map Message.Text(
                                message = "Такого пользователя не существует.",
                                chatId = action.chatId,
                                buttons = Buttons.from(
                                    listOf()
                                )
                            )
                        }
                        else
                        {
                            if(isUserNotIn != null){
                                cM.usersNotInGroup.toggle(isUserNotIn)
                            }
                            if (isUserIn != null) {
                                cM.usersNotInGroup.toggle(isUserIn)
                            }
                        }

                        return@map Message.Text(
                            message = "Выбрали пользователя ${value}.",
                            chatId = action.chatId,
                            buttons = Buttons.from(getParticipantsButtons(cM))
                        )
                    }

                    is Action.Group.Edit.Apply -> {
                        val cM = GroupManager.getUsersChoiceManager(action.chatId)
                        if (cM.usersInGroup.getSelected().isEmpty() && cM.usersNotInGroup.getSelected().isEmpty()) {
                            return@map Message.Text(
                                message = "Нужно выбрать хотя бы одного участника",
                                chatId = action.chatId,
                                buttons = Buttons.from(
                                    listOf()
                                )
                            )
                        }

                        val selectedUsersInGroup = cM.usersInGroup.getSelected().map { it.id }
                        val selectedUsersNotInGroup = cM.usersNotInGroup.getSelected().map { it.id }

                        groupRepo.removeUsersFromGroup(action.chatId, GroupManager.getEditGroupName(action.chatId), selectedUsersInGroup)
                        groupRepo.addUsersToGroup(action.chatId, GroupManager.getEditGroupName(action.chatId), selectedUsersNotInGroup)

                        val participants = participantsRepo.getUsersByChatId(action.chatId)
                        val groupMembersIds = groupRepo.getGroup(action.chatId, GroupManager.getEditGroupName(action.chatId)).users

                        GroupManager.removeUsersChoiceManager(action.chatId)
                        val participantsInGroup: MutableList<User> = mutableListOf()
                        val participantsNotInGroup: MutableList<User> = mutableListOf()

                        participants.forEach {
                            if (groupMembersIds.contains(it.id)) {
                                participantsInGroup.add(it)
                            } else {
                                participantsNotInGroup.add(it)
                            }
                        }

                        GroupManager.createUsersChoiceManager(action.chatId, participantsInGroup, participantsNotInGroup)

                        Message.Text(
                            message = "Пользователи добавлены в группу",
                            chatId = action.chatId,
                            buttons = Buttons.from(getParticipantsButtons(cM))
                        )
                    }

                    is Action.Group.Edit.Back -> {
                        if (GroupManager.hasEditState(action.chatId)) {

                            GroupManager.removeUsersChoiceManager(action.chatId)

                            Message.Text(
                                message = "Вы вышли из режима создания групп.",
                                chatId = action.chatId,
                                buttons = Buttons.from(listOf(listOf("/editMembers", "/deleteGroup", "/end")))
                            )
                        } else {
                            Message.Text(
                                message = "Вы не находитесь в режиме редактирования групп:",
                                chatId = action.chatId,
                                buttons = Buttons.from(listOf()),
                            )
                        }
                    }

                    is Action.Group.Edit.End -> {
                        if (GroupManager.hasEditState(action.chatId)) {
                            GroupManager.removeEditState(action.chatId)
                            StateManager.setStateByChatId(action.chatId, State.None)

                            Message.Text(
                                message = "Вы вышли из режима добавления участников:",
                                chatId = action.chatId,
                                buttons = Buttons.from(listOf(listOf("/group"))),
                            )
                        } else {
                            Message.Text(
                                message = "Вы не находитесь в режиме добавления участников:",
                                chatId = action.chatId,
                                buttons = Buttons.from(listOf()),
                            )
                        }
                    }
                    else -> throw IllegalArgumentException("Not implemented Action for this feature")
                }
            }
    }
}

