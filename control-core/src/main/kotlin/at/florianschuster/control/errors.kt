package at.florianschuster.control

/**
 * Errors that can happen in a [Controller].
 */
sealed class ControllerError(
    message: String,
    cause: Throwable
) : RuntimeException(message, cause) {

    /**
     * Error during [Mutator].
     */
    internal class Mutate(
        tag: String,
        action: String,
        cause: Throwable
    ) : ControllerError("Mutator error in $tag, action = $action", cause)

    /**
     * Error during [Reducer].
     */
    internal class Reduce(
        tag: String,
        previousState: String,
        mutation: String,
        cause: Throwable
    ) : ControllerError(
        message = "Reducer error in $tag, previousState = $previousState, mutation = $mutation",
        cause = cause
    )
}