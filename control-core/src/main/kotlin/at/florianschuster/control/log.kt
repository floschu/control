package at.florianschuster.control

/**
 * A logger used by [ControllerLog] to log [ControllerEvent]'s.
 */
typealias Logger = LoggerContext.(message: String) -> Unit

/**
 * The context of a [Logger]. Contains the [ControllerEvent] that is being logged.
 */
interface LoggerContext {
    val event: ControllerEvent
}

/**
 * Configuration to define how [ControllerEvent]'s are logged by a [Controller].
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
    class Custom(override val logger: Logger) : ControllerLog()
}

internal fun createLoggerContext(event: ControllerEvent) = object : LoggerContext {
    override val event: ControllerEvent = event
}

internal fun ControllerLog.log(eventCreator: () -> ControllerEvent) {
    val logger = logger ?: return
    val eventToLog = eventCreator()
    createLoggerContext(eventToLog).logger(eventToLog.toString())
}
