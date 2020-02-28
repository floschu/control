package at.florianschuster.control

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
 * An implementation of [Controller].
 */
@ExperimentalCoroutinesApi
@FlowPreview
internal class ControllerImplementation<Action, Mutation, State>(
    private val scope: CoroutineScope,
    private val dispatcher: CoroutineDispatcher,

    private val initialState: State,
    private val mutator: Mutator<Action, State, Mutation>,
    private val reducer: Reducer<Mutation, State>,

    private val actionsTransformer: Transformer<Action>,
    private val mutationsTransformer: Transformer<Mutation>,
    private val statesTransformer: Transformer<State>,

    private val tag: String,
    private val controllerLog: ControllerLog
) : Controller<Action, Mutation, State> {

    private var initialized = false
    private val actionChannel = BroadcastChannel<Action>(1)
    private val stateChannel = ConflatedBroadcastChannel(initialState)
    private val controllerStub by lazy { ControllerStubImplementation<Action, State>(initialState) }

    override val state: Flow<State>
        get() = if (!stubEnabled) {
            if (!initialized) lazyInitialize()
            stateChannel.asFlow()
        } else {
            controllerStub.stateChannel.asFlow()
        }

    override val currentState: State
        get() = if (!stubEnabled) {
            if (!initialized) lazyInitialize()
            stateChannel.value
        } else {
            controllerStub.stateChannel.value
        }

    override fun dispatch(action: Action) {
        if (!stubEnabled) {
            if (!initialized) lazyInitialize()
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
        controllerLog.log(tag, ControllerLog.Event.Created)
    }

    private fun lazyInitialize() = kotlinx.atomicfu.locks.synchronized(this) {
        val actionFlow: Flow<Action> = actionChannel.asFlow()

        val mutationFlow: Flow<Mutation> = actionsTransformer(actionFlow)
            .flatMapMerge { action ->
                controllerLog.log(tag, ControllerLog.Event.Action(action.toString()))
                mutator(action, { currentState }, actionFlow).catch { cause ->
                    val error = Controller.Error.Mutator(tag, "$action", cause)
                    controllerLog.log(tag, ControllerLog.Event.Error(error))
                    throw error
                }
            }

        val stateFlow: Flow<State> = mutationsTransformer(mutationFlow)
            .scan(initialState) { previousState, mutation ->
                controllerLog.log(tag, ControllerLog.Event.Mutation(mutation.toString()))
                val reducedState = try {
                    reducer(previousState, mutation)
                } catch (cause: Throwable) {
                    val error =
                        Controller.Error.Reducer(tag, "$previousState", "$mutation", cause)
                    controllerLog.log(tag, ControllerLog.Event.Error(error))
                    throw error
                }
                controllerLog.log(tag, ControllerLog.Event.State(reducedState.toString()))
                reducedState
            }

        scope.launch(dispatcher + CoroutineName(tag)) {
            statesTransformer(stateFlow)
                .distinctUntilChanged()
                .onStart { controllerLog.log(tag, ControllerLog.Event.Started) }
                .onEach(stateChannel::send)
                .onCompletion { controllerLog.log(tag, ControllerLog.Event.Destroyed) }
                .collect()
        }

        initialized = true
    }
}