package database

import org.jetbrains.exposed.dao.id.LongIdTable

object GroupTable : LongIdTable(name = "group") {
    val chatId = long("chat_id")
    val name = varchar("name", 100)
}