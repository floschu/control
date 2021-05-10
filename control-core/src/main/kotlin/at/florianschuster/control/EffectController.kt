package at.florianschuster.control

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

/**
 * A [Controller] that provides a [Flow] of [Effect]'s that can happen during a
 * mutation in [EffectMutator], a state reduction in [EffectReducer] or a
 * transformation in [EffectTransformer].
 *
 * An [Effect] could be a one-of UI notification such as a Toast or a Snackbar
 * on Android.
 *
 * Before using this, make sure to look into the [Controller] documentation.
 */
interface EffectController<Action, State, Effect> : Controller<Action, State> {

    /**
     * [Flow] of [Effect]s. Use this to collect [Effect] emissions.
     * The [Flow] is received in a fan-out fashion. One emission will be
     * emitted to one collector only.
     */
    val effects: Flow<Effect>
}

/**
 * Creates an [EffectController] bound to the [CoroutineScope] via [ControllerImplementation].
 *
 * The principle of the created state machine is the same as with
 * [CoroutineScope.createController].
 *
 * An [Effect] can be emitted either in [mutator], [reducer], [actionsTransformer],
 * [mutationsTransformer] or [statesTransformer].
 */
@ExperimentalCoroutinesApi
@FlowPreview
fun <Action, Mutation, State, Effect> CoroutineScope.createEffectController(

    /**
     * The initial [State] for the internal state machine.
     */
    initialState: State,
    /**
     * See [EffectMutator].
     */
    mutator: EffectMutator<Action, Mutation, State, Effect> = { _ -> emptyFlow() },
    /**
     * See [EffectReducer].
     */
    reducer: EffectReducer<Mutation, State, Effect> = { _, previousState -> previousState },

    /**
     * See [EffectTransformer].
     */
    actionsTransformer: EffectTransformer<Action, Effect> = { it },
    mutationsTransformer: EffectTransformer<Mutation, Effect> = { it },
    statesTransformer: EffectTransformer<State, Effect> = { it },

    /**
     * Used for [ControllerLog] and as [CoroutineName] for the internal state machine.
     */
    tag: String = defaultTag(),
    /**
     * Log configuration for [ControllerEvent]s. See [ControllerLog].
     */
    controllerLog: ControllerLog = ControllerLog.None,

    /**
     * When the internal state machine [Flow] should be started. See [ControllerStart].
     */
    controllerStart: ControllerStart = ControllerStart.Lazy,

    /**
     * Override to launch the internal state machine [Flow] in a different [CoroutineDispatcher]
     * than the one used in the [CoroutineScope.coroutineContext].
     *
     * [Mutator] and [Reducer] will run on this [CoroutineDispatcher].
     */
    dispatcher: CoroutineDispatcher = defaultScopeDispatcher()
): EffectController<Action, State, Effect> = ControllerImplementation(
    scope = this, dispatcher = dispatcher, controllerStart = controllerStart,

    initialState = initialState, mutator = mutator, reducer = reducer,
    actionsTransformer = actionsTransformer,
    mutationsTransformer = mutationsTransformer,
    statesTransformer = statesTransformer,

    tag = tag, controllerLog = controllerLog
)

/**
 * An [EffectEmitter] can emit side-effects.
 *
 * This is implemented by the respective context's of [EffectMutator], [EffectReducer]
 * and [EffectTransformer].
 */
interface EffectEmitter<Effect> {

    /**
     * Emits an [Effect].
     */
    fun emitEffect(effect: Effect)
}

/**
 * A [Mutator] used in a [EffectController] that is able to emit effects.
 */
typealias EffectMutator<Action, Mutation, State, Effect> =
    EffectMutatorContext<Action, State, Effect>.(action: Action) -> Flow<Mutation>

/**
 * A [MutatorContext] that additionally provides the functionality of an [EffectEmitter].
 * This context is used for an [EffectMutator].
 */
interface EffectMutatorContext<Action, State, Effect> :
    MutatorContext<Action, State>, EffectEmitter<Effect>

/**
 * A [Reducer] used in a [EffectController]  that is able to emit effects.
 */
typealias EffectReducer<Mutation, State, Effect> =
    EffectReducerContext<Effect>.(mutation: Mutation, previousState: State) -> State

/**
 * A [ReducerContext] that additionally provides the functionality of an [EffectEmitter].
 */
interface EffectReducerContext<Effect> : ReducerContext, EffectEmitter<Effect>

/**
 * A [Transformer] used in a [EffectController]  that is able to emit effects.
 */
typealias EffectTransformer<Emission, Effect> =
    EffectTransformerContext<Effect>.(emissions: Flow<Emission>) -> Flow<Emission>

/**
 * A [TransformerContext] that additionally provides the functionality of an [EffectEmitter].
 */
interface EffectTransformerContext<Effect> : TransformerContext, EffectEmitter<Effect>