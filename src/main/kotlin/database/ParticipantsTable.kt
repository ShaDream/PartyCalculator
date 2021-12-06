package database

import org.jetbrains.exposed.dao.id.LongIdTable

object ParticipantsTable : LongIdTable(name = "participants") {
    val chatId = long("chat_id")
    val name = varchar("name", 100)
}