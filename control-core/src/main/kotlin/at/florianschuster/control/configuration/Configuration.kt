package at.florianschuster.control.configuration

/**
 * Configures control. See [ControlConfiguration].
 */
fun configureControl(configuration: ControlConfiguration.() -> Unit) {
    Control.configuration.apply(configuration)
    Control.log { Operation.ControlConfigured }
}

/**
 * Configuration proxy class.
 */
class ControlConfiguration {
    internal var errorLogger: ((Throwable) -> Unit)? = null
    internal var operationLoggerEnabled = false
    internal var operationLogger: ((String) -> Unit)? = null

    /**
     * Logs [Throwable]'s happening in certain control components.
     */
    fun errors(logger: (Throwable) -> Unit) {
        this.errorLogger = logger
    }

    /**
     * Logs certain control [Operation]'s with [logger] if [loggingEnabled] is true.
     */
    fun operations(loggingEnabled: Boolean = true, logger: (String) -> Unit) {
        this.operationLoggerEnabled = loggingEnabled
        this.operationLogger = logger
    }
}
