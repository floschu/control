package at.florianschuster.control

/**
 * Configuration to define how a [Controller] logs its state errors and operations.
 */
sealed class ControlLogConfiguration {

    internal fun createMessage(
        tag: String,
        function: String,
        message: String? = null
    ) = "||| <control> ||| $tag -> $function${if (message != null) ": $message" else ""} |||"

    internal abstract fun log(function: String, error: Throwable)
    internal abstract fun log(function: String, message: String?)

    /**
     * No logging.
     */
    object None : ControlLogConfiguration() {
        override fun log(function: String, message: String?) = Unit
        override fun log(function: String, error: Throwable) = Unit
    }

    /**
     * Simple logging, uses [println]. Only logs errors and operation names.
     */
    data class Simple(val tag: String) : ControlLogConfiguration() {
        override fun log(function: String, message: String?) {
            println(createMessage(tag, function))
        }

        override fun log(function: String, error: Throwable) {
            println(createMessage(tag, function, "$error"))
        }
    }

    /**
     * Elaborate logging, uses [println]. Logs errors and operation names and contents.
     */
    data class Elaborate(val tag: String) : ControlLogConfiguration() {
        override fun log(function: String, message: String?) {
            println(createMessage(tag, function, message))
        }

        override fun log(function: String, error: Throwable) {
            println(createMessage(tag, function, "$error"))
        }
    }

    /**
     * A custom logger.
     * Operations can be logged through [operations]. Errors can be logged through [errors].
     */
    data class Custom(
        val tag: String,
        val elaborate: Boolean = true,
        val operations: ((String) -> Unit)? = null,
        val errors: ((Throwable) -> Unit)? = null
    ) : ControlLogConfiguration() {
        override fun log(function: String, message: String?) {
            operations?.invoke(createMessage(tag, function, if (elaborate) message else null))
        }

        override fun log(function: String, error: Throwable) {
            if (errors != null) errors.invoke(error) else log(function, "$error")
        }
    }

    companion object {

        /**
         * The default [ControlLogConfiguration] that is used by a [Controller].
         * Set this to change it for all [Controller]'s that do not specify a specific configuration.
         */
        var default: ControlLogConfiguration = None
    }
}
