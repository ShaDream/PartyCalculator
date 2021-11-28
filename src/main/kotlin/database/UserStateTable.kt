package database

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.*
import repository.UserStateRepositoryImpl
import state.State

object UserStateTable : LongIdTable(name = "user_state") {
    val chatId = long("chat_id").uniqueIndex()
    val state = varchar("state", 20)
}

fun main() {

    val database = Database.connect(
        url = "jdbc:postgresql://localhost:5432/postgres",
        driver = "org.postgresql.Driver",
        user = "postgres",
        password = "password"
    )


    val userStateRepository = UserStateRepositoryImpl(database)

    println(userStateRepository.getState(1L))

    userStateRepository.setState(1L, State.Receipt)

    println(userStateRepository.getState(1L))

}