package feature.group

import action.Action
import feature.IFeature
import helper.CommonButtons.mainGroupButtons
import helper.NameChecker
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import manager.GroupManager
import message.Buttons
import message.Message
import repository.*
import state.State
import state.StateManager

class GroupAddFeature(private val groupRepo: GroupRepo) : IFeature {
    override fun bind(actions: Observable<Action>): Observable<Message> {
        return actions.observeOn(Schedulers.computation())
            .filter { it is Action.Group.Add }
            .map { action ->
                when (action) {
                    is Action.Group.Add.Start -> {
                        if (GroupManager.hasAddState(action.chatId)) {
                            Message.Text(
                                message = "Вы уже в режиме добавления группы.",
                                chatId = action.chatId,
                                buttons = Buttons.from(listOf())
                            )
                        } else {
                            GroupManager.addAddState(action.chatId)
                            StateManager.setStateByChatId(action.chatId, State.Group)

                            Message.Text(
                                message = "Введите имя новой группы.",
                                chatId = action.chatId,
                                buttons = Buttons.from(listOf(listOf("/back")))
                            )
                        }
                    }

                    is Action.Group.Add.New -> {
                        if (!GroupManager.hasAddState(action.chatId)) {
                            return@map Message.Text(
                                message = "Вы не в режиме добавления группы.",
                                chatId = action.chatId,
                                buttons = Buttons.from(listOf())
                            )
                        }
                        if (action.message == "/back") {
                            GroupManager.removeAddState(action.chatId)
                            StateManager.setStateByChatId(action.chatId, State.None)
                            return@map Message.Text(
                                message = "Вы вышли из режима создания группы.",
                                chatId = action.chatId,
                                buttons = Buttons.from(mainGroupButtons())
                            )
                        }
                        if (!NameChecker.isNameValid(action.message)) {
                            return@map Message.Text(
                                message = "Недопустимое имя для группы." +
                                        "Возможно состоит только из цифр или содержит запрещенные символы(\"/\" для примера)",
                                chatId = action.chatId,
                                buttons = Buttons.from(listOf()),
                            )
                        }
                        if (groupRepo.hasGroup(action.chatId, action.message)) {
                            return@map Message.Text(
                                message = "Данная группы уже была добавлена!",
                                chatId = action.chatId,
                                buttons = Buttons.from(listOf()),
                            )
                        }

                        groupRepo.addGroup(action.chatId, action.message, listOf<UserId>())
                        GroupManager.removeAddState(action.chatId)
                        GroupManager.createEditState(action.chatId, action.message)
                        Message.Text(
                            message = "Вы создали группу: ${action.message}. \n" +
                                    "Сейчас Вы находитесь в меню редактирования группы ${action.message}",
                            chatId = action.chatId,
                            buttons = Buttons.from(listOf(listOf("/editMembers", "/deleteGroup", "/end")))
                        )

                    }

                    is Action.Group.Add.Discard -> {
                        if (GroupManager.hasAddState(action.chatId)) {
                            GroupManager.removeAddState(action.chatId)

                            Message.Text(
                                message = "Вы вышли из режима редактирования группы.",
                                chatId = action.chatId,
                                buttons = Buttons.from(mainGroupButtons())
                            )
                        } else {
                            Message.Text(
                                message = "Вы не находитесь в режиме добавления групп:",
                                chatId = action.chatId,
                                buttons = Buttons.from(listOf()),
                            )
                        }
                    }

                    else -> throw IllegalArgumentException("Not implemented Action for this feature")
                }
            }
    }
}