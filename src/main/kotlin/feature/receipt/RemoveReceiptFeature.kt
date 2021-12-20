package feature.receipt

import action.Action
import action.Action.Receipt.Remove.*
import feature.IFeature
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import manager.ChoiceManager
import manager.ReceiptManager
import message.Buttons
import message.Message
import repository.ParticipantsRepo
import repository.Receipt
import repository.ReceiptRepo
import state.State
import state.StateManager
import java.lang.IllegalArgumentException
import java.util.*

class RemoveReceiptFeature(private val receiptRepo: ReceiptRepo, private val participantsRepo: ParticipantsRepo) :
    IFeature {

    private fun getReceiptsButtons(participants: ChoiceManager<Receipt>): List<List<String>> {
        return listOf(
            participants.getButtons({ it.id.id.toString() }, "⬅️", "➡️"),
            listOf("/delete", "/end")
        )
    }

    override fun bind(actions: Observable<Action>): Observable<Message> {
        return actions
            .observeOn(Schedulers.computation())
            .mapOptional {
                when (it) {
                    is Action.Receipt.Remove -> Optional.of(it)
                    else -> Optional.empty()
                }
            }
            .map {
                when (it) {
                    is Start -> {
                        if (ReceiptManager.hasRemoveState(it.chatId)) {
                            Message.Text(
                                message = "Вы уже в режиме для удаления.",
                                chatId = it.chatId,
                                buttons = Buttons.from(listOf())
                            )
                        } else {
                            ReceiptManager.addRemoveState(chatId = it.chatId)
                            StateManager.setStateByChatId(it.chatId, State.Receipt)

                            if (!ReceiptManager.hasChoiceManager(it.chatId)) {
                                val receipts = receiptRepo.getReceipts(it.chatId)
                                ReceiptManager.createChoiceManager(it.chatId, receipts)
                            }

                            val choiceManager = ReceiptManager.getChoiceManager(it.chatId)

                            Message.Text(
                                message = "Выберите чеки для удаления.\n\n" +
                                        ReceiptListFeature.receiptsToString(
                                            participantsRepo,
                                            choiceManager.getCurrentPage()
                                        ),
                                chatId = it.chatId,
                                buttons = Buttons.from(getReceiptsButtons(choiceManager))
                            )
                        }
                    }

                    is End -> {
                        ReceiptManager.removeChoiceManager(chatId = it.chatId)
                        ReceiptManager.removeRemoveState(chatId = it.chatId)
                        StateManager.setStateByChatId(it.chatId, State.None)

                        Message.Text(
                            message = "Вы вышли из режима удаления чеков.",
                            chatId = it.chatId,
                            buttons = Buttons.from(
                                listOf(listOf("/receipt"))
                            )
                        )
                    }

                    is Delete -> {
                        val choiceManager = ReceiptManager.getChoiceManager(it.chatId)
                        val selected = choiceManager.getSelected()

                        if (selected.isEmpty()) {
                            Message.Text(
                                message = "Вы никого не выбрали.",
                                chatId = it.chatId,
                                buttons = Buttons.from(listOf())
                            )
                        } else {
                            selected.map { receiptRepo.removeReceipt(it.id) }
                            ReceiptManager.removeChoiceManager(chatId = it.chatId)
                            ReceiptManager.removeRemoveState(chatId = it.chatId)
                            StateManager.setStateByChatId(it.chatId, State.None)
                            Message.Text(
                                message = "Вы удалили чеки с номерами: " +
                                        " ${selected.joinToString(separator = ", ") { it.id.id.toString() }}.",
                                chatId = it.chatId,
                                buttons = Buttons.from(
                                    listOf(listOf("/receipt"))
                                )
                            )
                        }
                    }

                    is Next -> {
                        val choiceManager = ReceiptManager.getChoiceManager(it.chatId)
                        choiceManager.nextPage()

                        Message.Text(
                            message = "Следующая страница.\n\n" +
                                    ReceiptListFeature.receiptsToString(
                                        participantsRepo,
                                        choiceManager.getCurrentPage()
                                    ),
                            chatId = it.chatId,
                            buttons = Buttons.from(getReceiptsButtons(choiceManager))
                        )
                    }

                    is Previous -> {
                        val choiceManager = ReceiptManager.getChoiceManager(it.chatId)
                        choiceManager.previousPage()

                        Message.Text(
                            message = "Предыдущая страница.\n\n" +
                                    ReceiptListFeature.receiptsToString(
                                        participantsRepo,
                                        choiceManager.getCurrentPage()
                                    ),
                            chatId = it.chatId,
                            buttons = Buttons.from(getReceiptsButtons(choiceManager))
                        )
                    }

                    is Choice -> {
                        val chatId = it.chatId
                        val rawId =
                            if (it.message.contains("✅")) it.message.dropLast(2)
                            else it.message

                        val choiceManager = ReceiptManager.getChoiceManager(chatId)

                        val id =
                            Optional.ofNullable(rawId.toLongOrNull()).flatMap { id ->
                                Optional.ofNullable(receiptRepo.getReceipts(chatId).find { it.id.id == id })
                            }


                        if (id.isPresent) {
                            choiceManager.toggle(id.get())

                            Message.Text(
                                message = "Вы нажали на ${id.get().id.id}.",
                                chatId = it.chatId,
                                buttons = Buttons.from(getReceiptsButtons(choiceManager))
                            )
                        } else {
                            Message.Text(
                                message = "Такого чека не существует.",
                                chatId = it.chatId,
                                buttons = Buttons.from(getReceiptsButtons(choiceManager))
                            )
                        }
                    }

                    else -> throw IllegalArgumentException("Такой Action не обрабатывается этой фичей.")
                }
            }
    }

}