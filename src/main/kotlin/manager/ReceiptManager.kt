package manager

import repository.Group
import repository.Receipt
import repository.User
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import javax.swing.text.html.Option

object ReceiptManager {
    private var addState = ConcurrentHashMap<Long, ChatIdAddState>()
    private var removeState = ConcurrentHashMap.newKeySet<Long>()

    private var choiceManagerMap: ConcurrentHashMap<Long, ChoiceManager<Receipt>> = ConcurrentHashMap()

    fun createAddState(chatId: Long, groups: List<Group>, peoples: List<User>): ChatIdAddState {
        val chatIdAddState = ChatIdAddState(peoples, groups)
        addState[chatId] = chatIdAddState

        return chatIdAddState
    }

    fun deleteAddState(chatId: Long) {
        addState.remove(chatId)
    }

    fun hasAddState(chatId: Long): Boolean {
        return addState.containsKey(chatId)
    }

    fun getAddState(chatId: Long): Optional<ChatIdAddState> {
        return Optional.ofNullable(addState[chatId])
    }

    fun addRemoveState(chatId: Long) {
       removeState.add(chatId)
    }

    fun removeRemoveState(chatId: Long) {
        removeState.remove(chatId)
    }

    fun hasRemoveState(chatId: Long): Boolean {
        return removeState.contains(chatId)
    }

    fun createChoiceManager(chatId: Long, users: List<Receipt>) {
        choiceManagerMap[chatId] = ChoiceManager(users, 3)
    }

    fun hasChoiceManager(chatId: Long): Boolean {
        return choiceManagerMap.containsKey(chatId)
    }

    fun getChoiceManager(chatId: Long): ChoiceManager<Receipt> {
        return choiceManagerMap.getValue(chatId)
    }

    fun removeChoiceManager(chatId: Long) {
        choiceManagerMap.remove(chatId)
    }
}

class ChatIdAddState(val people: List<User>, val groups: List<Group>) {
    var amount: Float = 0f
    var buyer: ChoiceManager<User> = ChoiceManager(people, 3)
    var participents: ChoiceManager<User> = ChoiceManager(people, 3)
    var participentsGroup: ChoiceManager<Group> = ChoiceManager(groups, 3)
    var currentState: AddState = AddState.AmountAdding
}

enum class AddState {
    AmountAdding,
    BuyerChoosing,
    ParticipantsChoosing,
}
