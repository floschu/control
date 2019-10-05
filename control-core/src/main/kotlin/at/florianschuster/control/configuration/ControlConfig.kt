package at.florianschuster.control.configuration

internal object ControlConfig {
    val configuration = ControlConfiguration()

    internal fun handleError(throwable: Throwable) {
        when {
            configuration.escalateCrashes -> throw throwable
            else -> configuration.errorLogger?.invoke(throwable)
        }
    }

    internal inline fun log(operation: () -> Operation) {
        if (!configuration.operationLoggerEnabled) return
        configuration.operationLogger?.invoke(operation().toString())
    }
}
