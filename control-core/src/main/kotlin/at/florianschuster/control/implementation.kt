package at.florianschuster.control

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel.Factory.BUFFERED
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.launch

/**
 * An implementation of [Controller].
 */
@ExperimentalCoroutinesApi
@FlowPreview
internal class ControllerImplementation<Action, Mutation, State>(
    internal val scope: CoroutineScope,
    internal val dispatcher: CoroutineDispatcher,
    internal val coroutineStart: CoroutineStart,

    internal val initialState: State,
    internal val mutator: Mutator<Action, Mutation, State>,
    internal val reducer: Reducer<Mutation, State>,

    internal val actionsTransformer: Transformer<Action>,
    internal val mutationsTransformer: Transformer<Mutation>,
    internal val statesTransformer: Transformer<State>,

    internal val tag: String,
    internal val controllerLog: ControllerLog
) : ManagedController<Action, Mutation, State> {

    // region state machine

    private val actionChannel = BroadcastChannel<Action>(BUFFERED)
    private val mutableStateFlow = MutableStateFlow(initialState)

    internal val stateJob: Job = scope.launch(
        context = dispatcher + CoroutineName(tag),
        start = coroutineStart
    ) {
        val actionFlow: Flow<Action> = actionsTransformer(actionChannel.asFlow())

        val mutatorScope = mutatorScope({ currentState }, actionFlow)
        val mutationFlow: Flow<Mutation> = actionFlow.flatMapMerge { action ->
            controllerLog.log(ControllerEvent.Action(tag, action.toString()))
            mutatorScope.mutator(action).catch { cause ->
                val error = ControllerError.Mutate(tag, "$action", cause)
                controllerLog.log(ControllerEvent.Error(tag, error))
                throw error
            }
        }

        val stateFlow: Flow<State> = mutationsTransformer(mutationFlow)
            .onEach { controllerLog.log(ControllerEvent.Mutation(tag, it.toString())) }
            .scan(initialState) { previousState, mutation ->
                runCatching { reducer(mutation, previousState) }.getOrElse { cause ->
                    val error = ControllerError.Reduce(
                        tag, "$previousState", "$mutation", cause
                    )
                    controllerLog.log(ControllerEvent.Error(tag, error))
                    throw error
                }
            }

        statesTransformer(stateFlow)
            .onStart { controllerLog.log(ControllerEvent.Started(tag)) }
            .onEach { state ->
                controllerLog.log(ControllerEvent.State(tag, state.toString()))
                mutableStateFlow.value = state
            }
            .onCompletion { controllerLog.log(ControllerEvent.Completed(tag)) }
            .collect()
    }

    // endregion

    // region stub

    internal lateinit var stub: ControllerStubImplementation<Action, State>
    internal val stubInitialized: Boolean get() = this::stub.isInitialized

    // endregion

    // region controller

    override val state: Flow<State>
        get() = if (stubInitialized) stub.stateFlow else {
            start()
            mutableStateFlow
        }

    override val currentState: State
        get() = if (stubInitialized) stub.stateFlow.value else {
            start()
            mutableStateFlow.value
        }

    override fun dispatch(action: Action) {
        if (stubInitialized) {
            stub.mutableDispatchedActions.add(action)
        } else {
            start()
            actionChannel.offer(action)
        }
    }

    // endregion

    // region managed controller

    override fun start(): Boolean {
        return if (stateJob.isActive) false else stateJob.start()
    }

    override fun cancel(): State {
        stateJob.cancel()
        return mutableStateFlow.value
    }

    // endregion

    init {
        controllerLog.log(ControllerEvent.Created(tag))
    }

    companion object {
        fun <Action, State> mutatorScope(
            stateAccessor: () -> State,
            actionFlow: Flow<Action>
        ): MutatorScope<Action, State> = object :
            MutatorScope<Action, State> {
            override val currentState: State get() = stateAccessor()
            override val actions: Flow<Action> = actionFlow
        }
    }
}
