package at.florianschuster.control

/**
 * All events that are logged in a [ControllerImplementation].
 */
sealed class ControllerEvent(
    private val tag: String,
    private val message: String
) {

    /**
     * When the [ControllerImplementation] is created.
     */
    class Created internal constructor(
        tag: String
    ) : ControllerEvent(tag, "created")

    /**
     * When the [ControllerImplementation.state] stream is started.
     */
    class Started internal constructor(
        tag: String
    ) : ControllerEvent(tag, "state stream started")

    /**
     * When the [ControllerImplementation] receives an [Action].
     */
    class Action internal constructor(
        tag: String, action: String
    ) : ControllerEvent(tag, "action: $action")

    /**
     * When the [ControllerImplementation] mutator produces a new [Mutation].
     */
    class Mutation internal constructor(
        tag: String, mutation: String
    ) : ControllerEvent(tag, "mutation: $mutation")

    /**
     * When the [ControllerImplementation] reduces a new [State].
     */
    class State internal constructor(
        tag: String, state: String
    ) : ControllerEvent(tag, "state: $state")

    /**
     * When an error happens in [ControllerImplementation] stream.
     */
    class Error internal constructor(
        tag: String, cause: ControllerError
    ) : ControllerEvent(tag, "error: $cause")

    /**
     * When the [ControllerImplementation.stub] is set to enabled.
     */
    class Stub internal constructor(
        tag: String
    ) : ControllerEvent(tag, "is now stubbed")

    /**
     * When the [ControllerImplementation] stream is completed.
     */
    class Completed internal constructor(
        tag: String
    ) : ControllerEvent(tag, "completed")

    override fun toString(): String {
        return "||| <control> ||| $tag -> $message |||"
    }
}
