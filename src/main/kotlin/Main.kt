import action.ActionsManager
import action.MainActionManager
import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.text
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.keyboard.KeyboardButton
import com.github.kotlintelegrambot.entities.KeyboardReplyMarkup
import com.sksamuel.hoplite.ConfigLoader
import config.ApplicationConfig
import feature.StartFeature
import message.Message
import state.StateManager
import utility.Utility
import kotlin.reflect.jvm.internal.impl.types.AbstractTypeCheckerContext

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
            is Message.Text -> bot.sendMessage(
                ChatId.fromId(message.chatId),
                message.message,

                replyMarkup = if(message.buttonNames.count() > 0)
                    KeyboardReplyMarkup(keyboard = Utility.GenerateUsersButton(message.buttonNames), oneTimeKeyboard = true)
                //Ещё можно стереть клавиатуру, отправив обратно ReplyKeyboardRemove()
                else null
            )
        }
    }

    bot.startPolling()
}

//https://github.com/kotlin-telegram-bot/kotlin-telegram-bot/blob/main/samples/dispatcher/src/main/kotlin/com/github/kotlintelegrambot/dispatcher/Main.kt