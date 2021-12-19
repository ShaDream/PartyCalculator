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

class GroupListFeature (private val groupRepo: GroupRepo) : IFeature {
    override fun bind(actions: Observable<Action>): Observable<Message> {
        return actions.observeOn(Schedulers.computation())
            .filter { it is Action.Group.List }
            .map { action ->
                when (action) {
                    is Action.Group.List.Start -> {
                        StateManager.setStateByChatId(action.chatId, State.Group)
                        GroupManager.addListState(action.chatId)
                        Message.Text(
                            message = "Здесь будет список всех групп кнопками",
                            chatId = action.chatId,
                            buttons = Buttons.from(listOf("/end"))
                        )
                    }

                    is Action.Group.List.End -> {
                        if (GroupManager.hasListState(action.chatId)) {
                            GroupManager.removeListState(action.chatId)
                            StateManager.setStateByChatId(action.chatId, State.None)

                            Message.Text(
                                message = "Вы вышли из режима просмотра групп.",
                                chatId = action.chatId,
                                buttons = Buttons.from(listOf("/group")),
                            )
                        } else {
                            Message.Text(
                                message = "Вы не находитесь в режиме просмотра групп:",
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