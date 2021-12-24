package helper

object CommonButtons {

    fun mainMenuButtons(): List<List<String>> {
        return listOf(
            listOf("/start", "/help"),
            listOf("/receipt", "/people", "/group"),
        )
    }
}