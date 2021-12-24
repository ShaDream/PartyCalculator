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

class GroupListFeature(private val groupRepo: GroupRepo) : IFeature {
    override fun bind(actions: Observable<Action>): Observable<Message> {
        return actions.observeOn(Schedulers.computation())
            .filter { it is Action.Group.List }
            .map { action ->
                when (action) {
                    is Action.Group.List -> {
                        val groups = groupRepo.getGroups(action.chatId)

                        if (groups.isEmpty()) {
                            Message.Text(
                                message = "Список групп пуст.",
                                chatId = action.chatId,
                                buttons = Buttons.from(listOf()),
                            )
                        } else {
                            Message.Text(
                                message = "Список групп:\n" +
                                        groups.joinToString(separator = ", ") { it.name },
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