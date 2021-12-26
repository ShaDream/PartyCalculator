package feature.receipt

import action.Action
import feature.IFeature
import helper.NameChecker
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import manager.AddState
import manager.ChatIdAddState
import manager.ChoiceManager
import manager.ReceiptManager
import message.Buttons
import message.Message
import repository.*
import state.State
import state.StateManager

class ReceiptAddFeature(
    val participantsRepo: ParticipantsRepo,
    val groupRepo: GroupRepo,
    val receiptRepo: ReceiptRepo
) : IFeature {
    private fun getChangedGroups(selected: List<User>, groups: List<Group>, selectedGroups: List<Group>): List<Group> {
        val uid = selected.map { it.id }
        val used = groups.map { uid.containsAll(it.users) }
        val changed = mutableListOf<Group>()
        for (i in groups.indices) {
            if (used[i] && !selectedGroups.contains(groups[i]) || !used[i] && selectedGroups.contains(groups[i]))
                changed.add(groups[i])
        }
        return changed
    }

    private fun getParticipantsButtons(
        groups: ChoiceManager<Group>,
        participants: ChoiceManager<User>
    ): List<List<String>> {
        return listOf(
            participants.getButtons({ it.name }, "⬅️", "➡️"),
            groups.getButtons({ it.name }, "◀️", "▶️"),
            listOf("/back", "/discard", "/apply")
        )
    }

    private fun getBuyerButtons(buyer: ChoiceManager<User>): List<List<String>> {
        return listOf(
            buyer.getButtons({ it.name }, "⬅️", "➡️"),
            listOf("/back", "/discard", "/apply")
        )
    }

    private fun handleAmountState(action: Action, state: ChatIdAddState): Message {
        return when (action) {
            is Action.Receipt.Add.Number -> {
                if (action.number <= 0) {
                    return Message.Text(
                        message = "Неправильная сумма",
                        chatId = action.chatId,
                        buttons = Buttons.from(
                            listOf()
                        )
                    )
                }

                state.currentState = AddState.BuyerChoosing
                state.amount = action.number

                Message.Text(
                    message = "Успешно сохранено. Теперь выберете покупателя.",
                    chatId = action.chatId,
                    buttons = Buttons.from(
                        getBuyerButtons(state.buyer)
                    )
                )
            }
            else -> Message.Text(
                message = "Данная команда не доступна в этом состоянии",
                chatId = action.chatId,
                buttons = Buttons.from(
                    listOf()
                )
            )

        }
    }

    private fun handleBuyerState(action: Action, state: ChatIdAddState): Message {
        return when (action) {
            is Action.Receipt.Add.Back -> {
                state.currentState = AddState.AmountAdding

                Message.Text(
                    message = "Введите сумму покупок:",
                    chatId = action.chatId,
                    buttons = Buttons.from(
                        listOf(listOf("/discard"))
                    )
                )
            }

            is Action.Receipt.Add.Next -> {
                state.buyer.nextPage()

                Message.Text(
                    message = "Следующая страница:",
                    chatId = action.chatId,
                    buttons = Buttons.from(
                        getBuyerButtons(state.buyer)
                    )
                )
            }

            is Action.Receipt.Add.Previous -> {
                state.buyer.previousPage()

                Message.Text(
                    message = "Предыдущая страница:",
                    chatId = action.chatId,
                    buttons = Buttons.from(
                        getBuyerButtons(state.buyer)
                    )
                )
            }

            is Action.Receipt.Add.Choice -> {
                val value = NameChecker.getNameWithoutCheckSymbol(action.value)

                val pressedUser = state.people.firstOrNull { it.name == value }
                    ?: return Message.Text(
                        message = "Такого пользователя нет",
                        chatId = action.chatId,
                        buttons = Buttons.from(listOf())
                    )

                val toggle = state.buyer.getSelected()

                if (toggle.contains(pressedUser)) {
                    return Message.Text(
                        message = "Пользователь $value уже выбран как покупатель чека.",
                        chatId = action.chatId,
                        buttons = Buttons.from(getBuyerButtons(state.buyer))
                    )
                }

                (toggle + pressedUser).forEach {
                    state.buyer.toggle(it)
                }

                Message.Text(
                    message = "Вы выбрали $value покупателем чека.",
                    chatId = action.chatId,
                    buttons = Buttons.from(getBuyerButtons(state.buyer))
                )
            }

            is Action.Receipt.Add.Apply -> {
                if (state.buyer.getSelected().count() != 1) {
                    return Message.Text(
                        message = "Нужно выбрать хотя бы одного покупателя",
                        chatId = action.chatId,
                        buttons = Buttons.from(
                            listOf()
                        )
                    )
                }

                state.currentState = AddState.ParticipantsChoosing
                Message.Text(
                    message = "Добавьте участников покупки(на кого её делить):",
                    chatId = action.chatId,
                    buttons = Buttons.from(
                        getParticipantsButtons(state.participentsGroup, state.participents)
                    )
                )
            }

            else -> Message.Text(
                message = "Данная команда не доступна в этом состоянии",
                chatId = action.chatId,
                buttons = Buttons.from(
                    listOf()
                )
            )
        }
    }

    private fun handleParticipantsState(action: Action, state: ChatIdAddState): Message {
        return when (action) {
            is Action.Receipt.Add.Back -> {
                state.currentState = AddState.BuyerChoosing
                Message.Text(
                    message = "Выберете покупателя:",
                    chatId = action.chatId,
                    buttons = Buttons.from(
                        getBuyerButtons(state.buyer)
                    )
                )
            }

            is Action.Receipt.Add.Next -> {
                state.participents.nextPage()

                Message.Text(
                    message = "Следующая страница:",
                    chatId = action.chatId,
                    buttons = Buttons.from(
                        getParticipantsButtons(state.participentsGroup, state.participents)
                    )
                )
            }

            is Action.Receipt.Add.Previous -> {
                state.participents.previousPage()

                Message.Text(
                    message = "Предыдущая страница:",
                    chatId = action.chatId,
                    buttons = Buttons.from(
                        getParticipantsButtons(state.participentsGroup, state.participents)
                    )
                )
            }

            is Action.Receipt.Add.NextGroup -> {
                state.participentsGroup.nextPage()

                Message.Text(
                    message = "Следующая страница:",
                    chatId = action.chatId,
                    buttons = Buttons.from(
                        getParticipantsButtons(state.participentsGroup, state.participents)
                    )
                )
            }

            is Action.Receipt.Add.PreviousGroup -> {
                state.participentsGroup.previousPage()

                Message.Text(
                    message = "Предыдущая страница:",
                    chatId = action.chatId,
                    buttons = Buttons.from(
                        getParticipantsButtons(state.participentsGroup, state.participents)
                    )
                )
            }

            is Action.Receipt.Add.Choice -> {
                val value = NameChecker.getNameWithoutCheckSymbol(action.value)

                val isGroup = state.groups.firstOrNull { it.name == value }
                val isUser = state.people.firstOrNull { it.name == value }

                if (isGroup == null && isUser == null) {
                    return Message.Text(
                        message = "Такого пользователя или группы не существует.",
                        chatId = action.chatId,
                        buttons = Buttons.from(
                            listOf()
                        )
                    )
                }

                if (isGroup != null) {
                    val pressedSelectedGroup = state.participentsGroup.getSelected()
                        .firstOrNull { it.name == isGroup.name } != null

                    // if pressed group is already selected, then unselect all participants of this group
                    if (pressedSelectedGroup) {
                        isGroup.users
                            .map { groupUserId -> state.people.first { it.id == groupUserId } }
                            .forEach { state.participents.toggle(it) }

                    }
                    // if pressed group is not selected, then select all unselected participants of this group
                    else {
                        isGroup.users
                            .map { groupUserId -> state.people.first { it.id == groupUserId } }
                            .filter { !state.participents.getSelected().contains(it) }
                            .forEach { state.participents.toggle(it) }
                    }

                    // recalculate selection for all groups. Return only group that need toggle
                    getChangedGroups(
                        state.participents.getSelected(),
                        state.groups,
                        state.participentsGroup.getSelected()
                    ).forEach {
                        state.participentsGroup.toggle(it)
                    }

                    return Message.Text(
                        message = "Нажали на группу ${value}.",
                        chatId = action.chatId,
                        buttons = Buttons.from(
                            getParticipantsButtons(
                                state.participentsGroup,
                                state.participents
                            )
                        )
                    )
                }

                if (isUser != null) {
                    state.participents.toggle(isUser)
                }

                // recalculate selection for all groups. Return only group that need toggle
                getChangedGroups(
                    state.participents.getSelected(),
                    state.groups,
                    state.participentsGroup.getSelected()
                ).forEach {
                    state.participentsGroup.toggle(it)
                }

                return Message.Text(
                    message = "Нажали на пользователя ${value}.",
                    chatId = action.chatId,
                    buttons = Buttons.from(
                        getParticipantsButtons(
                            state.participentsGroup,
                            state.participents
                        )
                    )
                )
            }

            is Action.Receipt.Add.Apply -> {
                if (state.participents.getSelected().isEmpty()) {
                    return Message.Text(
                        message = "Нужно выбрать хотя бы одного участника",
                        chatId = action.chatId,
                        buttons = Buttons.from(
                            listOf()
                        )
                    )
                }

                val selected = state.buyer.getSelected().first()

                val selectedUsers = state.participents.getSelected().map { it.id }

                receiptRepo.addReceipt(action.chatId, selected.id, selectedUsers, state.amount)

                StateManager.setStateByChatId(action.chatId, State.None)
                ReceiptManager.deleteAddState(action.chatId)
                Message.Text(
                    message = "Чек добавлен",
                    chatId = action.chatId,
                    buttons = Buttons.from(
                        listOf(
                            listOf("/start"),
                            listOf("/receipt")
                        )
                    )
                )
            }

            else -> Message.Text(
                message = "Данная команда не доступна в этом состоянии",
                chatId = action.chatId,
                buttons = Buttons.from(
                    listOf()
                )
            )
        }
    }

    override fun bind(actions: Observable<Action>): Observable<Message> {
        return actions.observeOn(Schedulers.computation())
            .filter { it is Action.Receipt.Add }
            .map {
                when (it) {
                    is Action.Receipt.Add.Start -> {
                        if (ReceiptManager.hasAddState(it.chatId)) {
                            return@map Message.Text(
                                message = "Вы уже находитесь в этом режиме.",
                                chatId = it.chatId,
                                buttons = Buttons.from(
                                    listOf()
                                )
                            )
                        }

                        val peoples = participantsRepo.getUsersByChatId(it.chatId)
                        val groups = groupRepo.getGroups(it.chatId)
                        ReceiptManager.createAddState(it.chatId, groups, peoples)
                        StateManager.setStateByChatId(it.chatId, State.Receipt)

                        Message.Text(
                            message = "Введите сумму покупок:",
                            chatId = it.chatId,
                            buttons = Buttons.from(
                                listOf(listOf("/discard"))
                            )
                        )
                    }

                    is Action.Receipt.Add.Discard -> {
                        StateManager.setStateByChatId(it.chatId, State.None)
                        ReceiptManager.deleteAddState(it.chatId)

                        Message.Text(
                            message = "Вы вышли из добавления чека.",
                            chatId = it.chatId,
                            buttons = Buttons.from(
                                listOf(
                                    listOf("/start"),
                                    listOf("/receipt")
                                )
                            )
                        )
                    }

                    else -> {
                        val state = ReceiptManager.getAddState(it.chatId)

                        if (!state.isPresent) {
                            return@map Message.Text(
                                message = "Недопустимая команда.",
                                chatId = it.chatId,
                                buttons = Buttons.from(
                                    listOf()
                                )
                            )
                        }

                        val validState = state.get()

                        when (validState.currentState) {
                            AddState.AmountAdding -> handleAmountState(it, validState)
                            AddState.ParticipantsChoosing -> handleParticipantsState(it, validState)
                            AddState.BuyerChoosing -> handleBuyerState(it, validState)
                        }
                    }
                }
            }

    }
}