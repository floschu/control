package at.florianschuster.control

/**
 * A logger used by [ControllerLog] to log [ControllerEvent]'s.
 */
typealias Logger = LoggerScope.(message: String) -> Unit

/**
 * The scope of a [Logger]. Contains the [ControllerEvent] that is being logged.
 */
interface LoggerScope {
    val event: ControllerEvent
}

/**
 * Configuration to define how [ControllerEvent]'s are logged by a [ControllerImplementation].
 */
sealed class ControllerLog {

    internal open val logger: Logger? = null

    /**
     * No logging.
     */
    object None : ControllerLog()

    /**
     * Uses [println] to log.
     */
    object Println : ControllerLog() {
        override val logger: Logger = { message -> println(message) }
    }

    /**
     * Uses a custom [Logger] to log.
     */
    data class Custom(override val logger: Logger) : ControllerLog()

    companion object {

        /**
         * The default [ControllerLog] that is used by a [ControllerImplementation].
         * Set this to change the logger for all [ControllerImplementation]'s that do not specify one.
         */
        var default: ControllerLog = None
    }
}

internal fun ControllerLog.log(event: ControllerEvent) {
    logger?.invoke(loggerScope(event), event.toString())
}

internal fun loggerScope(event: ControllerEvent) = object : LoggerScope {
    override val event: ControllerEvent = event
}