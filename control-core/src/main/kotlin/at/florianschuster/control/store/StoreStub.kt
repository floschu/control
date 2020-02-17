package at.florianschuster.control.store

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch

/**
 * Use this [StoreStub] for view testing.
 */
interface StoreStub<Action, State> {

    /**
     * [Store] actions as ordered [List].
     * Use this to verify if view bindings trigger the correct [Action]'s.
     */
    val actions: List<Action>

    /**
     * Offers a mocked [State].
     * Use this to verify if state is correctly bound to a view.
     */
    fun setState(state: State)

    companion object
}

/**
 * An implementation of [StoreStub].
 */
@ExperimentalCoroutinesApi
@FlowPreview
internal class StoreStubImplementation<Action, State>(
    storeImplementation: StoreImplementation<Action, *, State>
) : StoreStub<Action, State> {

    private val _actions = mutableListOf<Action>()

    internal val actionChannel = BroadcastChannel<Action>(1)
    internal val stateChannel = ConflatedBroadcastChannel(storeImplementation.initialState)

    override val actions: List<Action> get() = _actions

    override fun setState(state: State) {
        stateChannel.offer(state)
    }

    init {
        storeImplementation.scope.launch { actionChannel.asFlow().toList(_actions) }
    }
}