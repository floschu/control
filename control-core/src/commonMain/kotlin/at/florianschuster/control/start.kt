package at.florianschuster.control

import kotlinx.coroutines.CoroutineScope

/**
 * Options for [Controller] builder such as [CoroutineScope.createController]
 * to define when the internal state machine should be started.
 */
sealed class ControllerStart {

    internal abstract val logName: String

    /**
     * The state machine is started once [Controller.state] or [Controller.dispatch]
     * are accessed.
     */
    data object Lazy : ControllerStart() {
        override val logName: String = "Lazy"
    }

    /**
     * The state machine is iImmediately started once the [Controller] is built.
     */
    data object Immediately : ControllerStart() {
        override val logName: String = "Immediately"
    }

    /**
     * The state machine is started once [ControllerImplementation.start] is called.
     */
    internal data object Manual : ControllerStart() {
        override val logName: String = "Manual"
    }
}
