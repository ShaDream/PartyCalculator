package repository

interface ParticipantsRepo {
    fun createUser(name: String, chatId: Long): UserId
    fun hasUser(name: String, chatId: Long): Boolean
    fun getUsersByChatId(chatId: Long): List<User>
    fun removeUser(id: UserId): Boolean
    fun getUserStat(id: UserId): UserReceipt
}

data class UserId(val id: Long)
data class User(val id: UserId, val name: String, val chatId: Long)

/*
 * Инфо о том сколько пользователь потратил, и сколько должен.
 * Это необходимо для расчёта кому сколько скидывать
 */
data class UserReceipt(val user: User, val owes: Float, val spend: Float)

data class ReceiptId(val id: Long)

data class Receipt(
    val id: ReceiptId,
    val from: UserId,
    val to: List<UserId>,
    val amount: Float
)

data class Transfer(val from: User, val to: User, val amount: Int)

interface ReceiptRepo {
    fun addReceipt(chatId: Long, from: UserId, to: List<UserId>, fullAmount: Float): Receipt
    fun removeReceipt(id: ReceiptId): Boolean
    fun getReceipt(id: ReceiptId): Receipt
    fun getReceipts(chatId: Long): List<Receipt>
}

data class GroupId(val id: Long)

data class Group(
    val id: GroupId,
    val name: String,
    val users: List<UserId>
)

interface GroupRepo {
    fun addGroup(chatId: Long, name: String, users: List<UserId>): GroupId
    fun addUsersToGroup(chatId: Long, name: String, users: List<UserId>): Boolean
    fun removeUsersFromGroup(chatId: Long, name: String, users: List<UserId>): Boolean
    fun removeGroup(id: GroupId): Boolean
    fun getGroup(chatId: Long, name: String): Group
    fun getGroups(chatId: Long): List<Group>
    fun hasGroup(chatId: Long, name: String): Boolean
}

