package feature

import action.Action
import io.reactivex.rxjava3.core.Observable
import message.Message

interface IFeature {
    fun bind(actions: Observable<Action>): Observable<Message>
}