import action.ActionsManager
import action.MainActionManager
import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.text
import com.github.kotlintelegrambot.entities.ChatId
import com.sksamuel.hoplite.ConfigLoader
import config.ApplicationConfig
import feature.HelpFeature
import feature.StartFeature
import message.Message

fun main() {

    val config = ConfigLoader().loadConfigOrThrow<ApplicationConfig>("/application.conf")

    val actionsManager = ActionsManager.Builder()
        .setBaseManager(MainActionManager())
        .build()

    val viewModel =
        ViewModel(
            listOf(
                StartFeature(),
                HelpFeature()
            )

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
            is Message.Text -> bot.sendMessage(
                chatId = ChatId.fromId(message.chatId),
                text = message.message,
                replyMarkup = message.buttons.toKeyboardReplyMarkup()
            )
        }
    }

    bot.startPolling()
}

//https://github.com/kotlin-telegram-bot/kotlin-telegram-bot/blob/main/samples/dispatcher/src/main/kotlin/com/github/kotlintelegrambot/dispatcher/Main.kt