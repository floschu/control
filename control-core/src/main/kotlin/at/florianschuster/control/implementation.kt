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
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.Flow
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
) : Controller<Action, Mutation, State> {

    internal val stateJob: Job

    private val actionChannel = BroadcastChannel<Action>(BUFFERED)
    private val stateChannel = ConflatedBroadcastChannel<State>()

    // region stub

    internal lateinit var stub: ControllerStubImplementation<Action, State>
    internal val stubInitialized: Boolean get() = this::stub.isInitialized

    // endregion

    // region controller

    override val state: Flow<State>
        get() = if (stubInitialized) stub.stateChannel.asFlow() else {
            if (!stateJob.isActive) startStateJob()
            stateChannel.asFlow()
        }

    override val currentState: State
        get() = if (stubInitialized) stub.stateChannel.value else {
            if (!stateJob.isActive) startStateJob()
            stateChannel.value
        }

    override fun dispatch(action: Action) {
        if (stubInitialized) {
            stub.mutableActions.add(action)
        } else {
            if (!stateJob.isActive) startStateJob()
            actionChannel.offer(action)
        }
    }

    init {
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
                try {
                    reducer(mutation, previousState)
                } catch (cause: Throwable) {
                    val error = ControllerError.Reduce(tag, "$previousState", "$mutation", cause)
                    controllerLog.log(ControllerEvent.Error(tag, error))
                    throw error
                }
            }

        controllerLog.log(ControllerEvent.Created(tag))

        stateJob = scope.launch(
            context = dispatcher + CoroutineName(tag),
            start = coroutineStart
        ) {
            statesTransformer(stateFlow)
                .onStart { controllerLog.log(ControllerEvent.Started(tag)) }
                .onEach { state ->
                    controllerLog.log(ControllerEvent.State(tag, state.toString()))
                    stateChannel.send(state)
                }
                .onCompletion { controllerLog.log(ControllerEvent.Completed(tag)) }
                .collect()
        }
    }

    internal fun startStateJob(): Boolean = kotlinx.atomicfu.locks.synchronized(this) {
        return if (stateJob.isActive) false // double checked locking
        else stateJob.start()
    }

    // endregion
}

internal fun <Action, State> mutatorScope(
    stateAccessor: () -> State,
    actionFlow: Flow<Action>
): MutatorScope<Action, State> = object : MutatorScope<Action, State> {
    override val currentState: State get() = stateAccessor()
    override val actions: Flow<Action> = actionFlow
}