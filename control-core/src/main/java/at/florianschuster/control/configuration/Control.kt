package at.florianschuster.control.configuration

/**
 * Internal configuration holder.
 */
internal object Control {
    val configuration = ControlConfiguration()

    /**
     * Logs [Throwable]'s internally.
     */
    internal fun log(throwable: Throwable) {
        configuration.errorLogger?.invoke(throwable)
    }

    /**
     * Logs [Operation]'s internally.
     */
    internal inline fun log(operation: () -> Operation) {
        if (!configuration.operationLoggerEnabled) return
        configuration.operationLogger?.invoke(operation().toString())
    }
}