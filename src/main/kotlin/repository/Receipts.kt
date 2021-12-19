package repository

import database.ReceiptsTable
import database.ReceiptTransactionsTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

class Receipts(private val database: Database) : ReceiptRepo {
    override fun addReceipt(chatId: Long, from: UserId, to: List<UserId>, fullAmount: Float): Receipt {
        return transaction(database) {
            val receipt = ReceiptsTable.insert {
                it[ReceiptsTable.chatId] = chatId
                it[ReceiptsTable.fullAmount] = fullAmount
            }[ReceiptsTable.id]


            val amount = fullAmount / to.count()

            to.forEach { userId ->
                ReceiptTransactionsTable.insert { data ->
                    data[receiptId] = receipt.value
                    data[toParticipant] = userId.id
                    data[fromParticipant] = from.id
                    data[ReceiptTransactionsTable.amount] = amount
                }
            }

            Receipt(ReceiptId(receipt.value), from, to, fullAmount)
        }
    }

    override fun removeReceipt(id: ReceiptId): Boolean {
        return transaction(database)
        {
            val deletedTransactions =
                ReceiptTransactionsTable.deleteWhere { ReceiptTransactionsTable.receiptId eq id.id }
            val deleted = ReceiptsTable.deleteWhere { ReceiptsTable.id eq id.id }

            deleted > 0 && deletedTransactions > 0
        }
    }

    override fun getReceipt(id: ReceiptId): Receipt {
        return transaction(database)
        {
            val resultQuery =
                ReceiptTransactionsTable.innerJoin(ReceiptsTable, { receiptId },
                    { ReceiptsTable.id })
                    .select { ReceiptTransactionsTable.receiptId eq id.id }//.groupBy { ReceiptTransactionsTable.fromParticipant }

            Receipt(
                id = id,
                from = UserId(resultQuery.map { it[ReceiptTransactionsTable.fromParticipant] }.first()),
                to = resultQuery.map { UserId(it[ReceiptTransactionsTable.toParticipant]) },
                amount = resultQuery.map { it[ReceiptsTable.fullAmount] }.first(),
            )
        }
    }

    override fun getReceipts(chatId: Long): List<Receipt> {
        return transaction(database)
        {

            ReceiptTransactionsTable

            ReceiptTransactionsTable
                .innerJoin(
                    ReceiptsTable,
                    { receiptId },
                    { ReceiptsTable.id }
                )
                .slice(
                    ReceiptTransactionsTable.receiptId,
                    ReceiptTransactionsTable.fromParticipant,
                    ReceiptTransactionsTable.toParticipant,
                    ReceiptsTable.fullAmount
                )
                .select { ReceiptsTable.chatId eq chatId }
                .groupBy { row ->
                    Triple(
                        row[ReceiptTransactionsTable.receiptId],
                        row[ReceiptTransactionsTable.fromParticipant],
                        row[ReceiptsTable.fullAmount]
                    )
                }
                .map { (triple, rows) ->
                    Receipt(
                        id = ReceiptId(triple.first),
                        from = UserId(triple.second),
                        to = rows.map { UserId(it[ReceiptTransactionsTable.toParticipant]) },
                        amount = triple.third
                    )
                }
        }
    }
}