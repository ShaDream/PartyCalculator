package repository

interface ParticipantsRepo {
    fun createUser(name: String, chatId: Long): UserId
    fun hasUser(name: String, chatId: Long): Boolean
    fun getUsersByChatId(chatId: Long): List<User>
    fun removeUser(id: UserId): Boolean
    fun getUserStat(id: UserId): UserReceipt
    fun getAllUserStats(id: UserId): List<UserReceipt>
}

data class UserId(val id: Long)
data class User(val id: UserId, val name: String, val chatId: Long)

/*
 * Инфо о том сколько пользователь потратил, и сколько должен.
 * Это необходимо для расчёта кому сколько скидывать
 */
data class UserReceipt(val user: User, val from: Float, val to: Float)

data class ReceiptId(val id: Long)

data class Receipt(
    val id: ReceiptId,
    val from: UserId,
    val to: List<UserId>,
    val amount: Float
)

interface ReceiptRepo {
    fun addReceipt(from: UserId, to: List<UserId>, fullAmount: Float): Receipt
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
    fun addGroup(name: String, users: List<UserId>): Group
    fun removeGroup(id: GroupId): Boolean
    fun getGroup(id: GroupId): Group
    fun getGroups(chatId: Long): List<Group>
}

