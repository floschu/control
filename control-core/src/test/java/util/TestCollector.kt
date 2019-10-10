package util

import kotlin.test.assertEquals

class TestCollector<T> {
    val values = mutableListOf<T>()
    val errors = mutableListOf<Throwable>()

    fun assertNoErrors() {
        assertEquals(0, errors.count(), "TestCollector errors.")
    }

    fun assertValuesCount(expectedCount: Int) {
        assertEquals(expectedCount, values.count(), "TestCollector values count")
    }

    fun assertValues(expectedValues: List<T>) {
        assertEquals(expectedValues, values, "TestCollector values")
    }

    fun assertValue(index: Int, expectedValue: T) {
        assertEquals(expectedValue, values[index], "TestCollector value at $index")
    }
}