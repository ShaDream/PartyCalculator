package manager

import java.util.concurrent.*

object ParticipantManager {
    private var addState = ConcurrentHashMap.newKeySet<Long>()
    //TODO: set to map with system to check the state of deleted users
    private var removeState = ConcurrentHashMap.newKeySet<Long>()

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
}