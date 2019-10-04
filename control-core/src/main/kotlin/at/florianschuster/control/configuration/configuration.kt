package at.florianschuster.control.configuration

/**
 * Configures control. See [ControlConfiguration] for further details.
 */
fun configureControl(configuration: ControlConfiguration.() -> Unit) {
    ControlConfig.configuration.apply(configuration)
    ControlConfig.log { Operation.ControlConfigured }
}

class ControlConfiguration {
    internal var escalateCrashes: Boolean = false
    internal var errorLogger: ((Throwable) -> Unit)? = null
    internal var operationLoggerEnabled = false
    internal var operationLogger: ((String) -> Unit)? = null

    /**
     * When set to true, certain control components crash on errors.
     * Set this to true for debug builds for example.
     */
    fun crashes(escalate: Boolean) {
        this.escalateCrashes = escalate
    }

    /**
     * Handles errors that happen in control.
     */
    fun errorLogger(logger: (Throwable) -> Unit) {
        this.errorLogger = logger
    }

    /**
     * Logs certain control [Operation]'s.
     */
    fun operationLogger(enabled: Boolean = true, logger: (String) -> Unit) {
        this.operationLoggerEnabled = enabled
        this.operationLogger = logger
    }
}
