package at.florianschuster.control

/**
 * Creates a default tag that a [ControllerImplementation] uses for debugging.
 */
@Suppress("NOTHING_TO_INLINE")
internal inline fun defaultTag(): String {
    val stackTrace = Throwable().stackTrace
    check(stackTrace.size >= 2) { "Stacktrace didn't have enough elements." }
    return stackTrace[1].className.split("$").first().split(".").last()
}