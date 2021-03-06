package feature.participant

import action.Action
import feature.IFeature
import helper.NameChecker
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import manager.ParticipantManager
import message.Buttons
import message.Message
import repository.GroupRepo
import repository.ParticipantsRepo
import state.State
import state.StateManager
import java.lang.IllegalArgumentException

class ParticipantAddFeature(private val participantsRepo: ParticipantsRepo, private val groupRepo: GroupRepo) :
    IFeature {
    override fun bind(actions: Observable<Action>): Observable<Message> {
        return actions.observeOn(Schedulers.computation())
            .filter { it is Action.Participant.Add }
            .map { action ->
                when (action) {
                    is Action.Participant.Add.Start -> {
                        val chatId = action.chatId
                        if (ParticipantManager.hasAddState(chatId)) {
                            Message.Text(
                                message = "Вы уже в режиме добавление участников. Введите имя нового участника.",
                                chatId = chatId,
                                buttons = Buttons.from(
                                    listOf(listOf("/end"))
                                ),
                            )
                        } else {
                            ParticipantManager.addAddState(chatId)
                            StateManager.setStateByChatId(chatId, State.People)

                            Message.Text(
                                message = "Включен режим добавление участников.\n\n" +
                                        "Введите имя нового участника:",
                                chatId = chatId,
                                buttons = Buttons.from(
                                    listOf(listOf("/end"))
                                ),
                            )
                        }
                    }

                    is Action.Participant.Add.End -> {
                        val chatId = action.chatId
                        if (ParticipantManager.hasAddState(chatId)) {
                            ParticipantManager.removeAddState(chatId)
                            StateManager.setStateByChatId(chatId, State.None)

                            Message.Text(
                                message = "Вы вышли из режима добавления участников.",
                                chatId = chatId,
                                buttons = Buttons.from(
                                    listOf(listOf("/people"))
                                ),
                            )
                        } else {
                            Message.Text(
                                message = "Вы не находитесь в режиме добавления участников.",
                                chatId = chatId,
                                buttons = Buttons.from(listOf()),
                            )
                        }
                    }

                    is Action.Participant.Add.New -> {
                        val chatId = action.chatId
                        val message = action.message
                        if (!ParticipantManager.hasAddState(chatId)) {
                            return@map Message.Text(
                                message = "Вы не находитесь в режиме добавления участников.",
                                chatId = chatId,
                                buttons = Buttons.from(listOf()),
                            )
                        }
                        if (participantsRepo.hasUser(message, chatId)) {
                            return@map Message.Text(
                                message = "Такой участник уже есть.",
                                chatId = chatId,
                                buttons = Buttons.from(listOf()),
                            )
                        }

                        if (groupRepo.hasGroup(chatId, message)) {
                            return@map Message.Text(
                                message = "Такое имя используется группой",
                                chatId = chatId,
                                buttons = Buttons.from(listOf()),
                            )
                        }

                        if (!NameChecker.isNameValid(message)) {
                            return@map Message.Text(
                                message = "Имя не валидно(состоит только из цифр или содержит запрещенные символы(\"/\" для примера))",
                                chatId = chatId,
                                buttons = Buttons.from(listOf()),
                            )
                        }

                        participantsRepo.createUser(message, chatId)

                        Message.Text(
                            message = "Вы создали пользователя: $message.",
                            chatId = chatId,
                            buttons = Buttons.from(listOf()),
                        )
                    }

                    else -> throw IllegalArgumentException("Такой Action не обрабатывается этой фичей")
                }

            }
    }
}