package at.florianschuster.control

/**
 * Generates a default tag that can be used by [ControllerImplementation]
 * to for logging and as coroutine name.
 */
internal expect fun defaultTag(): String