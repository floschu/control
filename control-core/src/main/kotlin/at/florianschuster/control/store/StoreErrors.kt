package at.florianschuster.control.store

/**
 * Errors than can occur in a [StoreImplementation].
 */
sealed class StoreError(
    message: String,
    cause: Throwable
) : RuntimeException(message, cause) {

    /**
     * See [StoreImplementation.mutator].
     */
    class Mutator internal constructor(
        tag: String,
        action: String,
        cause: Throwable
    ) : StoreError("Mutator error in $tag, action = $action", cause)

    /**
     * See [StoreImplementation.reducer].
     */
    class Reducer internal constructor(
        tag: String,
        previousState: String,
        mutation: String,
        cause: Throwable
    ) : StoreError(
        message = "Reducer error in $tag, previousState = $previousState, mutation = $mutation",
        cause = cause
    )
}