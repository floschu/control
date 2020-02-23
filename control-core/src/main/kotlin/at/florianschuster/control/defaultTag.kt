package at.florianschuster.control

/**
 * Creates a default tag that a [ControllerImplementation] uses for debugging.
 */
@Suppress("NOTHING_TO_INLINE")
internal inline fun defaultTag(suffix: String = defaultTagSuffix): String {
    val stackTrace = Throwable().stackTrace
    check(stackTrace.size >= 2) { "Stacktrace didn't have enough elements." }
    val className = stackTrace[1].className.split("$").first().split(".").last()
    return "${className}$suffix"
}

internal const val defaultTagSuffix = "_controller"