package repository

import state.State
import java.util.*

interface UserStateRepository {

    fun getState(chatId: Long): Optional<State>

    fun setState(chatId: Long, state: State)
}