package at.florianschuster.control

/**
 * Configuration to define how a [Controller] logs its state errors and operations.
 */
sealed class LogConfiguration {

    internal fun createMessage(
        tag: String,
        function: String,
        message: String? = null
    ) = "||| <control> ||| $tag -> $function${if (message != null) ": $message" else ""} |||"

    internal open fun log(function: String, error: Throwable) = log(function, "$error")
    internal open fun log(function: String, message: String?) = Unit

    /**
     * No logging.
     */
    object None : LogConfiguration()

    /**
     * Simple logging, uses [println]. Only logs errors and operation names.
     */
    data class Simple(val tag: String) : LogConfiguration() {
        override fun log(function: String, message: String?) {
            println(createMessage(tag, function))
        }
    }

    /**
     * Elaborate logging, uses [println]. Logs errors and operation names and contents.
     */
    data class Elaborate(val tag: String) : LogConfiguration() {
        override fun log(function: String, message: String?) {
            println(createMessage(tag, function, message))
        }
    }

    /**
     * A custom logger can be attached.
     * Operations can be logged through [operations]. Errors can be logged through [errors].
     */
    data class Custom(
        val tag: String,
        val elaborate: Boolean = true,
        val operations: ((String) -> Unit)? = null,
        val errors: ((Throwable) -> Unit)? = null
    ) : LogConfiguration() {
        override fun log(function: String, message: String?) {
            operations?.invoke(createMessage(tag, function, if (elaborate) message else null))
        }

        override fun log(function: String, error: Throwable) {
            if (errors != null) errors.invoke(error) else super.log(function, error)
        }
    }

    companion object {

        /**
         * The default [LogConfiguration] that is used by a [Controller].
         * Set this to change it for all [Controller]'s.
         */
        var DEFAULT: LogConfiguration = None
    }
}
