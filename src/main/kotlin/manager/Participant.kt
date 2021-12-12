package manager

import com.github.kotlintelegrambot.entities.ChatId
import java.util.concurrent.*

object ParticipantManager {
    private var addParticipantState = ConcurrentHashMap.newKeySet<Long>()

    fun addToParticipantState(chatId: Long) {
        addParticipantState.add(chatId)
    }

    fun removeFromParticipantState(chatId: Long) {
        addParticipantState.remove(chatId)
    }

    fun hasInParticipantState(chatId: Long): Boolean {
        return addParticipantState.contains(chatId)
    }
}