package helper

object CommonButtons {

    fun mainMenuButtons(): List<List<String>> {
        return listOf(
            listOf("/start", "/help"),
            listOf("/receipt", "/people", "/group"),
        )
    }

    fun mainGroupButtons(): List<List<String>> {
        return listOf(
            listOf("/start"),
            listOf("/createGroup", "/editGroups"),
            listOf("/removeGroups", "/listGroups")
        )
    }
}