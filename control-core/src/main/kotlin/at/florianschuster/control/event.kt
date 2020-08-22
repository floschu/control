package at.florianschuster.control

/**
 * All events that are logged in the internal state machine within a [Controller].
 */
sealed class ControllerEvent(
    private val tag: String,
    private val message: String
) {

    /**
     * When the implementation is created.
     */
    class Created internal constructor(
        tag: String, controllerStart: String
    ) : ControllerEvent(tag, "created with controllerStart: $controllerStart")

    /**
     * When the state machine is started.
     */
    class Started internal constructor(
        tag: String
    ) : ControllerEvent(tag, "state stream started")

    /**
     * When the state machine receives an [Action].
     */
    class Action internal constructor(
        tag: String, action: String
    ) : ControllerEvent(tag, "action: $action")

    /**
     * When the [Mutator] produces a new [Mutation].
     */
    class Mutation internal constructor(
        tag: String, mutation: String
    ) : ControllerEvent(tag, "mutation: $mutation")

    /**
     * When the [Reducer] reduces a new [State].
     */
    class State internal constructor(
        tag: String, state: String
    ) : ControllerEvent(tag, "state: $state")

    /**
     * When an [Effect] is produced by the [EffectController].
     */
    class Effect internal constructor(
        tag: String, effect: String
    ) : ControllerEvent(tag, "effect: $effect")

    /**
     * When an error happens during the execution of the internal state machine.
     */
    class Error internal constructor(
        tag: String, cause: ControllerError
    ) : ControllerEvent(tag, "error: $cause")

    /**
     * When the [ControllerStub] is enabled.
     */
    class Stub internal constructor(
        tag: String
    ) : ControllerEvent(tag, "is now stubbed")

    /**
     * When the internal state machine completes.
     */
    class Completed internal constructor(
        tag: String
    ) : ControllerEvent(tag, "completed")

    override fun toString(): String = "||| <control> ||| $tag -> $message |||"
}
