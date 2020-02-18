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
     * All events that can be logged in a [StoreImplementation].
     */
    sealed class Event(val message: String) {

        /**
         * When [StoreImplementation] is created.
         */
        object Created : Event("created")

        /**
         * When [StoreImplementation.state] is started.
         */
        object Started : Event("state stream started")

        /**
         * When [StoreImplementation] receives an action in [StoreImplementation.mutator].
         */
        class Action(action: String) : Event("action: $action")

        /**
         * When [StoreImplementation] receives a mutation in [StoreImplementation.reducer].
         */
        class Mutation(mutation: String) : Event("mutation: $mutation")

        /**
         * When [StoreImplementation] produces a new state in [StoreImplementation.reducer].
         */
        class State(state: String) : Event("state: $state")

        /**
         * When an error is thrown in [StoreImplementation].
         */
        class Error(cause: Throwable) : Event("error: $cause")

        /**
         * When [StoreImplementation.stubEnabled] is toggled.
         */
        class Stub(enabled: Boolean) : Event("stub: enabled = $enabled")

        /**
         * When [StoreImplementation] is destroyed (= the state [Flow] is completed).
         */
        object Destroyed : Event("destroyed")
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