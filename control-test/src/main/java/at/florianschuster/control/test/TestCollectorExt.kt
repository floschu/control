package at.florianschuster.control.test

import kotlinx.coroutines.flow.Flow
import kotlin.test.assertEquals

/**
 * Asserts that no errors occurred during collection the the [Flow].
 */
fun <T> TestCollector<T>.hasNoErrors(): TestCollector<T> {
    assertEquals(0, errors.count(), "$tag no errors")
    return this
}

/**
 * Asserts that [expected] count of errors occurred during collection the the [Flow].
 */
infix fun <T> TestCollector<T>.hasErrorCount(expected: Int): TestCollector<T> {
    assertEquals(expected, errors.count(), "$tag errors count")
    return this
}

/**
 * Asserts that [expected] errors occurred during collection the the [Flow].
 */
fun <T> TestCollector<T>.hasErrors(vararg expected: Throwable): TestCollector<T> {
    assertEquals(expected.toList(), errors, "$tag errors")
    return this
}

/**
 * Asserts that [expected] errors occurred during collection the the [Flow].
 */
infix fun <T> TestCollector<T>.hasErrors(expected: List<Throwable>): TestCollector<T> {
    assertEquals(expected, errors, "$tag errors")
    return this
}

/**
 * Asserts that an [S::class] error occurred at [index] of the [errors] collection.
 */
inline fun <reified E : Throwable> TestCollector<*>.hasError(index: Int): TestCollector<*> {
    assertEquals(E::class, errors[index]::class, "$tag error at $index")
    return this
}

/**
 * Asserts that no emissions occurred during collection the the [Flow].
 */
fun <T> TestCollector<T>.hasNoEmissions(): TestCollector<T> {
    assertEquals(0, emissions.count(), "$tag no values")
    return this
}

/**
 * Asserts that [expected] count of emissions occurred during collection the the [Flow].
 */
infix fun <T> TestCollector<T>.hasEmissionCount(expected: Int): TestCollector<T> {
    assertEquals(expected, emissions.count(), "$tag values count")
    return this
}

/**
 * Asserts that [expected] emissions occurred during collection the the [Flow].
 */
fun <T> TestCollector<T>.hasEmissions(vararg expected: T): TestCollector<T> {
    assertEquals(expected.toList(), emissions, "$tag values")
    return this
}

/**
 * Asserts that [expected] emissions occurred during collection the the [Flow].
 */
infix fun <T> TestCollector<T>.hasEmissions(expected: List<T>): TestCollector<T> {
    assertEquals(expected, emissions, "$tag values")
    return this
}

/**
 * Asserts that [expected] emission occurred at [index] in the [emissions] collection.
 */
fun <T> TestCollector<T>.hasEmission(index: Int, expected: T): TestCollector<T> {
    assertEquals(expected, emissions[index], "$tag value at $index")
    return this
}