package feature.receipt

import action.Action
import feature.IFeature
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import manager.AddState
import manager.ChoiceManager
import manager.ReceiptManager
import message.Buttons
import message.Message
import repository.*
import state.State
import state.StateManager
import java.lang.IllegalArgumentException

class ReceiptAddFeature(
    val participantsRepo: ParticipantsRepo,
    val groupRepo: GroupRepo,
    val receiptRepo: ReceiptRepo
) : IFeature {
    fun getChangedGroups(selected: List<User>, groups: List<Group>, selectedGroups: List<Group>): List<Group> {
        val uid = selected.map { it.id }
        val used = groups.map { uid.containsAll(it.users) }
        val changed = mutableListOf<Group>()
        for (i in groups.indices) {
            if (used[i] && !selectedGroups.contains(groups[i]) || !used[i] && selectedGroups.contains(groups[i]))
                changed.add(groups[i])
        }
        return changed
    }

    fun getParticipantsButtons(groups: ChoiceManager<Group>, participants: ChoiceManager<User>): List<String> {
        return participants.getButtons { it.name } +
                "/previous" + "/next" +
                groups.getButtons { it.name } +
                "/previousGroup" + "/nextGroup" +
                "/back" + "/discard" + "/apply"
    }

    fun getBuyerButtons(buyer: ChoiceManager<User>): List<String> {
        return buyer.getButtons { it.name } +
                "/previous" + "/next" +
                "/back" + "/discard" + "/apply"
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
                                listOf("/discard")
                            )
                        )
                    }

                    is Action.Receipt.Add.NextGroup -> {
                        if (!ReceiptManager.hasAddState(it.chatId)) {
                            return@map Message.Text(
                                message = "Вы уже находитесь в этом режиме.",
                                chatId = it.chatId,
                                buttons = Buttons.from(
                                    listOf()
                                )
                            )
                        }

                        val state = ReceiptManager.getAddState(it.chatId)!!

                        if (state.currentState != AddState.ParticipantsChoosing) {
                            return@map Message.Text(
                                message = "Нельзя использовать команду.",
                                chatId = it.chatId,
                                buttons = Buttons.from(
                                    listOf()
                                )
                            )
                        }

                        state.participentsGroup.nextPage()
                        Message.Text(
                            message = "Следующая страница группы",
                            chatId = it.chatId,
                            buttons = Buttons.from(
                                getParticipantsButtons(state.participentsGroup, state.participents)
                            )
                        )
                    }

                    is Action.Receipt.Add.PreviousGroup -> {
                        if (!ReceiptManager.hasAddState(it.chatId)) {
                            return@map Message.Text(
                                message = "Вы уже находитесь в этом режиме.",
                                chatId = it.chatId,
                                buttons = Buttons.from(
                                    listOf()
                                )
                            )
                        }

                        val state = ReceiptManager.getAddState(it.chatId)!!

                        if (state.currentState != AddState.ParticipantsChoosing) {
                            return@map Message.Text(
                                message = "Нельзя использовать команду.",
                                chatId = it.chatId,
                                buttons = Buttons.from(
                                    listOf()
                                )
                            )
                        }

                        state.participentsGroup.previousPage()
                        Message.Text(
                            message = "Предыдущая страница группы",
                            chatId = it.chatId,
                            buttons = Buttons.from(
                                getParticipantsButtons(state.participentsGroup, state.participents)
                            )
                        )
                    }

                    is Action.Receipt.Add.Next -> {
                        if (!ReceiptManager.hasAddState(it.chatId)) {
                            return@map Message.Text(
                                message = "Вы уже находитесь в этом режиме.",
                                chatId = it.chatId,
                                buttons = Buttons.from(
                                    listOf()
                                )
                            )
                        }

                        val state = ReceiptManager.getAddState(it.chatId)!!

                        when (state.currentState) {
                            AddState.BuyerChoosing -> {
                                state.buyer.nextPage()
                                Message.Text(
                                    message = "Следующая страница",
                                    chatId = it.chatId,
                                    buttons = Buttons.from(
                                        getBuyerButtons(state.buyer)
                                    )
                                )
                            }
                            AddState.ParticipantsChoosing -> {
                                state.participents.nextPage()

                                Message.Text(
                                    message = "Следующая страница",
                                    chatId = it.chatId,
                                    buttons = Buttons.from(
                                        getParticipantsButtons(state.participentsGroup, state.participents)
                                    )
                                )
                            }
                            else -> Message.Text(
                                message = "Нельзя использовать команду.",
                                chatId = it.chatId,
                                buttons = Buttons.from(
                                    listOf()
                                )
                            )
                        }

                    }

                    is Action.Receipt.Add.Previous -> {
                        if (!ReceiptManager.hasAddState(it.chatId)) {
                            return@map Message.Text(
                                message = "Вы не находитесь в этом режиме.",
                                chatId = it.chatId,
                                buttons = Buttons.from(
                                    listOf()
                                )
                            )
                        }

                        val state = ReceiptManager.getAddState(it.chatId)!!


                        when (state.currentState) {
                            AddState.BuyerChoosing -> {
                                state.buyer.previousPage()

                                Message.Text(
                                    message = "Предыдущая страница",
                                    chatId = it.chatId,
                                    buttons = Buttons.from(
                                        getBuyerButtons(state.buyer)
                                    )
                                )
                            }
                            AddState.ParticipantsChoosing -> {
                                state.participents.previousPage()

                                Message.Text(
                                    message = "предыдущая страница",
                                    chatId = it.chatId,
                                    buttons = Buttons.from(
                                        getParticipantsButtons(state.participentsGroup, state.participents)
                                    )
                                )
                            }
                            else -> Message.Text(
                                message = "Нельзя использовать команду.",
                                chatId = it.chatId,
                                buttons = Buttons.from(
                                    listOf()
                                )
                            )
                        }
                    }

                    is Action.Receipt.Add.Apply -> {
                        if (!ReceiptManager.hasAddState(it.chatId)) {
                            return@map Message.Text(
                                message = "Вы не находитесь в этом режиме.",
                                chatId = it.chatId,
                                buttons = Buttons.from(
                                    listOf()
                                )
                            )
                        }

                        val state = ReceiptManager.getAddState(it.chatId)!!

                        when (state.currentState) {
                            AddState.BuyerChoosing -> {
                                if (state.buyer.getSelected().count() != 1) {
                                    return@map Message.Text(
                                        message = "Нужно выбрать лишь одного покупателя",
                                        chatId = it.chatId,
                                        buttons = Buttons.from(
                                            listOf()
                                        )
                                    )
                                }

                                state.currentState = AddState.ParticipantsChoosing
                                Message.Text(
                                    message = "Добавьте участников покупки(на кого её делить):",
                                    chatId = it.chatId,
                                    buttons = Buttons.from(
                                        getParticipantsButtons(state.participentsGroup, state.participents)
                                    )
                                )
                            }
                            AddState.ParticipantsChoosing -> {
                                if (state.participents.getSelected().isEmpty()) {
                                    return@map Message.Text(
                                        message = "Нужно выбрать хотя бы одного участника",
                                        chatId = it.chatId,
                                        buttons = Buttons.from(
                                            listOf()
                                        )
                                    )
                                }

                                val selected = state.buyer.getSelected().first()

                                val selectedUsers = state.participents.getSelected().map { it.id }

                                receiptRepo.addReceipt(it.chatId, selected.id, selectedUsers, state.amount)

                                StateManager.setStateByChatId(it.chatId, State.None)
                                ReceiptManager.deleteAddState(it.chatId)
                                Message.Text(
                                    message = "Чек добавлен",
                                    chatId = it.chatId,
                                    buttons = Buttons.from(
                                        listOf("/receipt")
                                    )
                                )
                            }

                            else -> Message.Text(
                                message = "Нельзя использовать команду.",
                                chatId = it.chatId,
                                buttons = Buttons.from(
                                    listOf()
                                )
                            )
                        }
                    }

                    is Action.Receipt.Add.Back -> {
                        if (!ReceiptManager.hasAddState(it.chatId)) {
                            return@map Message.Text(
                                message = "Режим неактивен",
                                chatId = it.chatId,
                                buttons = Buttons.from(
                                    listOf()
                                )
                            )
                        }

                        var state = ReceiptManager.getAddState(it.chatId)!!

                        when (state.currentState) {
                            AddState.AmountAdding -> {
                                Message.Text(
                                    message = "Это первый шаг. Нельзя вернуться назад",
                                    chatId = it.chatId,
                                    buttons = Buttons.from(
                                        listOf()
                                    )
                                )
                            }

                            AddState.BuyerChoosing -> {
                                state.currentState = AddState.AmountAdding
                                Message.Text(
                                    message = "Введите сумму покупок:",
                                    chatId = it.chatId,
                                    buttons = Buttons.from(
                                        listOf("/discard")
                                    )
                                )
                            }

                            AddState.ParticipantsChoosing -> {
                                state.currentState = AddState.BuyerChoosing
                                Message.Text(
                                    message = "Выберете покупателя:",
                                    chatId = it.chatId,
                                    buttons = Buttons.from(
                                        getBuyerButtons(state.buyer)
                                    )
                                )
                            }
                        }
                    }

                    is Action.Receipt.Add.Number -> {
                        if (!ReceiptManager.hasAddState(it.chatId)) {
                            return@map Message.Text(
                                message = "Режим неактивен",
                                chatId = it.chatId,
                                buttons = Buttons.from(
                                    listOf()
                                )
                            )
                        }

                        val state = ReceiptManager.getAddState(it.chatId)!!

                        if (state.currentState != AddState.AmountAdding) {
                            return@map Message.Text(
                                message = "Вы не находитесь в режиме добавления суммы",
                                chatId = it.chatId,
                                buttons = Buttons.from(
                                    listOf()
                                )
                            )
                        }

                        if (it.number <= 0) {
                            return@map Message.Text(
                                message = "Неправильная сумма",
                                chatId = it.chatId,
                                buttons = Buttons.from(
                                    listOf()
                                )
                            )
                        }

                        state.currentState = AddState.BuyerChoosing
                        state.amount = it.number

                        Message.Text(
                            message = "Успешно сохранено. Теперь выберете покупателя.",
                            chatId = it.chatId,
                            buttons = Buttons.from(
                                getBuyerButtons(state.buyer)
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
                                listOf("/receipt")
                            )
                        )
                    }

                    is Action.Receipt.Add.Choice -> {
                        if (!ReceiptManager.hasAddState(it.chatId)) {
                            return@map Message.Text(
                                message = "Режим неактивен",
                                chatId = it.chatId,
                                buttons = Buttons.from(
                                    listOf()
                                )
                            )
                        }

                        val state = ReceiptManager.getAddState(it.chatId)!!

                        when (state.currentState) {
                            AddState.BuyerChoosing -> {
                                val value =
                                    if (it.value.contains("✅")) it.value.dropLast(2)
                                    else it.value

                                val pressedUser = state.people.firstOrNull { it.name == value }
                                    ?: return@map Message.Text(
                                        message = "Такого пользователя нет",
                                        chatId = it.chatId,
                                        buttons = Buttons.from(listOf())
                                    )

                                state.buyer.toggle(pressedUser)

                                Message.Text(
                                    message = "Вы нажали на ${value}.",
                                    chatId = it.chatId,
                                    buttons = Buttons.from(getBuyerButtons(state.buyer))
                                )
                            }

                            AddState.ParticipantsChoosing -> {
                                val value =
                                    if (it.value.contains("✅")) it.value.dropLast(2)
                                    else it.value

                                val isGroup = state.groups.firstOrNull { it.name == value }
                                val isUser = state.people.firstOrNull { it.name == value }

                                if (isGroup == null && isUser == null) {
                                    return@map Message.Text(
                                        message = "Такого пользователя или гпуппы не существует.",
                                        chatId = it.chatId,
                                        buttons = Buttons.from(
                                            listOf()
                                        )
                                    )
                                }

                                if (isGroup != null) {
                                    val isSelected = state.participentsGroup.getSelected()
                                        .firstOrNull { it.name == isGroup.name } != null

                                    if (isSelected) {
                                        isGroup.users.forEach { userId ->
                                            val user = state.people.first { it.id == userId }
                                            state.participents.toggle(user)
                                        }
                                    } else {
                                        isGroup.users.filter { userId ->
                                            val user = state.people.first { it.id == userId }
                                            !state.participents.getSelected().contains(user)
                                        }.forEach { userId ->
                                            val user = state.people.first { it.id == userId }
                                            state.participents.toggle(user)
                                        }
                                    }
                                    getChangedGroups(
                                        state.participents.getSelected(),
                                        state.groups,
                                        state.participentsGroup.getSelected()
                                    ).forEach {
                                        state.participentsGroup.toggle(it)
                                    }

                                    return@map Message.Text(
                                        message = "Нажали на группу ${value}.",
                                        chatId = it.chatId,
                                        buttons = Buttons.from(
                                            getParticipantsButtons(
                                                state.participentsGroup,
                                                state.participents
                                            )
                                        )
                                    )
                                }

                                state.participents.toggle(isUser!!)

                                getChangedGroups(
                                    state.participents.getSelected(),
                                    state.groups,
                                    state.participentsGroup.getSelected()
                                ).forEach {
                                    state.participentsGroup.toggle(it)
                                }

                                return@map Message.Text(
                                    message = "Нажали на пользователя ${value}.",
                                    chatId = it.chatId,
                                    buttons = Buttons.from(
                                        getParticipantsButtons(
                                            state.participentsGroup,
                                            state.participents
                                        )
                                    )
                                )
                            }

                            else -> Message.Text(
                                message = "Нельзя использовать команду.",
                                chatId = it.chatId,
                                buttons = Buttons.from(
                                    listOf()
                                )
                            )
                        }

                    }

                    else -> throw IllegalArgumentException("Такой Action не обрабатывается этой фичей")
                }
            }
    }
}