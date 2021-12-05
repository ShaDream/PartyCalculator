package utility

import com.github.kotlintelegrambot.entities.keyboard.KeyboardButton

public object Utility {
    fun GenerateUsersButton(buttonsNames : List<String>): List<List<KeyboardButton>> {
        return buttonsNames.map{ value -> listOf(KeyboardButton(value))}
    }

}