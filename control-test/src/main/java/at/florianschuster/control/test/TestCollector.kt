package at.florianschuster.control.test

import kotlinx.coroutines.flow.Flow
import java.util.Collections

/**
 * A class that contains all value emissions, all error emissions and all completions when
 * a [Flow] is tested with [Flow.test].
 * Similar to RxJava TestObserver.
 */
class TestCollector<T> {
    var tag: String = this::class.java.simpleName

    /**
     * All emissions of the collected [Flow].
     */
    val emissions: List<T> get() = synchronizedEmissions

    /**
     * All errors of the collected [Flow].
     */
    val errors: List<Throwable> get() = synchronizedErrors

    /**
     * Resets [emissions] and [errors] collections and thus this [TestCollector].
     */
    fun reset() {
        synchronizedEmissions.clear()
        synchronizedErrors.clear()
    }

    internal fun add(value: T) = synchronizedEmissions.add(value)
    private val synchronizedEmissions: MutableList<T> =
        Collections.synchronizedList(mutableListOf())

    internal fun add(throwable: Throwable): Boolean = synchronizedErrors.add(throwable)
    private val synchronizedErrors: MutableList<Throwable> =
        Collections.synchronizedList(mutableListOf())
}
