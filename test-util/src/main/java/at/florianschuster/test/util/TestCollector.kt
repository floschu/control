package at.florianschuster.test.util

import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import kotlin.test.assertEquals

class TestCollector<T> {
    var tag: String = this::class.java.simpleName

    val values: List<T> get() = atomicValues.get()
    fun add(value: T): Boolean = atomicValues.get().add(value)
    private val atomicValues = AtomicReference<MutableList<T>>(mutableListOf())

    val errors: List<Throwable> get() = atomicErrors.get()
    fun add(throwable: Throwable): Boolean = atomicErrors.get().add(throwable)
    private val atomicErrors = AtomicReference<MutableList<Throwable>>(mutableListOf())

    val completed: Boolean get() = atomicCompleted.get()
    fun complete() = atomicCompleted.set(true)
    private val atomicCompleted = AtomicBoolean(false)

    fun assertNoErrors() {
        assertEquals(0, atomicErrors.get().count(), "$tag no errors")
    }

    fun assertErrorsCount(expectedCount: Int) {
        assertEquals(expectedCount, atomicErrors.get().count(), "$tag errors count")
    }

    fun assertErrors(expectedErrors: List<Throwable>) {
        assertEquals(expectedErrors, atomicErrors.get(), "$tag errors")
    }

    fun assertError(index: Int, expectedError: Throwable) {
        assertEquals(expectedError, atomicErrors.get()[index], "$tag error at $index")
    }

    fun assertNoValues() {
        assertEquals(0, atomicValues.get().count(), "$tag no values")
    }

    fun assertValuesCount(expectedCount: Int) {
        assertEquals(expectedCount, atomicValues.get().count(), "$tag values count")
    }

    fun assertValues(expectedValues: List<T>) {
        assertEquals(expectedValues, atomicValues.get(), "$tag values")
    }

    fun assertValue(index: Int, expectedValue: T) {
        assertEquals(expectedValue, atomicValues.get()[index], "$tag value at $index")
    }

    fun assertCompleted() {
        assertEquals(true, atomicCompleted.get(), "$tag completed")
    }

    fun assertNotCompleted() {
        assertEquals(false, atomicCompleted.get(), "$tag not completed")
    }

    fun reset() {
        atomicValues.set(mutableListOf())
        atomicErrors.set(mutableListOf())
        atomicCompleted.set(false)
    }
}