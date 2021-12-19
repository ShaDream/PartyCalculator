package manager

import repository.User
import java.util.concurrent.ConcurrentHashMap

object GroupManager {
    private var addState = ConcurrentHashMap.newKeySet<Long>()
    private var listState = ConcurrentHashMap.newKeySet<Long>()
    private var editExactGroupState: ConcurrentHashMap<Long, String> = ConcurrentHashMap()
    private var choiceManagerMap: ConcurrentHashMap<Long, ChoiceManager<User>> = ConcurrentHashMap()

    fun addListState(chatId: Long) {
        addState.add(chatId)
    }

    fun removeListState(chatId: Long) {
        addState.remove(chatId)
    }

    fun hasListState(chatId: Long): Boolean {
        return addState.contains(chatId)
    }

    fun addAddState(chatId: Long) {
        addState.add(chatId)
    }

    fun removeAddState(chatId: Long) {
        addState.remove(chatId)
    }

    fun hasAddState(chatId: Long): Boolean {
        return addState.contains(chatId)
    }


    fun createEditState(chatId: Long, groupName: String) {
        editExactGroupState[chatId] = groupName
    }

    fun hasEditState(chatId: Long): Boolean {
        return editExactGroupState.containsKey(chatId)
    }

    fun getEditGroupState(chatId: Long): String {
        return editExactGroupState.getValue(chatId)
    }

    fun removeEditState(chatId: Long) {
        editExactGroupState.remove(chatId)
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