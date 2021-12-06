package database

import org.jetbrains.exposed.dao.id.LongIdTable

object GroupMemberTable : LongIdTable(name = "group_member") {
    val groupId = long("group_id")
    val participantId = long("participant_id")
}