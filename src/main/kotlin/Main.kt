import action.ActionsManager
import action.MainActionManager
import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.text
import com.github.kotlintelegrambot.entities.ChatId
import com.sksamuel.hoplite.ConfigLoader
import config.ApplicationConfig
import feature.StartFeature
import message.Message
import state.StateManager

fun main() {

    val config = ConfigLoader().loadConfigOrThrow<ApplicationConfig>("/application.conf")

    val actionsManager = ActionsManager.Builder()
        .setBaseManager(MainActionManager())
        .build()

    val viewModel = ViewModel(
        StartFeature()
    )

    val bot = bot {
        token = config.telegram.token
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