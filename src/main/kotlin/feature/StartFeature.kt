package feature

import action.Action
import io.reactivex.rxjava3.core.Observable
import message.Message
import io.reactivex.rxjava3.schedulers.Schedulers
import java.lang.IllegalArgumentException


class StartFeature : IFeature {
    override fun bind(actions: Observable<Action>): Observable<Message> {
        return actions.observeOn(Schedulers.computation())
            .filter { it is Action.Start }
            .map { action ->
                when (action) {
                    is Action.Start -> Message.Text("Привет, это стартовый обработчик", action.chatId)
                    else -> throw IllegalArgumentException("Такой Action не обрабатывается этой фичей")
                }
            }
    }

}