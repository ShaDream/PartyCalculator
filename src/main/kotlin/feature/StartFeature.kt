package feature

import action.Action
import io.reactivex.rxjava3.core.Observable
import message.Message
import io.reactivex.rxjava3.schedulers.Schedulers
import message.Buttons
import java.lang.IllegalArgumentException


class StartFeature : IFeature {
    override fun bind(actions: Observable<Action>): Observable<Message> {
        return actions.observeOn(Schedulers.computation())
            .filter { it is Action.Start }
            .map { action ->
                when (action) {
                    is Action.Start ->
                        Message.Text(
                            message = "Привет, это стартовый обработчик",
                            chatId = action.chatId,
                            buttons = Buttons.from(
                                listOf(
                                    "/start", "/help", "/people",
                                    "/receipt", "/group"
                                )
                            )
                        )
                    else -> throw IllegalArgumentException("Такой Action не обрабатывается этой фичей")
                }
            }
    }

}