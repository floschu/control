package at.florianschuster.control.configuration

internal sealed class Operation(
    private val tag: String?,
    private val message: String
) {
    object ControlConfigured : Operation(null, "is now configured")

    class Initialized(tag: String?, initialState: String) :
        Operation(tag, "initialized with $initialState")

    class Mutate(tag: String?, action: String) :
        Operation(tag, "mutate with $action")

    class Reduce(tag: String?, previousState: String, mutation: String, newState: String) :
        Operation(tag, "reduce $previousState with $mutation to $newState")

    class Destroyed(tag: String?) : Operation(tag, "destroyed")

    override fun toString(): String {
        return "||||| <control> ||||| ${if (tag != null) "$tag -> " else ""}$message |||||"
    }
}
