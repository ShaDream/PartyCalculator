package feature

import action.Action
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import message.Buttons
import message.Message
import java.lang.IllegalArgumentException

class MainFeature : IFeature {
    override fun bind(actions: Observable<Action>): Observable<Message> {
        return actions.observeOn(Schedulers.computation())
            .filter { it is Action.Main }
            .map { action ->
                when (action) {
                    is Action.Main.Start ->
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
                    is Action.Main.Help ->
                        Message.Text(
                            message = "Это обработчик для команды /help",
                            chatId = action.chatId,
                            buttons = Buttons.from(listOf()) // Кнопки не меняем
                        )
                    is Action.Main.People ->
                        Message.Text(
                            message = "Доступные методы для взаимодействия с участниками",
                            chatId = action.chatId,
                            buttons = Buttons.from(listOf("/createPeople", "/deletePeople", "/listPeople", "/start"))
                        )
                    is Action.Main.Group ->
                        Message.Text(
                            message = "Доступные методы для взаимодействия с группами",
                            chatId = action.chatId,
                            buttons = Buttons.from(
                                listOf(
                                    "/createGroup",
                                    "/deleteGroup",
                                    "/listGroup",
                                    "/start",
                                )
                            )
                        )
                    is Action.Main.Receipt ->
                        Message.Text(
                            message = "Доступные методы для взаимодействия с чеками",
                            chatId = action.chatId,
                            buttons = Buttons.from(
                                listOf(
                                    "/addReceipt",
                                    "/removeReceipt",
                                    "/listReceipts",
                                    "/calculateReceipts",
                                    "/start",
                                )
                            )
                        )
                    else -> throw IllegalArgumentException("Такой Action не обрабатывается этой фичей")
                }
            }
    }
}