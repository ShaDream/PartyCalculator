package feature

import action.Action
import helper.CommonButtons.mainMenuButtons
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
                            message = "Привет, этот бот сделает совместные покупки проще.\n\n" +
                                    "Поможем разделить траты между участниками и скажем кто кому и сколько должен перевести.\n\n" +
                                    "Нажмите команду /help для подробностей.",
                            chatId = action.chatId,
                            buttons = Buttons.from(mainMenuButtons())
                        )
                    is Action.Main.Help ->
                        Message.Text(
                            message = "Команды в группе /people позволяют создавать, удалять и смотреть список пользователей\n\n" +
                                    "Команды в группе /receipt позволяют создавать, удалять, смотреть чеки для покупок и также считать кто должен скинуть деньги за них.\n\n" +
                                    "Команды в группе /groups позволяют создавать, редактировать и удалять группы пользователей. " +
                                    "Группы действуют как быстрый выбор участников, которые платят за чек." +
                                    ". Например если у вас есть несколько компаний, и они покупают друг для друга, " +
                                    "то можно завести на каждую компанию группу и когда создаётся чек на эту группу, " +
                                    "просто нажать одну кнопку для добавления всех участников в этот чек. ",
                            chatId = action.chatId,
                            buttons = Buttons.from(listOf()) // Кнопки не меняем
                        )
                    is Action.Main.People ->
                        Message.Text(
                            message = "Доступные методы для взаимодействия с участниками",
                            chatId = action.chatId,
                            buttons = Buttons.from(
                                listOf(
                                    listOf("/start"),
                                    listOf("/createPeople", "/deletePeople", "/listPeople")
                                )
                            )
                        )
                    is Action.Main.Group ->
                        Message.Text(
                            message = "Доступные методы для взаимодействия с группами",
                            chatId = action.chatId,
                            buttons = Buttons.from(
                                listOf(
                                    listOf("/start"),
                                    listOf(
                                        "/createGroup",
                                        "/editGroups"
                                    ),
                                    listOf(
                                        "/removeGroups",
                                        "/listGroups"
                                    )
                                )
                            )
                        )
                    is Action.Main.Receipt ->
                        Message.Text(
                            message = "Доступные методы для взаимодействия с чеками",
                            chatId = action.chatId,
                            buttons = Buttons.from(
                                listOf(
                                    listOf("/start"),
                                    listOf(
                                        "/addReceipt",
                                        "/removeReceipt"
                                    ),
                                    listOf(
                                        "/listReceipts",
                                        "/calculateReceipts"
                                    )
                                )
                            )
                        )
                    else -> throw IllegalArgumentException("Такой Action не обрабатывается этой фичей")
                }
            }
    }
}