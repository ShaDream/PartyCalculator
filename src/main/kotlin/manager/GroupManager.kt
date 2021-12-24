package manager

import repository.Group
import repository.Receipt
import repository.User
import java.util.concurrent.ConcurrentHashMap

object GroupManager {
    private var addState = ConcurrentHashMap.newKeySet<Long>()
    private var removeState = ConcurrentHashMap.newKeySet<Long>()
    private var editExactGroupState: ConcurrentHashMap<Long, String> = ConcurrentHashMap()
    private var usersChoiceManagerMap = ConcurrentHashMap<Long, GroupUsersEditState>()
    private var choiceManagerMap = ConcurrentHashMap<Long, ChoiceManager<Group>>()

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

    fun createChoiceManager(chatId: Long, groups: List<Group>) {
        choiceManagerMap[chatId] = ChoiceManager(groups, 3)
    }

    fun hasChoiceManager(chatId: Long): Boolean {
        return choiceManagerMap.containsKey(chatId)
    }

    fun getChoiceManager(chatId: Long): ChoiceManager<Group> {
        return choiceManagerMap.getValue(chatId)
    }

    fun removeChoiceManager(chatId: Long) {
        choiceManagerMap.remove(chatId)
    }

    fun createUsersChoiceManager(chatId: Long, participantsInGroup: List<User>, participantsNotInGroup: List<User>) {
        usersChoiceManagerMap[chatId] = GroupUsersEditState(participantsInGroup, participantsNotInGroup)
    }

    fun hasUsersChoiceManager(chatId: Long): Boolean {
        return usersChoiceManagerMap.containsKey(chatId)
    }

    fun getUsersChoiceManager(chatId: Long): GroupUsersEditState {
        return usersChoiceManagerMap.getValue(chatId)
    }

    fun removeUsersChoiceManager(chatId: Long) {
        usersChoiceManagerMap.remove(chatId)
    }
}

class GroupUsersEditState(participantsInGroup: List<User>, participantsNotInGroup: List<User>) {
    var usersInGroup: ChoiceManager<User> = ChoiceManager(participantsInGroup, 3)
    var usersNotInGroup: ChoiceManager<User> = ChoiceManager(participantsNotInGroup, 3)

}