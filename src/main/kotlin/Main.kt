import action.ActionsManager
import action.MainActionManager
import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.text
import com.github.kotlintelegrambot.entities.ChatId
import feature.StartFeature
import message.Message
import state.StateManager

fun main() {
    val TOKEN_API = "883462233:AAEcOegWNoTtR7mWmz0zCDgDJNlw9KvVEeA"

    val actionsManager = ActionsManager.Builder()
        .setBaseManager(MainActionManager())
        .build()

    val viewModel = ViewModel(
        StartFeature()
    )

    val bot = bot {
        token = TOKEN_API
        dispatch {
            text {
                val action = actionsManager.getAction(text, message.chat.id)
                viewModel.pullAction(action)
                println(text)
            }
        }
    }

    viewModel.messages.subscribe { message ->
        when (message) {
            is Message.Text -> bot.sendMessage(ChatId.fromId(message.chatId), message.message)
        }
    }

    bot.startPolling()
}