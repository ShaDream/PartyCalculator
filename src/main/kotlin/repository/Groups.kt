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
    override fun addUsersToGroup(chatId: Long, name: String, users: List<UserId>): Boolean {
        return transaction(database) {
            val group = GroupTable.select { (GroupTable.name eq name) and (GroupTable.chatId eq chatId)}.first()
            val groupId = group[GroupTable.id].value

            users.forEach { id ->
                if(GroupMemberTable.select {
                        (GroupMemberTable.participantId eq id.id) and (GroupMemberTable.groupId eq groupId)
                }.count() > 0L)
                    return@transaction false
            }

            users.forEach { id ->
                GroupMemberTable.insert {
                    it[GroupMemberTable.groupId] = groupId
                    it[GroupMemberTable.participantId] = id.id
                }
            }

            return@transaction true
        }
    }

    override fun removeUsersFromGroup(chatId: Long, name: String, users: List<UserId>): Boolean {
        return transaction(database) {
            val group = GroupTable.select {
                (GroupTable.name eq name) and (GroupTable.chatId eq chatId)
            }.first()
            val groupId = group[GroupTable.id].value

            users.forEach { id ->
                GroupMemberTable.deleteWhere {
                    (GroupMemberTable.groupId eq groupId) and (GroupMemberTable.participantId eq id.id)
                }
            }
            return@transaction true
        }
    }


    override fun removeGroup(id: GroupId): Boolean {
        return transaction(database) {
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