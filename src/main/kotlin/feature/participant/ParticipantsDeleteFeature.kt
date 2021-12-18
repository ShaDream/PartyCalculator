package feature.participant

import action.Action
import action.ParticipantActionManager
import feature.IFeature
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import manager.ParticipantManager
import message.Buttons
import message.Message
import repository.ParticipantsRepo
import state.State
import state.StateManager
import java.lang.IllegalArgumentException

class ParticipantsDeleteFeature(participantsRepo: ParticipantsRepo) : IFeature {
    override fun bind(actions: Observable<Action>): Observable<Message> {
        return actions.observeOn(Schedulers.computation())
            .filter { it is Action.Participant.Remove }
            .map {
                when (it) {
                    is Action.Participant.Remove.Start -> {
                        ParticipantManager.addRemoveState(chatId = it.chatId)
                        StateManager.setStateByChatId(it.chatId, State.People)

                        //TODO: Add choiceManager with names to select. Need to return buttons. NewChoiceManager(users, select = once)
                        Message.Text(
                            message = "Выберите участников для удаления. Отображаются только те, у кого нет счетов. Чтобы отображался каждый участник, удалите все чеки.",
                            chatId = it.chatId,
                            //TODO: ChoiceManager.GetButtons()
                            buttons = Buttons.from(listOf())
                        )
                    }

                    is Action.Participant.Remove.End -> {
                        ParticipantManager.removeRemoveState(chatId = it.chatId)
                        StateManager.setStateByChatId(it.chatId, State.None)

                        Message.Text(
                            message = "Вы вышли из режима удаления участников.",
                            chatId = it.chatId,
                            //TODO: ChoiceManager.GetButtons()
                            buttons = Buttons.from(listOf("/people"))
                        )
                    }

                    is Action.Participant.Remove.Delete -> {
                        //TODO: think about return value
                        //ChoiceManager.ButtonPressed(it.command)

                        Message.Text(
                            message = "Не реализовано совсем",
                            chatId = it.chatId,
                            buttons = Buttons.from(listOf())
                        )
                    }

                    else -> throw IllegalArgumentException("Такой Action не обрабатывается этой фичей")
                }
            }
    }

}