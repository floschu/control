package at.florianschuster.control

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlin.coroutines.ContinuationInterceptor

/**
 * An [EffectProvider] provides a [Flow] of (side-)effects that can happen during a
 * mutation in [Mutator] or a state reduction in [Reducer].
 */
interface EffectProvider<Effect> {

    /**
     * [Flow] of [Effect]s. Use this to collect [Effect] emissions.
     * The [Flow] is received in a fan-out fashion: one emission is just collected once.
     */
    val effects: Flow<Effect>
}

/**
 * An [EffectEmitter] emits side-effects.
 *
 * This is implemented by the respective context's of [EffectMutator], [EffectReducer]
 * and [EffectTransformer].
 */
interface EffectEmitter<Effect> {

    /**
     * Produces an [Effect].
     */
    fun emitEffect(effect: Effect)
}

/**
 * A [Controller] that provides a [Flow] of [Effect]'s via [EffectProvider].
 *
 * Before using this, make sure to look into the [Controller] documentation.
 */
interface EffectController<Action, Mutation, State, Effect> :
    Controller<Action, Mutation, State>,
    EffectProvider<Effect>

/**
 * Creates an [EffectController] bound to the [CoroutineScope] via [ControllerImplementation].
 *
 * The principle of the created state machine is the same as with
 * [CoroutineScope.createController].
 *
 * An [Effect] can be produced either in [mutator], [reducer], [actionsTransformer],
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
    controllerLog: ControllerLog = ControllerLog.default,

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
    dispatcher: CoroutineDispatcher = coroutineContext[ContinuationInterceptor] as CoroutineDispatcher
): EffectController<Action, Mutation, State, Effect> = ControllerImplementation(
    scope = this, dispatcher = dispatcher, controllerStart = controllerStart,

    initialState = initialState, mutator = mutator, reducer = reducer,
    actionsTransformer = actionsTransformer,
    mutationsTransformer = mutationsTransformer,
    statesTransformer = statesTransformer,

    tag = tag, controllerLog = controllerLog
)

/**
 * A [Mutator] used in a [EffectController] that is able to produce effects.
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
 * A [Reducer] used in a [EffectController]  that is able to produce effects.
 */
typealias EffectReducer<Mutation, State, Effect> =
    EffectReducerContext<Effect>.(mutation: Mutation, previousState: State) -> State

/**
 * A [ReducerContext] that additionally provides the functionality of an [EffectEmitter].
 */
interface EffectReducerContext<Effect> : ReducerContext, EffectEmitter<Effect>

/**
 * A [Transformer] used in a [EffectController]  that is able to produce effects.
 */
typealias EffectTransformer<Emission, Effect> =
    EffectTransformerContext<Effect>.(emissions: Flow<Emission>) -> Flow<Emission>

/**
 * A [TransformerContext] that additionally provides the functionality of an [EffectEmitter].
 */
interface EffectTransformerContext<Effect> : TransformerContext, EffectEmitter<Effect>