package feature.receipt

import action.Action
import feature.IFeature
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import message.Buttons
import message.Message
import repository.*
import java.lang.IllegalArgumentException

class ReceiptListFeature(private val receiptRepo: ReceiptRepo, private val participantsRepo: ParticipantsRepo) :
    IFeature {

    private fun receiptsToString(receipts: List<Receipt>): String {
        return receipts.joinToString(separator = "\n\n") {
            "Номер чека: ${it.id.id}\n" +
                    "Сумма: ${it.amount}\n" +
                    "Кто совершил покупку: ${getUserNameById(it.from)}\n" +
                    "Участники совместной покупки: ${it.to.joinToString(separator = ", ") { id -> getUserNameById(id) }}\n"
        }
    }

    private fun getUserNameById(userId: UserId): String {
        return participantsRepo.getUserStat(userId).user.name
    }

    override fun bind(actions: Observable<Action>): Observable<Message> {
        return actions.observeOn(Schedulers.computation())
            .filter { it is Action.Receipt.List }
            .map {
                when (it) {
                    is Action.Receipt.List -> {
                        val receipts = receiptRepo.getReceipts(it.chatId)

                        if (receipts.isEmpty()) {
                            Message.Text(
                                message = "Список чеков пуст.",
                                chatId = it.chatId,
                                buttons = Buttons.from(listOf()),
                            )
                        } else {
                            Message.Text(
                                message = "Список чеков:\n\n" + receiptsToString(receipts),
                                chatId = it.chatId,
                                buttons = Buttons.from(listOf()),
                            )
                        }
                    }
                    else -> throw IllegalArgumentException("Такой Action не обрабатывается этой фичей")
                }
            }
    }
}