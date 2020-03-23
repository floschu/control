package at.florianschuster.control

import kotlinx.coroutines.flow.Flow

/**
 * Modes for launching the [ControllerImplementation.state] [Flow].
 */
sealed class LaunchMode {

    /**
     * [ControllerImplementation.state] is created on init of [ControllerImplementation].
     */
    object Immediate : LaunchMode()

    /**
     * [ControllerImplementation.state] is created once [ControllerImplementation.state],
     * [ControllerImplementation.currentState] or [ControllerImplementation.dispatch] are accessed.
     */
    object OnAccess : LaunchMode()

    companion object {

        /**
         * The default [LaunchMode] used by [ControllerImplementation]
         */
        var default: LaunchMode = OnAccess
    }
}