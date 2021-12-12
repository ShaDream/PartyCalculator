package database

import org.jetbrains.exposed.dao.id.LongIdTable

object ReceiptsTable : LongIdTable(name = "receipts") {
    val chatId = long("chat_id")
    val fullAmount = float("full_amount")
}