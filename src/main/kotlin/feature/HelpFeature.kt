package feature

import action.Action
import io.reactivex.rxjava3.core.Observable
import message.Message
import io.reactivex.rxjava3.schedulers.Schedulers
import message.Buttons
import java.lang.IllegalArgumentException


class HelpFeature : IFeature {
    override fun bind(actions: Observable<Action>): Observable<Message> {
        return actions.observeOn(Schedulers.computation())
            .filter { it is Action.Help }
            .map { action ->
                when (action) {
                    is Action.Help ->
                        Message.Text(
                            message = "Это обработчик для команды /help",
                            chatId = action.chatId,
                            buttons = Buttons.from(listOf()) // Кнопки не меняем
                        )
                    else -> throw IllegalArgumentException("Такой Action не обрабатывается этой фичей")
                }
            }
    }

}