package at.florianschuster.control

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.launch

/**
 * An implementation of [Controller].
 */
internal class ControllerImplementation<Action, Mutation, State, Effect>(
    val scope: CoroutineScope,
    val dispatcher: CoroutineDispatcher,
    val controllerStart: ControllerStart,

    val initialState: State,
    val mutator: EffectMutator<Action, Mutation, State, Effect>,
    val reducer: EffectReducer<Mutation, State, Effect>,

    val actionsTransformer: EffectTransformer<Action, Effect>,
    val mutationsTransformer: EffectTransformer<Mutation, Effect>,
    val statesTransformer: EffectTransformer<State, Effect>,

    val tag: String,
    val controllerLog: ControllerLog
) : EffectController<Action, State, Effect>, EffectControllerStub<Action, State, Effect> {

    // region state machine

    private val actionSharedFlow = MutableSharedFlow<Action>(
        replay = 0,
        extraBufferCapacity = CAPACITY
    )
    private val mutableStateFlow = MutableStateFlow(initialState)

    @OptIn(ExperimentalCoroutinesApi::class)
    internal val stateJob: Job = scope.launch(
        context = dispatcher + CoroutineName(tag),
        start = CoroutineStart.LAZY
    ) {
        val transformerContext = createTransformerContext(effectEmitter)

        val actionFlow: Flow<Action> = transformerContext
            .actionsTransformer(actionSharedFlow.asSharedFlow())

        val mutatorContext = createMutatorContext(
            stateAccessor = { state.value },
            actionFlow = actionFlow,
            effectEmitter = effectEmitter
        )

        val mutationFlow: Flow<Mutation> = actionFlow.flatMapMerge { action ->
            controllerLog.log { ControllerEvent.Action(tag, action.toString()) }
            mutatorContext.mutator(action).catch { cause ->
                val error = ControllerError.Mutate(tag, "$action", cause)
                controllerLog.log { ControllerEvent.Error(tag, error) }
                throw error
            }
        }

        val reducerContext = createReducerContext(effectEmitter)

        val stateFlow: Flow<State> = transformerContext.mutationsTransformer(mutationFlow)
            .onEach { controllerLog.log { ControllerEvent.Mutation(tag, it.toString()) } }
            .scan(initialState) { previousState, mutation ->
                runCatching { reducerContext.reducer(mutation, previousState) }.getOrElse { cause ->
                    val error = ControllerError.Reduce(
                        tag, "$previousState", "$mutation", cause
                    )
                    controllerLog.log { ControllerEvent.Error(tag, error) }
                    throw error
                }
            }

        transformerContext.statesTransformer(stateFlow)
            .onStart { controllerLog.log { ControllerEvent.Started(tag) } }
            .onEach { state ->
                controllerLog.log { ControllerEvent.State(tag, state.toString()) }
                mutableStateFlow.value = state
            }
            .onCompletion { controllerLog.log { ControllerEvent.Completed(tag) } }
            .collect()
    }

    // endregion

    // region controller

    override val state: StateFlow<State>
        get() = if (stubEnabled) {
            stubbedStateFlow.asStateFlow()
        } else {
            if (controllerStart is ControllerStart.Lazy) start()
            mutableStateFlow.asStateFlow()
        }

    override fun dispatch(action: Action) {
        if (stubEnabled) {
            stubbedActions.add(action)
        } else {
            if (controllerStart is ControllerStart.Lazy) start()
            actionSharedFlow.tryEmit(action)
        }
    }

    // endregion

    // region effects

    private val effectChannel = Channel<Effect>(CAPACITY)

    private val effectEmitter: (Effect) -> Unit = { effect ->
        val canBeOffered = effectChannel.trySend(effect).isSuccess
        if (canBeOffered) {
            controllerLog.log { ControllerEvent.Effect(tag, effect.toString()) }
        } else {
            throw ControllerError.Effect(tag, effect.toString())
        }
    }

    override val effects: Flow<Effect>
        get() = if (stubEnabled) {
            stubbedEffectChannel.receiveAsFlow().cancellable()
        } else {
            if (controllerStart is ControllerStart.Lazy) start()
            effectChannel.receiveAsFlow().cancellable()
        }

    // endregion

    // region manual start + stop

    internal fun start(): Boolean {
        return if (stateJob.isActive) false else stateJob.start()
    }

    internal fun cancel() {
        stateJob.cancel()
    }

    // endregion

    // region stub

    internal var stubEnabled = false

    private val stubbedActions = mutableListOf<Action>()
    private val stubbedStateFlow = MutableStateFlow(initialState)
    private val stubbedEffectChannel = Channel<Effect>(CAPACITY)

    override val dispatchedActions: List<Action>
        get() = stubbedActions

    override fun emitState(state: State) {
        stubbedStateFlow.value = state
    }

    override fun emitEffect(effect: Effect) {
        stubbedEffectChannel.trySend(effect)
    }

    // endregion

    init {
        controllerLog.log { ControllerEvent.Created(tag, controllerStart.logName) }
        if (controllerStart is ControllerStart.Immediately) {
            start()
        }
    }

    companion object {
        internal const val CAPACITY = 64

        internal fun <Action, State, Effect> createMutatorContext(
            stateAccessor: () -> State,
            actionFlow: Flow<Action>,
            effectEmitter: (Effect) -> Unit
        ): EffectMutatorContext<Action, State, Effect> =
            object : EffectMutatorContext<Action, State, Effect> {
                override val currentState: State get() = stateAccessor()
                override val actions: Flow<Action> = actionFlow
                override fun emitEffect(effect: Effect) = effectEmitter(effect)
            }

        internal fun <Effect> createReducerContext(
            emitter: (Effect) -> Unit
        ): EffectReducerContext<Effect> = object : EffectReducerContext<Effect> {
            override fun emitEffect(effect: Effect) = emitter(effect)
        }

        internal fun <Effect> createTransformerContext(
            emitter: (Effect) -> Unit
        ): EffectTransformerContext<Effect> = object : EffectTransformerContext<Effect> {
            override fun emitEffect(effect: Effect) = emitter(effect)
        }
    }
}