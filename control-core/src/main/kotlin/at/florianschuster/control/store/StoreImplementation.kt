package at.florianschuster.control.store

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.launch

/**
 * An implementation of [Store].
 */
@ExperimentalCoroutinesApi
@FlowPreview
internal class StoreImplementation<Action, Mutation, State>(
    internal val scope: CoroutineScope,
    private val dispatcher: CoroutineDispatcher,

    internal val initialState: State,
    private val mutator: Mutator<Action, State, Mutation>,
    private val reducer: Reducer<Mutation, State>,

    private val actionsTransformer: Transformer<Action>,
    private val mutationsTransformer: Transformer<Mutation>,
    private val statesTransformer: Transformer<State>,

    private val tag: String,
    private val storeLogger: StoreLogger
) : Store<Action, Mutation, State> {

    private var stateFlowCreated = false

    private val actionChannel by lazy { BroadcastChannel<Action>(1) }
    private val stateChannel by lazy { ConflatedBroadcastChannel(initialState) }
    private val stubImplementation by lazy { StoreStubImplementation<Action, State>(initialState) }

    override val state: Flow<State>
        get() = if (!stubEnabled) {
            if (!stateFlowCreated) createStateFlow()
            stateChannel.asFlow()
        } else {
            stubImplementation.stateChannel.asFlow()
        }

    override val currentState: State
        get() = if (!stubEnabled) {
            if (!stateFlowCreated) createStateFlow()
            stateChannel.value
        } else {
            stubImplementation.stateChannel.value
        }

    override fun dispatch(action: Action) {
        if (!stubEnabled) {
            if (!stateFlowCreated) createStateFlow()
            actionChannel.offer(action)
        } else {
            stubImplementation.mutableActions.add(action)
        }
    }

    override var stubEnabled: Boolean = false
        set(value) {
            storeLogger.log(tag, StoreLogger.Event.Stub(value))
            field = value
        }

    override val stub: StoreStub<Action, State> get() = stubImplementation

    init {
        storeLogger.log(tag, StoreLogger.Event.Created)
    }

    private fun createStateFlow() {
        val mutationFlow: Flow<Mutation> = actionsTransformer(actionChannel.asFlow())
            .flatMapMerge { action ->
                storeLogger.log(tag, StoreLogger.Event.Action(action.toString()))
                mutator(action) { currentState }.catch { cause ->
                    val error = Error.Mutator(tag, "$action", cause)
                    storeLogger.log(tag, StoreLogger.Event.Error(error))
                    throw error
                }
            }

        val stateFlow: Flow<State> = mutationsTransformer(mutationFlow)
            .scan(initialState) { previousState, mutation ->
                storeLogger.log(tag, StoreLogger.Event.Mutation(mutation.toString()))
                val reducedState = try {
                    reducer(previousState, mutation)
                } catch (cause: Throwable) {
                    val error = Error.Reducer(tag, "$previousState", "$mutation", cause)
                    storeLogger.log(tag, StoreLogger.Event.Error(error))
                    throw error
                }
                storeLogger.log(tag, StoreLogger.Event.State(reducedState.toString()))
                reducedState
            }

        scope.launch(dispatcher + CoroutineName(tag)) {
            statesTransformer(stateFlow)
                .distinctUntilChanged()
                .onStart { storeLogger.log(tag, StoreLogger.Event.Started) }
                .onEach(stateChannel::send)
                .onCompletion { storeLogger.log(tag, StoreLogger.Event.Destroyed) }
                .collect()
        }

        stateFlowCreated = true
    }

    internal sealed class Error(
        message: String,
        cause: Throwable
    ) : RuntimeException(message, cause) {

        class Mutator internal constructor(
            tag: String,
            action: String,
            cause: Throwable
        ) : Error("Mutator error in $tag, action = $action", cause)

        class Reducer internal constructor(
            tag: String,
            previousState: String,
            mutation: String,
            cause: Throwable
        ) : Error(
            message = "Reducer error in $tag, previousState = $previousState, mutation = $mutation",
            cause = cause
        )
    }
}