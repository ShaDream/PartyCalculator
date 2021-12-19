package feature.group

import action.Action
import feature.IFeature
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import manager.GroupManager
import manager.ParticipantManager
import message.Buttons
import message.Message
import repository.GroupRepo
import repository.ParticipantsRepo
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
                                listOf(listOf("/editMembers", "/delete", "/end"))
                            )
                        )
                    }

                    is Action.Group.Edit.Members -> {
                        Message.Text(
                            message = "Здесь будет список пользователей",
                            chatId = action.chatId,
                            buttons = Buttons.from(listOf(listOf("/end")))
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

