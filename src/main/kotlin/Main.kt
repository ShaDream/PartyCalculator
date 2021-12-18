import action.ActionsManager
import action.MainActionManager
import action.ParticipantActionManager
import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.text
import com.github.kotlintelegrambot.entities.ChatId
import com.sksamuel.hoplite.ConfigLoader
import config.ApplicationConfig
import feature.MainFeature
import feature.participant.ParticipantAddFeature
import feature.participant.ParticipantListFeature
import message.Message
import org.jetbrains.exposed.sql.Database
import repository.Participants
import state.State

fun main() {

    val config = ConfigLoader().loadConfigOrThrow<ApplicationConfig>("/application.conf")

    val database = Database.connect(
        url = config.database.url,
        driver = "org.postgresql.Driver",
        user = config.database.user,
        password = config.database.password,
    )

    val participantsRepo = Participants(database)

    val actionsManager = ActionsManager.Builder()
        .setBaseManager(MainActionManager())
        .addManager(State.People, ParticipantActionManager())
        .build()

    val viewModel =
        ViewModel(
            listOf(
                MainFeature(),
                ParticipantAddFeature(participantsRepo),
                ParticipantListFeature(participantsRepo),
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