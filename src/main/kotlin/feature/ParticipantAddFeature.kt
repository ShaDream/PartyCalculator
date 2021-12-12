package feature

import action.Action
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import manager.ParticipantManager
import message.Buttons
import message.Message
import repository.ParticipantsRepo
import state.State
import state.StateManager
import java.lang.IllegalArgumentException

class ParticipantAddFeature(private val participantsRepo: ParticipantsRepo) : IFeature {
    override fun bind(actions: Observable<Action>): Observable<Message> {
        return actions.observeOn(Schedulers.computation())
            .filter { it is Action.Participant.Add }
            .map { action ->
                when (action) {
                    is Action.Participant.Add.Start -> {
                        val chatId = action.chatId
                        if (ParticipantManager.hasInParticipantState(chatId)) {
                            Message.Text(
                                message = "Вы уже в режиме добавление участников. Введите имя нового участника:",
                                chatId = chatId,
                                buttons = Buttons.from(listOf("/end")),
                            )
                        } else {
                            ParticipantManager.addToParticipantState(chatId)
                            StateManager.setStateByChatId(chatId, State.People)

                            Message.Text(
                                message = "Включен режим добавление участников. Введите имя нового участника:",
                                chatId = chatId,
                                buttons = Buttons.from(listOf("/end")),
                            )
                        }
                    }

                    is Action.Participant.Add.End -> {
                        val chatId = action.chatId
                        if (ParticipantManager.hasInParticipantState(chatId)) {
                            ParticipantManager.removeFromParticipantState(chatId)
                            StateManager.setStateByChatId(chatId, State.None)

                            Message.Text(
                                message = "Вы вышли из режима добавления участников:",
                                chatId = chatId,
                                buttons = Buttons.from(listOf("/people")),
                            )
                        } else {
                            Message.Text(
                                message = "Вы не находитесь в режиме добавления участников:",
                                chatId = chatId,
                                buttons = Buttons.from(listOf()),
                            )
                        }
                    }

                    is Action.Participant.Add.New -> {
                        val chatId = action.chatId
                        val message = action.message
                        if (!ParticipantManager.hasInParticipantState(chatId)) {
                            return@map Message.Text(
                                message = "Вы не находитесь в режиме добавления участников:",
                                chatId = chatId,
                                buttons = Buttons.from(listOf()),
                            )
                        }
                        if (participantsRepo.hasUser(message, chatId)){
                            return@map Message.Text(
                                message = "Такой участник уже есть",
                                chatId = chatId,
                                buttons = Buttons.from(listOf()),
                            )
                        }

                        participantsRepo.createUser(message, chatId)

                        Message.Text(
                            message = "Вы создали пользователя: $message",
                            chatId = chatId,
                            buttons = Buttons.from(listOf()),
                        )
                    }

                    else -> throw IllegalArgumentException("Такой Action не обрабатывается этой фичей")
                }

            }
    }
}