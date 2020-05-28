package at.florianschuster.control

import kotlinx.coroutines.CoroutineScope

/**
 * Options for [Controller] builder such as [CoroutineScope.createController] or
 * [ManagedController] to define when the internal state machine should be started.
 */
sealed class ControllerStart {

    /**
     * The state machine is started once [Controller.state], [Controller.currentState] or
     * [Controller.dispatch] are accessed.
     */
    object Lazy : ControllerStart()

    /**
     * The state machine is iImmediately started once the [Controller] is built.
     */
    object Immediately : ControllerStart()

    /**
     * The state machine is started once [ManagedController.start] is called.
     */
    internal object Managed : ControllerStart()
}