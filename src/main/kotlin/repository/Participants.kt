package repository

import database.ParticipantsTable
import database.ReceiptTransactionsTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

class Participants(private val database: Database) : ParticipantsRepo {
    override fun createUser(name: String, chatId: Long): UserId {
        return transaction(database) {
            val id = ParticipantsTable.insert {
                it[ParticipantsTable.chatId] = chatId
                it[ParticipantsTable.name] = name
            }[ParticipantsTable.id]

            UserId(id.value)
        }
    }

    override fun hasUser(name: String, chatId: Long): Boolean {
        val hasUser = transaction(database) {
            ParticipantsTable.select { (ParticipantsTable.chatId eq chatId) and (ParticipantsTable.name eq name) }
                .count()
        }

        return hasUser > 0
    }

    override fun getUsersByChatId(chatId: Long): List<User> {
        return transaction(database) {
            ParticipantsTable.select { ParticipantsTable.chatId eq chatId }.map {
                User(
                    id = UserId(it[ParticipantsTable.id].value),
                    chatId = it[ParticipantsTable.chatId],
                    name = it[ParticipantsTable.name]
                )
            }
        }
    }

    override fun removeUser(id: UserId): Boolean {
        return transaction(database) {
            val deleted = ParticipantsTable.deleteWhere { ParticipantsTable.id eq id.id }

            deleted > 0
        }
    }

    override fun getUserStat(id: UserId): UserReceipt {
        return transaction(database) {
            val user = ParticipantsTable.select { ParticipantsTable.id eq id.id }.map {
                User(
                    id = UserId(it[ParticipantsTable.id].value),
                    chatId = it[ParticipantsTable.chatId],
                    name = it[ParticipantsTable.name]
                )
            }.first()

            var owes = 0f
            var spend = 0f

            ReceiptTransactionsTable.select {
                (ReceiptTransactionsTable.toParticipant eq id.id) or (ReceiptTransactionsTable.fromParticipant eq id.id)
            }.forEach {
                val to = it[ReceiptTransactionsTable.toParticipant]
                val amount = it[ReceiptTransactionsTable.amount]

                if (to == id.id) {
                    spend -= amount
                } else {
                    owes += amount
                }
            }

            UserReceipt(
                user = user,
                owes = owes,
                spend = spend,
            )
        }
    }
}