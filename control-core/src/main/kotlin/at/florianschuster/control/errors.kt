package at.florianschuster.control

/**
 * Errors that can happen in a [Controller].
 */
internal sealed class ControllerError(
    message: String,
    cause: Throwable
) : RuntimeException(message, cause) {

    /**
     * Error during [Mutator].
     */
    class Mutate(
        tag: String,
        action: String,
        cause: Throwable
    ) : ControllerError(message = "Mutator error in $tag, action = $action", cause = cause)

    /**
     * Error during [Reducer].
     */
    class Reduce(
        tag: String,
        previousState: String,
        mutation: String,
        cause: Throwable
    ) : ControllerError(
        message = "Reducer error in $tag, previousState = $previousState, mutation = $mutation",
        cause = cause
    )

    /**
     * Error during [EffectEmitter.emitEffect].
     */
    class Effect(
        tag: String,
        effect: String
    ) : ControllerError(
        message = "Effect error in $tag, effect = $effect",
        cause = IllegalStateException(
            "Capacity for effects has been reached. Either too many effects have been triggered " +
                "or they might not be consumed."
        )
    )
}
