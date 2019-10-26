@file:Suppress("TooManyFunctions")

package at.florianschuster.control.test

import kotlinx.coroutines.flow.Flow
import kotlin.test.assertEquals
import kotlin.test.assertNotSame
import kotlin.test.assertSame
import kotlin.test.assertTrue

/**
 * Expects a certain [assertion] to be true from the [TestCollector].
 */
infix fun <T> TestCollector<T>.expect(assertion: (TestCollector<T>) -> Unit): TestCollector<T> {
    assertion(this)
    return this
}

/**
 * Asserts that no errors occurred during collection the the [Flow].
 */
fun noErrors(): (TestCollector<*>) -> Unit = { collector ->
    assertEquals(0, collector.errors.count(), "${collector.tag} has errors")
}

/**
 * Asserts that [expected] count of errors occurred during collection the the [Flow].
 */
fun errorCount(expected: Int): (TestCollector<*>) -> Unit = { collector ->
    assertEquals(expected, collector.errors.count(), "${collector.tag} has wrong errors count")
}

/**
 * Asserts that [expected] errors occurred during collection the the [Flow].
 */
fun <T> errors(vararg expected: Throwable): (TestCollector<T>) -> Unit = { collector ->
    assertEquals(expected.toList(), collector.errors, "${collector.tag} has wrong errors")
}

/**
 * Asserts that [expected] errors occurred during collection the the [Flow].
 */
fun <T> errors(expected: List<Throwable>): (TestCollector<T>) -> Unit = { collector ->
    assertEquals(expected, collector.errors, "${collector.tag} has wrong errors")
}

/**
 * Asserts that an [errorClass] error occurred at [index] of the [errors] collection.
 */
inline fun <reified T : Throwable> error(index: Int): (TestCollector<*>) -> Unit =
    { collector ->
        assertEquals(
            T::class,
            collector.errors[index]::class,
            "${collector.tag} has wrong error at $index"
        )
    }

/**
 * Asserts that an error with Type [T] occurred first in the [errors] collection.
 */
inline fun <reified T : Throwable> firstError(): (TestCollector<*>) -> Unit = { collector ->
    assertEquals(
        T::class,
        collector.errors.first()::class,
        "${collector.tag} has wrong first error"
    )
}

/**
 * Asserts that an error with Type [T] occurred last in the [errors] collection.
 */
inline fun <reified T : Throwable> lastError(): (TestCollector<*>) -> Unit = { collector ->
    assertEquals(
        T::class,
        collector.errors.last()::class,
        "${collector.tag} has wrong last error"
    )
}

/**
 * Asserts that no emissions occurred during collection the the [Flow].
 */
fun noEmissions(): (TestCollector<*>) -> Unit = { collector ->
    assertEquals(
        0,
        collector.emissions.count(),
        "${collector.tag} has values"
    )
}

/**
 * Asserts that [expected] count of emissions occurred during collection the the [Flow].
 */
fun emissionCount(expected: Int): (TestCollector<*>) -> Unit = { collector ->
    assertEquals(expected, collector.emissions.count(), "${collector.tag} has wrong emissions count")
}

/**
 * Asserts that [expected] emissions occurred during collection the the [Flow].
 */
fun <T> emissions(vararg expected: T): (TestCollector<T>) -> Unit = { collector ->
    assertEquals(expected.toList(), collector.emissions, "${collector.tag} has wrong emissions")
}

/**
 * Asserts that [expected] emissions occurred during collection the the [Flow].
 */
fun <T> emissions(expected: List<T>): (TestCollector<T>) -> Unit = { collector ->
    assertEquals(expected, collector.emissions, "${collector.tag} has wrong emissions")
}

/**
 * Asserts that [expected] emission occurred at [index] in the [emissions] collection.
 */
fun <T> emission(index: Int, expected: T): (TestCollector<T>) -> Unit = { collector ->
    assertEquals(expected, collector.emissions[index], "${collector.tag} no emission at $index")
}

/**
 * Asserts that [expected] emission occurred first in the [emissions] collection.
 */
fun <T> firstEmission(expected: T): (TestCollector<T>) -> Unit = { collector ->
    assertEquals(expected, collector.emissions.first(), "${collector.tag} wrong first emission")
}

/**
 * Asserts that [expected] emission occurred last in the [emissions] collection.
 */
fun <T> lastEmission(expected: T): (TestCollector<T>) -> Unit = { collector ->
    assertEquals(expected, collector.emissions.last(), "${collector.tag} wrong last emission")
}

/**
 * Asserts that the [TestCollector] has not completed emission collection.
 */
fun noCompletion(): (TestCollector<*>) -> Unit = { collector ->
    assertSame(
        TestCollector.Completion.None,
        collector.completion,
        "${collector.tag} has completion"
    )
}

/**
 * Asserts that the [TestCollector] has completed emission collection.
 */
fun anyCompletion(): (TestCollector<*>) -> Unit = { collector ->
    assertNotSame(
        TestCollector.Completion.None,
        collector.completion,
        "${collector.tag} has no completion"
    )
}

/**
 * Asserts that the [TestCollector] has completed emission collection with [TestCollector.Completion.Regular].
 */
fun regularCompletion(): (TestCollector<*>) -> Unit = { collector ->
    assertSame(
        TestCollector.Completion.Regular,
        collector.completion,
        "${collector.tag} has no regular completion"
    )
}

/**
 * Asserts that the [TestCollector] has completed emission collection with [TestCollector.Completion.Exceptional].
 */
inline fun <reified T : Throwable> exceptionalCompletion(): (TestCollector<*>) -> Unit =
    { collector ->
        assertTrue(
            collector.completion is TestCollector.Completion.Exceptional,
            "${collector.tag} has no exceptional completion"
        )
        assertEquals(
            T::class,
            (collector.completion as TestCollector.Completion.Exceptional).error::class,
            "${collector.tag} has no exceptional completion with ${T::class}"
        )
    }