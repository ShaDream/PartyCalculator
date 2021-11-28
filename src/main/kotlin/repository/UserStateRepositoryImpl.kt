package repository

import database.UserStateTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import state.State
import java.util.*

class UserStateRepositoryImpl(private val userStateDatabase: Database) : UserStateRepository {

    override fun getState(chatId: Long): Optional<State> {

        val rawState = transaction(userStateDatabase) {
            UserStateTable.select { UserStateTable.chatId eq chatId }.map { it[UserStateTable.state] }.firstOrNull()
        }

        return Optional.ofNullable(rawState).map { State.valueOf(it) }

    }

    override fun setState(chatId: Long, state: State) {

        transaction(userStateDatabase) {

            if (getState(chatId).isPresent) {
                UserStateTable.update({ UserStateTable.chatId eq chatId }) {
                    it[UserStateTable.state] = state.name
                }
            } else {
                UserStateTable.insert {
                    it[UserStateTable.chatId] = chatId
                    it[UserStateTable.state] = state.name
                }
            }
        }
    }

}