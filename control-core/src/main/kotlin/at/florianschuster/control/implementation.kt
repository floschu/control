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
    val scope: CoroutineScope,
    val dispatcher: CoroutineDispatcher,
    val controllerStart: ControllerStart,

    val initialState: State,
    val mutator: Mutator<Action, Mutation, State>,
    val reducer: Reducer<Mutation, State>,

    val actionsTransformer: Transformer<Action>,
    val mutationsTransformer: Transformer<Mutation>,
    val statesTransformer: Transformer<State>,

    val tag: String,
    val controllerLog: ControllerLog
) : ManagedController<Action, Mutation, State> {

    // region state machine

    private val actionChannel = BroadcastChannel<Action>(BUFFERED)
    private val mutableStateFlow = MutableStateFlow(initialState)

    internal val stateJob: Job = scope.launch(
        context = dispatcher + CoroutineName(tag),
        start = CoroutineStart.LAZY
    ) {
        val actionFlow: Flow<Action> = actionsTransformer(actionChannel.asFlow())

        val mutatorContext = createMutatorContext({ currentState }, actionFlow)
        val mutationFlow: Flow<Mutation> = actionFlow.flatMapMerge { action ->
            controllerLog.log(ControllerEvent.Action(tag, action.toString()))
            mutatorContext.mutator(action).catch { cause ->
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
            if (controllerStart is ControllerStart.Lazy) start()
            mutableStateFlow
        }

    override val currentState: State
        get() = if (stubInitialized) stub.stateFlow.value else {
            if (controllerStart is ControllerStart.Lazy) start()
            mutableStateFlow.value
        }

    override fun dispatch(action: Action) {
        if (stubInitialized) {
            stub.mutableDispatchedActions.add(action)
        } else {
            if (controllerStart is ControllerStart.Lazy) start()
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
        controllerLog.log(ControllerEvent.Created(tag, controllerStart.logName))
        if (controllerStart is ControllerStart.Immediately) {
            start()
        }
    }

    companion object {
        fun <Action, State> createMutatorContext(
            stateAccessor: () -> State,
            actionFlow: Flow<Action>
        ): MutatorContext<Action, State> = object :
            MutatorContext<Action, State> {
            override val currentState: State get() = stateAccessor()
            override val actions: Flow<Action> = actionFlow
        }
    }
}
