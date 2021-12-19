package feature.participant

import action.Action
import action.ParticipantActionManager
import feature.IFeature
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import manager.ChoiceManager
import manager.ParticipantManager
import message.Buttons
import message.Message
import repository.Group
import repository.ParticipantsRepo
import repository.User
import state.State
import state.StateManager
import java.lang.IllegalArgumentException
import java.util.*

class ParticipantsDeleteFeature(private val participantsRepo: ParticipantsRepo) : IFeature {

    fun getParticipantsButtons(participants: ChoiceManager<User>): List<List<String>> {
        return listOf(
            participants.getButtons({ it.name }, "⬅️", "➡️"),
            listOf("/delete", "/end")
        )
    }

    override fun bind(actions: Observable<Action>): Observable<Message> {
        return actions.observeOn(Schedulers.computation())
            .filter { it is Action.Participant.Remove }
            .map { it ->
                when (it) {
                    is Action.Participant.Remove.Start -> {
                        if (ParticipantManager.hasRemoveState(it.chatId)) {
                            Message.Text(
                                message = "Вы уже в режиме для удаления.",
                                chatId = it.chatId,
                                buttons = Buttons.from(listOf())
                            )
                        } else {
                            ParticipantManager.addRemoveState(chatId = it.chatId)
                            StateManager.setStateByChatId(it.chatId, State.People)

                            if (!ParticipantManager.hasChoiceManager(it.chatId)) {
                                val users = participantsRepo.getUsersByChatId(it.chatId)
                                ParticipantManager.createChoiceManager(it.chatId, users)
                            }

                            val choiceManager = ParticipantManager.getChoiceManager(it.chatId)

                            Message.Text(
                                message = "Выберите участников для удаления.\n\n" +
                                        "Отображаются только те, у кого нет счетов.\n\n" +
                                        "Чтобы отображался каждый участник, удалите все чеки",
                                chatId = it.chatId,
                                buttons = Buttons.from(getParticipantsButtons(choiceManager))
                            )
                        }
                    }

                    is Action.Participant.Remove.End -> {
                        if (!ParticipantManager.hasRemoveState(it.chatId)) {
                            Message.Text(
                                message = "Вы не находитесь в режиме удаления участников.",
                                chatId = it.chatId,
                                buttons = Buttons.from(listOf())
                            )
                        } else {
                            ParticipantManager.removeChoiceManager(chatId = it.chatId)
                            ParticipantManager.removeRemoveState(chatId = it.chatId)
                            StateManager.setStateByChatId(it.chatId, State.None)

                            Message.Text(
                                message = "Вы вышли из режима удаления участников.",
                                chatId = it.chatId,
                                buttons = Buttons.from(
                                    listOf(listOf("/people"))
                                )
                            )
                        }
                    }

                    is Action.Participant.Remove.Delete -> {
                        if (!ParticipantManager.hasRemoveState(it.chatId)) {
                            Message.Text(
                                message = "Вы не находитесь в режиме удаления участников.",
                                chatId = it.chatId,
                                buttons = Buttons.from(listOf())
                            )
                        } else {
                            val choiceManager = ParticipantManager.getChoiceManager(it.chatId)
                            val selected = choiceManager.getSelected()

                            if (selected.isEmpty()) {
                                Message.Text(
                                    message = "Вы никого не выбрали.",
                                    chatId = it.chatId,
                                    buttons = Buttons.from(listOf())
                                )
                            } else {
                                selected.map { participantsRepo.removeUser(it.id) }
                                ParticipantManager.removeChoiceManager(chatId = it.chatId)
                                ParticipantManager.removeRemoveState(chatId = it.chatId)
                                StateManager.setStateByChatId(it.chatId, State.None)
                                Message.Text(
                                    message = "Пользовалети ${selected.joinToString(separator = ", ") { it.name }} удалены.",
                                    chatId = it.chatId,
                                    buttons = Buttons.from(
                                        listOf(listOf("/people"))
                                    )
                                )
                            }
                        }
                    }

                    is Action.Participant.Remove.Next -> {
                        if (!ParticipantManager.hasRemoveState(it.chatId)) {
                            Message.Text(
                                message = "Вы не находитесь в режиме удаления участников.",
                                chatId = it.chatId,
                                buttons = Buttons.from(listOf())
                            )
                        } else {
                            val choiceManager = ParticipantManager.getChoiceManager(it.chatId)
                            choiceManager.nextPage()

                            Message.Text(
                                message = "Следующая страница.",
                                chatId = it.chatId,
                                buttons = Buttons.from(getParticipantsButtons(choiceManager))
                            )
                        }

                    }

                    is Action.Participant.Remove.Previous -> {
                        if (!ParticipantManager.hasRemoveState(it.chatId)) {
                            Message.Text(
                                message = "Вы не находитесь в режиме удаления участников.",
                                chatId = it.chatId,
                                buttons = Buttons.from(listOf())
                            )
                        } else {
                            val choiceManager = ParticipantManager.getChoiceManager(it.chatId)
                            choiceManager.previousPage()

                            Message.Text(
                                message = "Предыдущая страница.",
                                chatId = it.chatId,
                                buttons = Buttons.from(getParticipantsButtons(choiceManager))
                            )
                        }
                    }

                    is Action.Participant.Remove.Choice -> {
                        if (!ParticipantManager.hasRemoveState(it.chatId)) {
                            Message.Text(
                                message = "Вы не находитесь в режиме удаления участников.",
                                chatId = it.chatId,
                                buttons = Buttons.from(listOf())
                            )
                        } else {
                            val chatId = it.chatId
                            val userName =
                                if (it.message.contains("✅")) it.message.dropLast(2)
                                else it.message

                            val choiceManager = ParticipantManager.getChoiceManager(chatId)

                            val user =
                                Optional.ofNullable(
                                    participantsRepo.getUsersByChatId(chatId).find { it.name == userName })

                            if (user.isPresent) {
                                choiceManager.toggle(user.get())

                                Message.Text(
                                    message = "Вы нажали на ${user.get().name}.",
                                    chatId = it.chatId,
                                    buttons = Buttons.from(getParticipantsButtons(choiceManager))
                                )
                            } else {
                                Message.Text(
                                    message = "Такого пользователя не существует.",
                                    chatId = it.chatId,
                                    buttons = Buttons.from(getParticipantsButtons(choiceManager))
                                )
                            }
                        }
                    }

                    else -> throw IllegalArgumentException("Такой Action не обрабатывается этой фичей.")
                }
            }
    }

}