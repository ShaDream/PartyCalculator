package manager

import repository.User
import state.State
import java.util.concurrent.*

object ParticipantManager {
    private var addState = ConcurrentHashMap.newKeySet<Long>()

    //TODO: set to map with system to check the state of deleted users
    private var removeState = ConcurrentHashMap.newKeySet<Long>()

    private var choiceManagerMap: ConcurrentHashMap<Long, ChoiceManager<User>> = ConcurrentHashMap()

    fun addAddState(chatId: Long) {
        addState.add(chatId)
    }

    fun removeAddState(chatId: Long) {
        addState.remove(chatId)
    }

    fun hasAddState(chatId: Long): Boolean {
        return addState.contains(chatId)
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

    fun createChoiceManager(chatId: Long, users: List<User>) {
        choiceManagerMap[chatId] = ChoiceManager(users, 3)
    }

    fun hasChoiceManager(chatId: Long): Boolean {
        return choiceManagerMap.containsKey(chatId)
    }

    fun getChoiceManager(chatId: Long): ChoiceManager<User> {
        return choiceManagerMap.getValue(chatId)
    }

    fun removeChoiceManager(chatId: Long) {
        choiceManagerMap.remove(chatId)
    }
}