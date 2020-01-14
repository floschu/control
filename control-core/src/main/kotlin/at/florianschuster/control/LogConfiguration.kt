package at.florianschuster.control

/**
 * TODO
 */
sealed class LogConfiguration {

    protected fun createMessage(
        tag: String,
        function: String,
        message: String? = null
    ) = "||||| <control> ||||| $tag -> $function${if (message != null) ": $message" else ""} |||||"

    abstract fun log(function: String, message: String?)

    object None : LogConfiguration() {
        override fun log(function: String, message: String?) = Unit
    }

    data class Simple(val tag: String) : LogConfiguration() {
        override fun log(function: String, message: String?) {
            println(createMessage(tag, function))
        }
    }

    data class Elaborate(val tag: String) : LogConfiguration() {
        override fun log(function: String, message: String?) {
            println(createMessage(tag, function, message))
        }
    }

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
