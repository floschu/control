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
import kotlinx.coroutines.flow.distinctUntilChanged
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
    scope: CoroutineScope,
    dispatcher: CoroutineDispatcher,
    coroutineStart: CoroutineStart,

    initialState: State,
    mutator: Mutator<Action, Mutation, State>,
    reducer: Reducer<Mutation, State>,

    actionsTransformer: Transformer<Action>,
    mutationsTransformer: Transformer<Mutation>,
    statesTransformer: Transformer<State>,

    private val tag: String,
    private val controllerLog: ControllerLog
) : Controller<Action, Mutation, State> {

    internal val stateJob: Job // internal for testing

    private val actionChannel = BroadcastChannel<Action>(BUFFERED)
    private val stateChannel = ConflatedBroadcastChannel(initialState)
    private val controllerStub by lazy { ControllerStubImplementation<Action, State>(initialState) }

    override val state: Flow<State>
        get() = if (!stubEnabled) {
            if (!stateJob.isActive) lazyStart()
            stateChannel.asFlow()
        } else {
            controllerStub.stateChannel.asFlow()
        }

    override val currentState: State
        get() = if (!stubEnabled) {
            if (!stateJob.isActive) lazyStart()
            stateChannel.value
        } else {
            controllerStub.stateChannel.value
        }

    override fun dispatch(action: Action) {
        if (!stubEnabled) {
            if (!stateJob.isActive) lazyStart()
            actionChannel.offer(action)
        } else {
            controllerStub.mutableActions.add(action)
        }
    }

    override var stubEnabled: Boolean = false
        set(value) {
            controllerLog.log(tag, ControllerLog.Event.Stub(value))
            field = value
        }

    override val stub: ControllerStub<Action, State> get() = controllerStub

    init {
        val actionFlow: Flow<Action> = actionsTransformer(actionChannel.asFlow())

        val mutatorScope = MutatorScopeImpl({ currentState }, actionFlow)
        val mutationFlow: Flow<Mutation> = actionFlow.flatMapMerge { action ->
            controllerLog.log(tag, ControllerLog.Event.Action(action.toString()))
            mutatorScope.mutator(action).catch { cause ->
                val error = ControllerError.Mutate(tag, "$action", cause)
                controllerLog.log(tag, ControllerLog.Event.Error(error))
                throw error
            }
        }

        val stateFlow: Flow<State> = mutationsTransformer(mutationFlow)
            .scan(initialState) { previousState, mutation ->
                controllerLog.log(tag, ControllerLog.Event.Mutation(mutation.toString()))
                val reducedState = try {
                    reducer(mutation, previousState)
                } catch (cause: Throwable) {
                    val error = ControllerError.Reduce(tag, "$previousState", "$mutation", cause)
                    controllerLog.log(tag, ControllerLog.Event.Error(error))
                    throw error
                }
                controllerLog.log(tag, ControllerLog.Event.State(reducedState.toString()))
                reducedState
            }

        controllerLog.log(tag, ControllerLog.Event.Created)

        stateJob = scope.launch(
            context = dispatcher + CoroutineName(tag),
            start = coroutineStart
        ) {
            statesTransformer(stateFlow)
                .distinctUntilChanged()
                .onStart { controllerLog.log(tag, ControllerLog.Event.Started) }
                .onEach(stateChannel::send)
                .onCompletion { controllerLog.log(tag, ControllerLog.Event.Destroyed) }
                .collect()
        }
    }

    private fun lazyStart() = kotlinx.atomicfu.locks.synchronized(this) {
        if (!stateJob.isActive) { // double checked locking
            stateJob.start()
        }
    }

    @Suppress("FunctionName")
    fun <Action, State> MutatorScopeImpl(
        stateAccessor: () -> State,
        actionFlow: Flow<Action>
    ): MutatorScope<Action, State> = object : MutatorScope<Action, State> {
        override val currentState: State get() = stateAccessor()
        override val actions: Flow<Action> = actionFlow
    }
}