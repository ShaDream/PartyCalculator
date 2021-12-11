import action.Action
import feature.IFeature
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Observable.merge
import io.reactivex.rxjava3.subjects.PublishSubject
import message.Message
import java.util.concurrent.TimeUnit

class ViewModel(features: List<IFeature>) {

    private val actions: PublishSubject<Action> = PublishSubject.create()

    val messages: PublishSubject<Message> = PublishSubject.create()

    init {
        merge(features.map { it.bind(actions) })
            .concatMap { i -> Observable.just(i).delay(100, TimeUnit.MILLISECONDS) }
            .subscribe(messages::onNext)
    }

    fun pullAction(action: Action) = actions.onNext(action)
}