package at.florianschuster.control.data

import java.lang.Exception

/**
 * Sealed class that represents an asynchronous load of a data [value].
 */
sealed class Data<out T> {
    /**
     * Returns the value [value] if it is available, otherwise null.
     */
    open operator fun invoke(): T? = null

    /**
     * Represents the initial state of the data.
     */
    object Uninitialized : Data<Nothing>()

    /**
     * Represents the loading state of the data.
     */
    object Loading : Data<Nothing>()

    /**
     * Represents the failed state of the data containing the [error] cause.
     */
    data class Failure(val error: Exception) : Data<Nothing>()

    /**
     * Represents the successful state of the data containing the [value].
     */
    data class Success<out T>(val value: T) : Data<T>() {
        override operator fun invoke(): T = value
    }

    val uninitialized: Boolean get() = this is Uninitialized
    val loading: Boolean get() = this is Loading
    val failed: Boolean get() = this is Failure
    val successful: Boolean get() = this is Success
    val complete: Boolean get() = this is Error || this is Success

    companion object {

        /**
         * Invoke this to create either a [Data.Success] or [Data.Failure] from the [captor].
         */
        operator fun <T> invoke(captor: () -> T): Data<T> =
            try {
                Success(captor())
            } catch (e: Exception) {
                Failure(e)
            }
    }
}
