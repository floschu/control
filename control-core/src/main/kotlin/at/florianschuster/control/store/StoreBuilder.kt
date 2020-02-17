package at.florianschuster.control.store

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlin.coroutines.ContinuationInterceptor

/**
 * Creates a [Store] bound to the [CoroutineScope] via [StoreImplementation].
 *
 * The [StoreImplementation.state] [Flow] is launched once [StoreImplementation.state],
 * [StoreImplementation.currentState] or [StoreImplementation.dispatch] are accessed.
 *
 * Per default, the [CoroutineDispatcher] of [CoroutineScope] is used to launch the
 * [StoreImplementation.state] [Flow]. If another [CoroutineDispatcher] should be used,
 * override [dispatcher].
 */
@ExperimentalCoroutinesApi
@FlowPreview
fun <Action, Mutation, State> CoroutineScope.createStore(

    /**
     * Set as [CoroutineName] for the [StoreImplementation.state] context.
     * Also used for logging if enabled via [storeLogger].
     */
    tag: String,

    /**
     * The [StoreImplementation] is started with this [State].
     */
    initialState: State,

    /**
     * See [Mutator].
     */
    mutator: Mutator<Action, Mutation> = { emptyFlow() },

    /**
     * See [Reducer].
     */
    reducer: Reducer<Mutation, State> = { previousState, _ -> previousState },

    /**
     * See [Transformer].
     */
    actionsTransformer: Transformer<Action> = { it },
    mutationsTransformer: Transformer<Mutation> = { it },
    statesTransformer: Transformer<State> = { it },

    /**
     * Override to launch [StoreImplementation.state] [Flow] in different [CoroutineDispatcher]
     * than the one used in the [CoroutineScope.coroutineContext].
     */
    dispatcher: CoroutineDispatcher = coroutineContext[ContinuationInterceptor] as CoroutineDispatcher,

    /**
     * Log configuration for the [StoreImplementation]. See [StoreLogger].
     */
    storeLogger: StoreLogger = StoreLogger.default
): Store<Action, Mutation, State> =
    StoreImplementation(
        scope = this, dispatcher = dispatcher,

        initialState = initialState, mutator = mutator, reducer = reducer,
        actionsTransformer = actionsTransformer,
        mutationsTransformer = mutationsTransformer,
        statesTransformer = statesTransformer,

        tag = tag, storeLogger = storeLogger
    )

/**
 * Creates a [Store] with [CoroutineScope.createStore] where [Action] == [Mutation].
 * The [Store] can only deal with synchronous state reductions without any asynchronous side-effects.
 */
@ExperimentalCoroutinesApi
@FlowPreview
fun <Action, State> CoroutineScope.createSynchronousStore(
    tag: String,

    initialState: State,
    reducer: Reducer<Action, State> = { previousState, _ -> previousState },

    actionsTransformer: Transformer<Action> = { it },
    statesTransformer: Transformer<State> = { it },

    dispatcher: CoroutineDispatcher = coroutineContext[ContinuationInterceptor] as CoroutineDispatcher,

    storeLogger: StoreLogger = StoreLogger.default
): Store<Action, Action, State> = createStore(
    dispatcher = dispatcher,

    initialState = initialState, mutator = ::flowOf, reducer = reducer,
    actionsTransformer = actionsTransformer,
    mutationsTransformer = { it },
    statesTransformer = statesTransformer,

    tag = tag, storeLogger = storeLogger
)

