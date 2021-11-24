package database

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.random.Random

object Users : LongIdTable() {
    val chatId = long("chat_id").uniqueIndex()
    val state = varchar("state", 10)
}

fun main() {

    Database.connect(
        url = "jdbc:postgresql://localhost:5432/postgres",
        driver = "org.postgresql.Driver",
        user = "postgres",
        password = "password"
    )

    val users = transaction {
        SchemaUtils.create(Users)
        addLogger(Slf4jSqlDebugLogger)

        Users.insert {
            it[chatId] = Random.nextLong()
            it[state] = String(Random.nextBytes(10))
        }

        Users.selectAll().map { it[Users.id] to it[Users.chatId] to it[Users.state] }
    }

    println(users)
}