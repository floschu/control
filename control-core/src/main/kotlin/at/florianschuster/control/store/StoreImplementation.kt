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
    private val mutator: Mutator<Action, Mutation>,
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
    private val stubImplementation by lazy { StoreStubImplementation(this) }

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
            stubImplementation.actionChannel.offer(action)
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
                mutator(action).catch { cause ->
                    val error = StoreError.Mutator(tag, "$action", cause)
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
                    val error = StoreError.Reducer(tag, "$previousState", "$mutation", cause)
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
}