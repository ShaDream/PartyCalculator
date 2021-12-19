package repository

import database.GroupMemberTable
import database.GroupTable
import database.ParticipantsTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

class Groups(val database: Database) : GroupRepo {
    override fun addGroup(chatId: Long, name: String, users: List<UserId>): GroupId {
        return transaction(database) {
            val groupId = GroupTable.insert {
                it[GroupTable.name] = name
                it[GroupTable.chatId] = chatId
            }[GroupTable.id]

            users.forEach { id ->
                GroupMemberTable.insert {
                    it[GroupMemberTable.groupId] = groupId.value
                    it[GroupMemberTable.participantId] = id.id
                }
            }

            GroupId(groupId.value)
        }
    }

    override fun removeGroup(id: GroupId): Boolean {
        return transaction(database) {
            val has = GroupTable.select { GroupTable.id eq id.id }.count() > 0L

            if (!has) {
                return@transaction false
            }

            GroupMemberTable.deleteWhere { GroupMemberTable.groupId eq id.id }
            GroupTable.deleteWhere { GroupTable.id eq id.id } > 0
        }
    }

    override fun getGroup(chatId: Long, name: String): Group {
        return transaction(database) {
            val group = GroupTable.select { (GroupTable.name eq name) and (GroupTable.chatId eq chatId)}.first()
            val id = group[GroupTable.id].value
            val users = GroupMemberTable.select { GroupMemberTable.groupId eq id}
                .map {
                    UserId(it[GroupMemberTable.participantId])
                }

            Group(
                id = GroupId(id),
                name = group[GroupTable.name],
                users = users
            )
        }
    }

    override fun hasGroup(chatId: Long, name: String): Boolean {
        val hasUser = transaction(database) {
            GroupTable.select { (GroupTable.chatId eq chatId) and (GroupTable.name eq name) }
                .count()
        }

        return hasUser > 0
    }

    override fun getGroups(chatId: Long): List<Group> {
        return transaction(database) {
            GroupTable.select{GroupTable.chatId eq chatId}.map {
                val groupName = it[GroupTable.name]
                getGroup(chatId, groupName)
            }
        }
    }
}