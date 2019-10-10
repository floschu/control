package at.florianschuster.control.util

/**
 * Sealed class that represents an asynchronous load of a data resource.
 */
sealed class Data<out T> {
    open operator fun invoke(): T? = null

    object Uninitialized : Data<Nothing>()
    object Loading : Data<Nothing>()
    data class Failure(val error: Throwable) : Data<Nothing>()
    data class Success<out T>(val element: T) : Data<T>() {
        override operator fun invoke(): T = element
    }

    val uninitialized: Boolean get() = this is Uninitialized
    val loading: Boolean get() = this is Loading
    val failed: Boolean get() = this is Failure
    val successful: Boolean get() = this is Success
    val complete: Boolean get() = this is Error || this is Success

    companion object {
        inline fun <T> of(dataFunction: () -> T): Data<T> =
            try {
                Success(dataFunction())
            } catch (e: Exception) {
                Failure(e)
            }
    }
}
