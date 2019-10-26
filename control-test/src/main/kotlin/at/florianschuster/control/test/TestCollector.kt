package at.florianschuster.control.test

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.test.TestCoroutineScope
import java.util.concurrent.atomic.AtomicReference

/**
 * A class that contains all value emissions and all error emissions of a [Flow] that is
 * tested with [Flow.test].
 *
 * Similar to RxJava TestObserver.
 */
@ExperimentalCoroutinesApi
class TestCollector<T>(
    testedFlow: Flow<T>,
    testScope: TestCoroutineScope
) {
    // Tag for this [TestCollector] that is used by the DSL
    var tag: String = this::class.java.simpleName

    private val mutableEmissions: MutableList<T> = mutableListOf()
    private val mutableErrors: MutableList<Throwable> = mutableListOf()
    private val mutableCompletion = AtomicReference<Completion>(Completion.None)
    private val job: Job = let {
        testedFlow
            .onEach { emission -> mutableEmissions.add(emission) }
            .onCompletion { e ->
                mutableCompletion.set(if (e != null) Completion.Exceptional(e) else Completion.Regular)
            }
            .catch { error -> mutableErrors.add(error) }
            .launchIn(testScope)
    }

    /**
     * All emissions of the collected [Flow].
     */
    val emissions: List<T> = mutableEmissions

    /**
     * All errors of the collected [Flow].
     */
    val errors: List<Throwable> = mutableErrors

    /**
     * Completion of the collected [Flow].
     */
    val completion: Completion get() = mutableCompletion.get()

    /**
     * Resets [emissions] and [errors] collections and thus this [TestCollector].
     */
    fun reset() {
        mutableEmissions.clear()
        mutableErrors.clear()
        mutableCompletion.set(Completion.None)
    }

    /**
     * Cancels the collection of [emissions] and [errors].
     */
    fun cancel() {
        job.cancel()
    }

    sealed class Completion {
        object None : Completion()
        data class Exceptional(val error: Throwable) : Completion()
        object Regular : Completion()
    }
}
