package manager

data class ChoiceManager<T>(val elems: List<T>, val pageSize: Int) {

    private var selected = mutableSetOf<T>()

    private var notSelected = mutableSetOf<T>()

    private var currentPage = 0

    private var maxPage = 0

    init {
        notSelected.addAll(elems)

        maxPage =
            if (elems.size % pageSize == 0) elems.size / pageSize - 1
            else if (elems.size < pageSize) 0
            else elems.size / pageSize
    }

    fun toggle(t: T): Boolean {
        if (selected.contains(t)) {
            selected.remove(t)
            notSelected.add(t)
            return true;
        } else if (notSelected.contains(t)) {
            notSelected.remove(t)
            selected.add(t)
            return true
        }
        return false
    }

    fun getButtons(f: (T) -> String, previousButton: String, nextButton: String): List<String> {
        var buttons = elems
            .subList(currentPage * pageSize, minOf((currentPage + 1) * pageSize, elems.size))
            .map { if (selected.contains(it)) "${f(it)} ✅" else f(it) }

        if (currentPage > 0)
            buttons = listOf(previousButton) + buttons

        if (currentPage < maxPage)
            buttons = buttons + nextButton

        return buttons
    }

    fun getSelected(): List<T> {
        return selected.toList()
    }

    fun nextPage(): Boolean {
        if (maxPage < currentPage + 1) return false
        currentPage += 1
        return true
    }

    fun previousPage(): Boolean {
        if (currentPage > 0) {
            currentPage -= 1
            return true
        }
        return false
    }

}