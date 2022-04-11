package at.florianschuster.control

import kotlinx.coroutines.FlowPreview
import org.jetbrains.annotations.TestOnly

/**
 * A stub of a [Controller] for view testing.
 */
interface ControllerStub<Action, State> : Controller<Action, State> {

    /**
     * The [Action]'s dispatched to the [Controller] as ordered [List].
     * Use this to verify if view bindings trigger the correct [Action]'s.
     */
    val dispatchedActions: List<Action>

    /**
     * Emits a new [State] for [Controller.state].
     * Use this to verify if [State] is correctly bound to a view.
     */
    fun emitState(state: State)
}

/**
 * Converts an [Controller] to an [ControllerStub] for view testing.
 * Once converted, the [Controller] is stubbed and cannot be un-stubbed.
 *
 * Custom implementations of [Controller] cannot be stubbed.
 */
@FlowPreview
@TestOnly
fun <Action, State> Controller<Action, State>.toStub(): ControllerStub<Action, State> {
    require(this is ControllerImplementation<Action, *, State, *>) {
        "Cannot stub a custom implementation of a Controller."
    }
    if (!stubEnabled) {
        controllerLog.log { ControllerEvent.Stub(tag) }
        stubEnabled = true
    }
    return this
}

/**
 * A stub of a [EffectController] for view testing.
 */
interface EffectControllerStub<Action, State, Effect> : ControllerStub<Action, State> {

    /**
     * Emits a new [Effect] for [EffectController.effects].
     * Use this to verify if [Effect] is correctly bound to a view.
     */
    fun emitEffect(effect: Effect)
}

/**
 * Converts an [EffectController] to an [EffectControllerStub] for view testing.
 * Once converted, the [EffectController] is stubbed and cannot be un-stubbed.
 *
 * Custom implementations of [EffectController] cannot be stubbed.
 */
@FlowPreview
@TestOnly
fun <Action, State, Effect> EffectController<Action, State, Effect>.toStub(): EffectControllerStub<Action, State, Effect> {
    require(this is ControllerImplementation<Action, *, State, Effect>) {
        "Cannot stub a custom implementation of a EffectController."
    }
    if (!stubEnabled) {
        controllerLog.log { ControllerEvent.Stub(tag) }
        stubEnabled = true
    }
    return this
}
