package at.florianschuster.control.githubexample.store

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.scan

typealias Mutator<Action, Mutation> = (action: Action) -> Flow<Mutation>
typealias Reducer<State, Mutation> = (previousState: State, mutation: Mutation) -> State
typealias SideEffects<E> = (Flow<E>) -> Flow<E>

class Store<Action, Mutation, State>(
    initialState: State,

    scope: CoroutineScope,

    mutator: Mutator<Action, Mutation> = { emptyFlow() },
    reducer: Reducer<State, Mutation> = { previousState, _ -> previousState },

    actionSideEffects: SideEffects<Action> = { it },
    mutationSideEffects: SideEffects<Mutation> = { it },
    stateSideEffects: SideEffects<State> = { it }
) {

    private val actionChannel = BroadcastChannel<Action>(1)
    private val stateChannel = ConflatedBroadcastChannel<State>()

    val actions: SendChannel<Action> get() = actionChannel
    val state: Flow<State> get() = stateChannel.asFlow()
    val currentState: State get() = stateChannel.value

    init {
        val mutationFlow: Flow<Mutation> = actionSideEffects(actionChannel.asFlow())
            .flatMapMerge { incomingAction ->
                mutator(incomingAction).catch { e -> println(e) }
            }

        val stateFlow: Flow<State> = mutationSideEffects(mutationFlow)
            .scan(initialState) { previousState, incomingMutation ->
                reducer(previousState, incomingMutation)
            }
            .catch { e -> println(e) }

        stateSideEffects(stateFlow).onEach(stateChannel::send).launchIn(scope)
    }
}