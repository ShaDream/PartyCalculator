package action

sealed class Action(open val chatId: Long) {
    sealed class Main(chatId: Long) : Action(chatId) {
        data class Start(override val chatId: Long) : Main(chatId)
        data class Help(override val chatId: Long) : Main(chatId)
        data class People(override val chatId: Long) : Main(chatId)
        data class Receipt(override val chatId: Long) : Main(chatId)
        data class Group(override val chatId: Long) : Main(chatId)
    }

    sealed class Participant(chatId: Long) : Action(chatId) {
        sealed class Add(override val chatId: Long) : Participant(chatId) {
            data class Start(override val chatId: Long) : Add(chatId)
            data class End(override val chatId: Long) : Add(chatId)
            data class New(override val chatId: Long, val message: String) : Add(chatId)
        }

        sealed class Remove(chatId: Long) : Action(chatId) {
            data class Start(override val chatId: Long) : Remove(chatId)
            data class End(override val chatId: Long) : Remove(chatId)
            data class Next(override val chatId: Long) : Remove(chatId)
            data class Previous(override val chatId: Long) : Remove(chatId)
            data class Choice(override val chatId: Long, val message: String) : Remove(chatId)
            data class Delete(override val chatId: Long) : Remove(chatId)
        }

        data class List(override val chatId: Long) : Participant(chatId)
    }

    sealed class Receipt(chatId: Long) : Action(chatId) {
        sealed class Add(chatId: Long) : Receipt(chatId) {
            data class Start(override val chatId: Long) : Add(chatId)
            data class Discard(override val chatId: Long) : Add(chatId)
            data class Back(override val chatId: Long) : Add(chatId)
            data class Number(override val chatId: Long, val number: Float) : Add(chatId)
            data class Previous(override val chatId: Long) : Add(chatId)
            data class Next(override val chatId: Long) : Add(chatId)
            data class NextGroup(override val chatId: Long) : Add(chatId)
            data class PreviousGroup(override val chatId: Long) : Add(chatId)
            data class Apply(override val chatId: Long) : Add(chatId)
            data class Choice(override val chatId: Long, val value: String) : Add(chatId)
        }

        data class List(override val chatId: Long) : Participant(chatId)

        sealed class Remove(chatId: Long) : Action(chatId) {
            data class Start(override val chatId: Long) : Remove(chatId)
            data class End(override val chatId: Long) : Remove(chatId)
            data class Next(override val chatId: Long) : Remove(chatId)
            data class Previous(override val chatId: Long) : Remove(chatId)
            data class Choice(override val chatId: Long, val message: String) : Remove(chatId)
            data class Delete(override val chatId: Long) : Remove(chatId)
        }

        data class Calculate(override val chatId: Long) : Participant(chatId)
    }

    sealed class Group(chatId: Long) : Action(chatId) {
        sealed class Add(chatId: Long) : Group(chatId) {
            data class Start(override val chatId: Long) : Group.Add(chatId)
            data class End(override val chatId: Long) : Group.Add(chatId)
            data class New(override val chatId: Long, val message: String) : Group.Add(chatId)
            data class Discard(override val chatId: Long) : Receipt.Add(chatId)
        }

        sealed class Edit(chatId: Long) : Group(chatId) {
            data class Start(override val chatId: Long) : Edit(chatId)
            data class End(override val chatId: Long) : Edit(chatId)
            data class PreviousUsersInGroup(override val chatId: Long) : Edit(chatId)
            data class NextUsersInGroup(override val chatId: Long) : Edit(chatId)
            data class NextUsersNotInGroup(override val chatId: Long) : Edit(chatId)
            data class PreviousUsersNotInGroup(override val chatId: Long) : Edit(chatId)
            data class EditMembers(override val chatId: Long) : Edit(chatId)
            data class Back(override val chatId: Long) : Group.Edit(chatId)
            data class Choice(override val chatId: Long, val message: String) : Edit(chatId)
            data class Apply(override val chatId: Long) : Edit(chatId)
            data class Previous(override val chatId: Long) : Edit(chatId)
            data class Next(override val chatId: Long) : Group.Edit(chatId)
            data class BackToMenu(override val chatId: Long) : Edit(chatId)
            data class ChoiceOfGroup(override val chatId: Long, val message: String) : Edit(chatId)
            data class Delete(override val chatId: Long) : Edit(chatId)
        }

        data class List(override val chatId: Long) : Group(chatId)

        sealed class Remove(chatId: Long) : Group(chatId) {
            data class Start(override val chatId: Long) : Remove(chatId)
            data class End(override val chatId: Long) : Remove(chatId)
            data class Next(override val chatId: Long) : Remove(chatId)
            data class Previous(override val chatId: Long) : Remove(chatId)
            data class Choice(override val chatId: Long, val message: String) : Remove(chatId)
            data class Delete(override val chatId: Long) : Remove(chatId)
        }
    }

    data class UndefinedAction(override val chatId: Long) : Action(chatId)

}