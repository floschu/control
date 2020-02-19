package at.florianschuster.control

/**
 * Configuration to define how [ControllerLog.Event]'s are logged by a [ControllerImplementation].
 */
sealed class ControllerLog {

    internal open val logger: ((message: String) -> Unit)? = null

    internal fun log(tag: String, event: Event) {
        logger?.invoke(defaultMessageCreator(tag, event))
    }

    /**
     * No logging.
     */
    object None : ControllerLog()

    /**
     * Uses [println] to log.
     */
    object Println : ControllerLog() {
        override val logger: (message: String) -> Unit = ::println
    }

    /**
     * Uses a custom [logger] to log.
     */
    data class Custom(override val logger: (message: String) -> Unit) : ControllerLog()

    /**
     * All events that are logged in a [ControllerImplementation].
     */
    sealed class Event(val message: String) {
        internal object Created : Event("created")
        internal object Started : Event("state stream started")
        internal class Action(action: String) : Event("action: $action")
        internal class Mutation(mutation: String) : Event("mutation: $mutation")
        internal class State(state: String) : Event("state: $state")
        internal class Error(cause: Throwable) : Event("error: $cause")
        internal class Stub(enabled: Boolean) : Event("stub: enabled = $enabled")
        internal object Destroyed : Event("destroyed")
    }

    companion object {

        /**
         * The default [ControllerLog] that is used by a [ControllerImplementation].
         * Set this to change the logger for all [ControllerImplementation]'s that do not specify one.
         */
        var default: ControllerLog = None

        /**
         * The default message creator for all [ControllerLog] types.
         * Override this to customize your logs.
         */
        var defaultMessageCreator: (tag: String, event: Event) -> String = { tag, event ->
            "||| <control> ||| $tag -> ${event.message} |||"
        }
    }
}