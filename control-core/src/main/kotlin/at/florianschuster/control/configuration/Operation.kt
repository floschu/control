package at.florianschuster.control.configuration

/**
 * Used to log certain operations in the library.
 */
internal sealed class Operation(
    private val tag: String,
    private val message: String
) {
    internal object ControlConfigured : Operation("library", "was configured")

    internal class ScopeSet<S>(tag: String, scope: S) :
        Operation(tag, "scope set to $scope")

    internal class Initialized<S>(tag: String, initialState: S) :
        Operation(tag, "initialized with $initialState")

    internal class Mutate<A>(tag: String, action: A) :
        Operation(tag, "mutate with $action")

    internal class Reduce<S, M>(tag: String, previousState: S, mutation: M, newState: S) :
        Operation(tag, "reduce $previousState with $mutation to $newState")

    internal class Canceled(tag: String) : Operation(tag, "canceled")

    internal class StubEnabled(tag: String, enabled: Boolean) :
        Operation(tag, "stub ${if (enabled) "enabled" else "not enabled"}")

    override fun toString(): String = "||||| <control> ||||| $tag -> $message |||||"
}
