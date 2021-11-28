package state

import java.util.concurrent.ConcurrentHashMap

object StateManager {
    private var chatIdState: ConcurrentHashMap<Long, State> = ConcurrentHashMap()

    fun getStateByChatId(chatId: Long): State {
        return chatIdState.getOrDefault(chatId, State.None)
    }

    fun setStateByChatId(chatId: Long, state: State) {
        chatIdState[chatId] = state
    }
}