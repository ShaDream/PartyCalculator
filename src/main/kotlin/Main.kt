import action.ActionsManager
import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.text
import com.github.kotlintelegrambot.entities.ChatId
import feature.StartFeature
import message.Message

fun main() {
    val TOKEN_API = ""

    val actionsManager = ActionsManager()

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