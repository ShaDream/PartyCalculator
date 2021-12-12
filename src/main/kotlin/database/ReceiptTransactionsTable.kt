package database

import org.jetbrains.exposed.dao.id.LongIdTable

object ReceiptTransactionsTable : LongIdTable(name = "receipt_transactions") {
    val receiptId = long("receipt_id")
    val fromParticipant = long("from_participant")
    val toParticipant = long("to_participant")
    val amount = float("amount")
}