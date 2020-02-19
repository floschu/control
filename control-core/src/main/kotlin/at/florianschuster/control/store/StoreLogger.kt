package at.florianschuster.control.store

import kotlinx.coroutines.flow.Flow

/**
 * Configuration to define how [StoreLogger.Event]'s are logged by a [StoreImplementation].
 */
sealed class StoreLogger {

    internal open val logger: ((message: String) -> Unit)? = null

    internal fun log(tag: String, event: Event) {
        logger?.invoke(defaultMessageCreator(tag, event))
    }

    /**
     * No logging.
     */
    object None : StoreLogger()

    /**
     * Uses [println] to log.
     */
    object Println : StoreLogger() {
        override val logger: (message: String) -> Unit = ::println
    }

    /**
     * Uses a custom [logger] to log.
     */
    data class Custom(override val logger: (message: String) -> Unit) : StoreLogger()

    /**
     * All events that are logged in a [StoreImplementation].
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
         * The default [StoreLogger] that is used by a [StoreImplementation].
         * Set this to change the logger for all [StoreImplementation]'s that do not specify one.
         */
        var default: StoreLogger = None

        /**
         * The default message creator for all [StoreLogger] types.
         * Override this to customize your logs.
         */
        var defaultMessageCreator: (tag: String, event: Event) -> String = { tag, event ->
            "||| <control> ||| $tag -> ${event.message} |||"
        }
    }
}