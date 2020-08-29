package at.florianschuster.control

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.BUFFERED
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asFlow
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
@ExperimentalCoroutinesApi
@FlowPreview
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

    private val actionChannel = BroadcastChannel<Action>(BUFFERED)
    private val mutableStateFlow = MutableStateFlow(initialState)

    internal val stateJob: Job = scope.launch(
        context = dispatcher + CoroutineName(tag),
        start = CoroutineStart.LAZY
    ) {
        val transformerContext = createTransformerContext(effectEmitter)

        val actionFlow: Flow<Action> = transformerContext.actionsTransformer(actionChannel.asFlow())

        val mutatorContext = createMutatorContext(
            stateAccessor = { currentState },
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

    // region effects

    private val effectEmitter: (Effect) -> Unit = { effect ->
        controllerLog.log { ControllerEvent.Effect(tag, effect.toString()) }
        val canBeOffered = effectsChannel.offer(effect)
        if (!canBeOffered) {
            val error = ControllerError.Effect(tag, effect.toString())
            controllerLog.log { ControllerEvent.Error(tag, error) }
            throw error
        }
    }

    private val effectsChannel = Channel<Effect>(BUFFERED)
    override val effects: Flow<Effect>
        get() = if (stubEnabled) stubbedEffectFlow else {
            effectsChannel.receiveAsFlow().cancellable()
        }

    // endregion

    // region controller

    override val state: Flow<State>
        get() = if (stubEnabled) stubbedStateFlow else {
            if (controllerStart is ControllerStart.Lazy) start()
            mutableStateFlow
        }

    override val currentState: State
        get() = if (stubEnabled) stubbedStateFlow.value else {
            if (controllerStart is ControllerStart.Lazy) start()
            mutableStateFlow.value
        }

    override fun dispatch(action: Action) {
        if (stubEnabled) {
            stubActions.add(action)
        } else {
            if (controllerStart is ControllerStart.Lazy) start()
            actionChannel.offer(action)
        }
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

    private val stubActions = mutableListOf<Action>()
    private val stubbedStateFlow = MutableStateFlow(initialState)
    private val _stubbedEffectFlow = MutableStateFlow<Effect?>(null)
    private val stubbedEffectFlow = _stubbedEffectFlow.filterNotNullCast()

    override val dispatchedActions: List<Action>
        get() = stubActions

    override fun emitState(state: State) {
        stubbedStateFlow.value = state
    }

    override fun emitEffect(effect: Effect) {
        _stubbedEffectFlow.value = effect
    }

    // endregion

    init {
        controllerLog.log { ControllerEvent.Created(tag, controllerStart.logName) }
        if (controllerStart is ControllerStart.Immediately) {
            start()
        }
    }

    companion object {
        internal fun <Action, State, Effect> createMutatorContext(
            stateAccessor: () -> State,
            actionFlow: Flow<Action>,
            effectEmitter: (Effect) -> Unit
        ): EffectMutatorContext<Action, State, Effect> =
            object : EffectMutatorContext<Action, State, Effect> {
                override val currentState: State get() = stateAccessor()
                override val actions: Flow<Action> = actionFlow
                override fun offerEffect(effect: Effect) = effectEmitter(effect)
            }

        internal fun <Effect> createReducerContext(
            emitter: (Effect) -> Unit
        ): EffectReducerContext<Effect> = object : EffectReducerContext<Effect> {
            override fun offerEffect(effect: Effect) = emitter(effect)
        }

        internal fun <Effect> createTransformerContext(
            emitter: (Effect) -> Unit
        ): EffectTransformerContext<Effect> = object : EffectTransformerContext<Effect> {
            override fun offerEffect(effect: Effect) = emitter(effect)
        }
    }
}