package at.florianschuster.control

/**
 * Configuration to define how a [Controller] logs its errors and operations.
 */
sealed class LogConfiguration {

    protected fun createMessage(
        tag: String,
        function: String,
        message: String? = null
    ) = "||||| <control> ||||| $tag -> $function${if (message != null) ": $message" else ""} |||||"

    internal abstract fun log(function: String, message: String?)

    /**
     * No logging.
     */
    object None : LogConfiguration() {
        override fun log(function: String, message: String?) = Unit
    }

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
     * A custom logger can be attached through [logger].
     */
    data class Custom(
        val tag: String,
        val elaborate: Boolean = true,
        val logger: (String) -> Unit
    ) : LogConfiguration() {
        override fun log(function: String, message: String?) {
            logger(createMessage(tag, function, if (elaborate) message else null))
        }
    }
}
