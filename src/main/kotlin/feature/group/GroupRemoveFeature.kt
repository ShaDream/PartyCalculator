package feature.group

import action.Action
import feature.IFeature
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import manager.ChoiceManager
import manager.GroupManager
import message.Buttons
import message.Message
import repository.*
import state.State
import state.StateManager
import java.util.*

class RemoveGroupFeature(private val groupRepo: GroupRepo): IFeature {

    private fun getGroupButtons(groups: ChoiceManager<Group>): List<List<String>> {
        return listOf(
            groups.getButtons({ it.name }, "⬅️", "➡️"),
            listOf("/delete", "/end")
        )
    }

    override fun bind(actions: Observable<Action>): Observable<Message> {
        return actions.observeOn(Schedulers.computation())
            .filter { it is Action.Group.Remove }
            .map {
                when (it) {
                    is Action.Group.Remove.Start -> {
                        if (GroupManager.hasRemoveState(it.chatId)) {
                            Message.Text(
                                message = "Вы уже в режиме для удаления групп.",
                                chatId = it.chatId,
                                buttons = Buttons.from(listOf())
                            )
                        } else {
                            GroupManager.addRemoveState(it.chatId)
                            StateManager.setStateByChatId(it.chatId, State.Group)
                            println(it.chatId)
                            if (!GroupManager.hasChoiceManager(it.chatId)) {
                                val groups = groupRepo.getGroups(it.chatId)
                                GroupManager.createChoiceManager(it.chatId, groups)
                            }

                            val choiceManager = GroupManager.getChoiceManager(it.chatId)

                            Message.Text(
                                message = "Выберите группы для удаления.",
                                chatId = it.chatId,
                                buttons = Buttons.from(getGroupButtons(choiceManager))
                            )
                        }
                    }

                    is Action.Group.Remove.End -> {
                        GroupManager.removeChoiceManager(chatId = it.chatId)
                        GroupManager.removeRemoveState(chatId = it.chatId)
                        StateManager.setStateByChatId(it.chatId, State.None)

                        Message.Text(
                            message = "Вы вышли из режима удаления групп.",
                            chatId = it.chatId,
                            buttons = Buttons.from(
                                listOf(listOf("/group"))
                            )
                        )
                    }

                    is Action.Group.Remove.Delete -> {
                        val choiceManager = GroupManager.getChoiceManager(it.chatId)
                        val selected = choiceManager.getSelected()

                        if (selected.isEmpty()) {
                            Message.Text(
                                message = "Вы ничего не выбрали.",
                                chatId = it.chatId,
                                buttons = Buttons.from(listOf())
                            )
                        } else {
                            selected.map {group -> groupRepo.removeGroup(group.id) }
                            GroupManager.removeChoiceManager(it.chatId)
                            GroupManager.removeRemoveState(chatId = it.chatId)
                            StateManager.setStateByChatId(it.chatId, State.None)

                            Message.Text(
                                message = "Вы удалили группы: " +
                                        " ${selected.joinToString(separator = ", ") { group -> group.id.id.toString() }}.",
                                chatId = it.chatId,
                                buttons = Buttons.from(listOf(listOf("/group"))),
                            )
                        }
                    }

                    is Action.Group.Remove.Next -> {
                        val choiceManager = GroupManager.getChoiceManager(it.chatId)
                        choiceManager.nextPage()

                        Message.Text(
                            message = "Следующая страница.",
                            chatId = it.chatId,
                            buttons = Buttons.from(getGroupButtons(choiceManager))
                        )
                    }

                    is Action.Group.Remove.Previous -> {
                        val choiceManager = GroupManager.getChoiceManager(it.chatId)
                        choiceManager.previousPage()

                        Message.Text(
                            message = "Предыдущая страница.",
                            chatId = it.chatId,
                            buttons = Buttons.from(getGroupButtons(choiceManager))
                        )
                    }

                    is Action.Group.Remove.Choice -> {
                        println(it.chatId)
                        val chatId = it.chatId
                        val rawName =
                            if (it.message.contains("✅")) it.message.dropLast(2)
                            else it.message
                        println(rawName)
                        val choiceManager = GroupManager.getChoiceManager(chatId)

                        val group =
                            Optional.ofNullable(rawName).flatMap {
                                Optional.ofNullable(groupRepo.getGroups(chatId).find {group -> group.name == it })
                            }


                        if (group.isPresent) {
                            choiceManager.toggle(group.get())

                            Message.Text(
                                message = "Вы нажали на ${group.get().name}.",
                                chatId = it.chatId,
                                buttons = Buttons.from(getGroupButtons(choiceManager))
                            )
                        } else {
                            Message.Text(
                                message = "Такой группы не существует.",
                                chatId = it.chatId,
                                buttons = Buttons.from(getGroupButtons(choiceManager))
                            )
                        }
                    }

                    else -> throw IllegalArgumentException("Такой Action не обрабатывается этой фичей.")
                }
            }
    }

}