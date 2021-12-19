package helper

private val forbiddenSymbols = listOf("⬅️", "/", "➡️", "▶️", "◀️")

object NameChecker {
    fun isNameValid(name: String): Boolean {
        return name.toFloatOrNull() == null && forbiddenSymbols.all { !name.contains(it) }
    }
}