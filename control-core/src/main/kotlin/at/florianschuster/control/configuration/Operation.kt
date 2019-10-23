package at.florianschuster.control.configuration

/**
 * Used to log certain operations in the library.
 */
internal sealed class Operation(
    private val tag: String,
    private val message: String
) {
    internal object ControlConfigured : Operation("library", "was configured")

    internal class Initialized(tag: String, initialState: String) :
        Operation(tag, "initialized with $initialState")

    internal class Mutate(tag: String, action: String) :
        Operation(tag, "mutate with $action")

    internal class Reduce(tag: String, previousState: String, mutation: String, newState: String) :
        Operation(tag, "reduce $previousState with $mutation to $newState")

    internal class Canceled(tag: String) : Operation(tag, "canceled")

    override fun toString(): String = "||||| <control> ||||| $tag -> $message |||||"
}
