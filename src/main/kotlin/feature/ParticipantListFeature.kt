package feature

import action.Action
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import message.Buttons
import message.Message
import repository.ParticipantsRepo
import java.lang.IllegalArgumentException

class ParticipantListFeature(private val participantsRepo: ParticipantsRepo) : IFeature {
    override fun bind(actions: Observable<Action>): Observable<Message> {
        return actions.observeOn(Schedulers.computation())
            .filter { it is Action.Participant.List }
            .map { action ->
                when (action) {
                    is Action.Participant.List -> {
                        val participants =
                            participantsRepo.getUsersByChatId(action.chatId).joinToString(separator = ", ") { it.name }

                        Message.Text(
                            message = "Участники:\n$participants",
                            chatId = action.chatId,
                            buttons = Buttons.from(listOf()),
                        )
                    }
                    else -> throw IllegalArgumentException("Такой Action не обрабатывается этой фичей")
                }
            }
    }
}