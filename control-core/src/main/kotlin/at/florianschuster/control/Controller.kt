package at.florianschuster.control

import at.florianschuster.control.store.Store
import at.florianschuster.control.store.createStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow

/**
 * A [Controller] is an UI-independent class that stores and controls the state of a view.
 * The role of a [Controller] is to separate business-logic from view-logic and make testing
 * easy. Every view should have its own [Controller] and delegates all logic to it. A
 * [Controller] has no dependency to a view.
 *
 *
 * <pre>
 *                  [Action] via [dispatch]
 *          +-----------------------------------+
 *          |                                   |
 *     +----+-----+                    +--------|-------+
 *     |          |                    |        v       |
 *     |   View   |                    |  [Controller]  |
 *     |    ^     |                    |                |
 *     +----|-----+                    +--------+-------+
 *          |                                   |
 *          +-----------------------------------+
 *                  [State] via [state]
 * </pre>
 *
 * The [Controller] uses a [Store] to create an uni-directional stream of data as shown in the
 * diagram above.
 */
interface Controller<Action, State> {

    /**
     * See [Store] and [CoroutineScope.createStore].
     */
    val store: Store<Action, *, State>

    /**
     * See [Store.dispatch] for more details.
     */
    fun dispatch(action: Action) = store.dispatch(action)

    /**
     * See [Store.currentState] for more details.
     */
    val currentState: State get() = store.currentState

    /**
     * See [Store.state] for more details.
     */
    val state: Flow<State> get() = store.state

    companion object
}