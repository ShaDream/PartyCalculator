package action

import state.State
import state.StateManager

interface IActionsManager {
    fun getAction(command: String?, chatId: Long): Action
}

class ActionsManager(
    private val managers: Map<State, IActionsManager>,
    private val mainManager: IActionsManager
) : IActionsManager {

    override fun getAction(command: String?, chatId: Long): Action {
        val state = StateManager.getStateByChatId(chatId)
        return managers.getOrDefault(state, mainManager).getAction(command, chatId)
    }

    class Builder {
        private var mainManager: IActionsManager = MainActionManager()
        private var managers: MutableMap<State, IActionsManager> = mutableMapOf()

        fun setBaseManager(manager: IActionsManager): Builder {
            mainManager = manager
            return this
        }

        fun addManager(state: State, manager: IActionsManager): Builder {
            managers += Pair(state, manager)
            return this
        }

        fun build(): ActionsManager {
            return ActionsManager(managers, mainManager)
        }
    }
}

