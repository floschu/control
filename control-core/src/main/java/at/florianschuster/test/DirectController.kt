package at.florianschuster.test

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * A [DirectController] can be used when there is no need for asynchronous mutations.
 * Instead the [Action] is directly used to reduce the [State].
 */
@FlowPreview
@ExperimentalCoroutinesApi
interface DirectController<Action, State> : Controller<Action, Action, State> {
    override fun mutate(action: Action): Flow<Action> = flowOf(action)

    @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
    override fun reduce(previousState: State, action: Action): State = previousState
}