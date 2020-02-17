package at.florianschuster.control.store

/**
 * Configuration to define how [StoreLogger.Event]'s are logged by a [StoreImplementation].
 */
sealed class StoreLogger {

    internal open val loggingProvider: ((tag: String, message: String) -> Unit)? = null

    internal inline fun log(tag: String, event: () -> Event) {
        loggingProvider?.invoke(tag, event().run { "||| <control> ||| $tag -> $message |||" })
    }

    /**
     * No logging.
     */
    object None : StoreLogger()

    /**
     * Uses [println] to log.
     */
    object Println : StoreLogger() {
        override val loggingProvider: ((tag: String, message: String) -> Unit)? = { _, message ->
            println(message)
        }
    }

    /**
     * Uses a log provider [logger] to log.
     */
    data class Custom(val logger: (tag: String, message: String) -> Unit) : StoreLogger() {
        override val loggingProvider: ((String, String) -> Unit)? = logger
    }

    /**
     * All events that can be logged in a [StoreImplementation].
     */
    sealed class Event(val message: String) {
        object Created : Event("created")
        object Started : Event("state stream started")
        class Action(action: String) : Event("action: $action")
        class Mutation(mutation: String) : Event("mutation: $mutation")
        class State(state: String) : Event("state: $state")
        class Error(cause: Throwable) : Event("error: $cause")
        class Stub(enabled: Boolean) : Event("stub: enabled = $enabled")
        object Destroyed : Event("destroyed")
    }

    companion object {

        /**
         * The default [StoreLogger] that is used by a [StoreImplementation].
         * Set this to change the logger for all [StoreImplementation]'s that do not specify one.
         */
        var default: StoreLogger = None
    }
}