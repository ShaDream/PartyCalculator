package manager

import repository.Group
import repository.Receipt
import repository.User
import java.util.concurrent.ConcurrentHashMap

object GroupManager {
    private var addState = ConcurrentHashMap.newKeySet<Long>()
    private var listState = ConcurrentHashMap.newKeySet<Long>()
    private var editExactGroupState: ConcurrentHashMap<Long, String> = ConcurrentHashMap()
    private var choiceManagerMap = ConcurrentHashMap<Long, GroupUsersEditState>()


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

    fun createEditState(chatId: Long, groupName: String, ) {
        editExactGroupState[chatId] = groupName
    }

    fun hasEditState(chatId: Long): Boolean {
        return editExactGroupState.containsKey(chatId)
    }

    fun getEditGroupName(chatId: Long): String {
        return editExactGroupState.getValue(chatId)
    }

    fun removeEditState(chatId: Long) {
        editExactGroupState.remove(chatId)
    }


    fun createChoiceManager(chatId: Long, participantsInGroup: List<User>, participantsNotInGroup: List<User>) {
        choiceManagerMap[chatId] = GroupUsersEditState(participantsInGroup, participantsNotInGroup)
    }

    fun hasChoiceManager(chatId: Long): Boolean {
        return choiceManagerMap.containsKey(chatId)
    }

    fun getChoiceManager(chatId: Long): GroupUsersEditState {
        return choiceManagerMap.getValue(chatId)
    }

    fun removeChoiceManager(chatId: Long) {
        choiceManagerMap.remove(chatId)
    }
}

class GroupUsersEditState(participantsInGroup: List<User>, participantsNotInGroup: List<User>) {
    var usersInGroup: ChoiceManager<User> = ChoiceManager(participantsInGroup, 3)
    var usersNotInGroup: ChoiceManager<User> = ChoiceManager(participantsNotInGroup, 3)

}