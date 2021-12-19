package manager

import action.Action
import com.github.kotlintelegrambot.entities.ChatId
import repository.Group
import repository.User
import java.util.concurrent.ConcurrentHashMap

object ReceiptManager {
    private var addState = ConcurrentHashMap<Long, ChatIdAddState>()

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

    fun getAddState(chatId: Long): ChatIdAddState? {
        return addState[chatId]
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
