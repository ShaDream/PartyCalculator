package feature.group

import action.Action
import feature.IFeature
import helper.NameChecker
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import manager.*
import message.Buttons
import message.Message
import repository.Group
import repository.GroupRepo
import repository.ParticipantsRepo
import repository.User
import state.State
import state.StateManager


class GroupEditFeature(private val groupRepo: GroupRepo, private val participantsRepo: ParticipantsRepo) : IFeature {
    override fun bind(actions: Observable<Action>): Observable<Message> {
        return actions.observeOn(Schedulers.computation())
            .filter { it is Action.Group.Edit }
            .map { action ->
                when (action) {
                    is Action.Group.Edit.Start -> {
                        StateManager.setStateByChatId(action.chatId, State.Group)
                        GroupManager.createEditState(action.chatId, action.message)

                        Message.Text(
                            message = "Вы находитесь в окне редактирования группы ${action.message}",
                            chatId = action.chatId,
                            buttons = Buttons.from(
                                listOf(listOf("/addMembers", "/deleteMembers", "/deleteGroup", "/end"))
                            )
                        )
                    }

                    is Action.Group.Edit.AddMembers -> {
                        val participants = participantsRepo.getUsersByChatId(action.chatId)
                        GroupManager.createChoiceManager(action.chatId, participants)

                        Message.Text(
                            message = "Выберите пользователей для добавления:",
                            chatId = action.chatId,
                            buttons = Buttons.from(
                                listOf(
                                    GroupManager.getChoiceManager(action.chatId).getButtons({ it.name }, "⬅️", "➡️"),
                                    listOf("/back", "/discard", "/apply")
                                )
                            )
                        )
                    }

                    is Action.Group.Edit.Previous -> {
                        val cM = GroupManager.getChoiceManager(action.chatId)
                        cM.previousPage()

                        Message.Text(
                            message = "Следующая страница:",
                            chatId = action.chatId,
                            buttons = Buttons.from(
                                listOf(
                                    cM.getButtons({ it.name }, "⬅️", "➡️"),
                                    listOf("/back", "/discard", "/apply")
                                )
                            )
                        )
                    }

                    is Action.Group.Edit.Next -> {
                        val cM = GroupManager.getChoiceManager(action.chatId)
                        cM.nextPage()

                        Message.Text(
                            message = "Следующая страница:",
                            chatId = action.chatId,
                            buttons = Buttons.from(
                                listOf(
                                    cM.getButtons({ it.name }, "⬅️", "➡️"),
                                    listOf("/back", "/discard", "/apply")
                                )
                            )
                        )
                    }

                    is Action.Group.Edit.Choice -> {
                        val value = NameChecker.getNameWithoutCheckSymbol(action.message)
                        val cM = GroupManager.getChoiceManager(action.chatId)
                        val isUser = cM.elems.firstOrNull { it.name == value }

                        if (isUser == null) {
                            return@map Message.Text(
                                message = "Такого пользователя не существует.",
                                chatId = action.chatId,
                                buttons = Buttons.from(
                                    listOf()
                                )
                            )
                        }

                        cM.toggle(isUser)

                        return@map Message.Text(
                            message = "Нажали на пользователя ${value}.",
                            chatId = action.chatId,
                            buttons = Buttons.from(
                                listOf(
                                    cM.getButtons({ it.name }, "⬅️", "➡️"),
                                    listOf("/back", "/discard", "/apply")
                                )
                            )
                        )

                    }

                    is Action.Group.Edit.Apply -> {
                        val cM = GroupManager.getChoiceManager(action.chatId)
                        if (cM.getSelected().isEmpty()) {
                            return@map Message.Text(
                                message = "Нужно выбрать хотя бы одного участника",
                                chatId = action.chatId,
                                buttons = Buttons.from(
                                    listOf()
                                )
                            )
                        }

                        val selectedUsers = cM.getSelected().map { it.id }

                        groupRepo.addUsersToGroup(action.chatId, GroupManager.getEditGroupState(action.chatId), selectedUsers)
                        StateManager.setStateByChatId(action.chatId, State.None)
                        GroupManager.removeAddState(action.chatId)
                        Message.Text(
                            message = "Пользователи добавлены в группу",
                            chatId = action.chatId,
                            buttons = Buttons.from(
                                listOf(listOf())
                            )
                        )
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

