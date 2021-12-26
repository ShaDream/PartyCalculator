package feature.receipt

import action.Action
import feature.IFeature
import helper.Calculator
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.*
import message.Buttons
import message.Message
import repository.*

class ReceiptCalculateFeature(private val receiptRepo: ReceiptRepo, private val participantsRepo: ParticipantsRepo) :
    IFeature {
    companion object {
        fun transfersToString(transfers: List<Transfer>): String {
            return transfers.joinToString(separator = "\n") {
                "Пользователь ${it.from.name} " +
                        "переводит ${it.to.name} " +
                        "${it.amount}"
            }
        }

        fun makeDeltaArray(users: List<UserReceipt>): IntArray {
            val arr = users.map {u -> (u.spend+u.owes).toInt()}.toIntArray()
            if (arr.isNotEmpty())
                arr[0] -= arr.sum()
            return arr
        }
    }


    override fun bind(actions: Observable<Action>): Observable<Message> {
        return actions
            .observeOn(Schedulers.computation())
            .mapOptional {
                when (it) {
                    is Action.Receipt.Calculate -> Optional.of(it)
                    else -> Optional.empty()
                }
            }
            .map {
                val participants = participantsRepo.getUsersByChatId(it.chatId).map { p ->
                    participantsRepo.getUserStat(p.id)
                }
                val receipts = receiptRepo.getReceipts(it.chatId)
                val transfers = Calculator(makeDeltaArray(participants)).getTransactions().map {
                    tr -> Transfer(
                        participants[tr.from].user,
                        participants[tr.to].user,
                        tr.amount
                    )
                }
                if (receipts.isEmpty()) {
                    Message.Text(
                        message = "Список покупок пуст.",
                        chatId = it.chatId,
                        buttons = Buttons.from(listOf()),
                    )
                } else {
                    Message.Text(
                        message = "Для чеков:\n\n" +
                                ReceiptListFeature.receiptsToString(
                                    participantsRepo,
                                    receipts
                                ) + "\n\nПредлагаемые переводы:\n\n" +
                                transfersToString(transfers),
                        chatId = it.chatId,
                        buttons = Buttons.from(listOf()),
                    )
                }
            }
        }
    }