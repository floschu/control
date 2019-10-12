package at.florianschuster.test

import at.florianschuster.test.configuration.configureControl
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Test
import kotlin.test.assertEquals

class ControllerExtTest {
    @Test
    fun `changesFrom just emits changes`() = runBlockingTest {
        val testData = flow {
            emit(1 to false)
            emit(2 to false)
            emit(2 to false)
            emit(3 to false)
            emit(3 to false)
            emit(4 to false)
        }.changesFrom { it.first }.toList()

        assertEquals(listOf(1, 2, 3, 4), testData)
    }

    @Test
    fun `bind emits values correctly`() = runBlockingTest {
        val testValues = mutableListOf<Int>()
        flow {
            emit(1)
            emit(2)
            emit(3)
            emit(4)
        }.bind { testValues.add(it) }.launchIn(this)

        assertEquals(listOf(1, 2, 3, 4), testValues)
    }

    @Test
    fun `bind catches errors correctly and logs it`() = runBlockingTest {
        val errors= mutableListOf<Throwable>()
        configureControl { errors { errors.add(it) } }

        val testValues = mutableListOf<Int>()
        flow {
            emit(1)
            emit(2)
            throw Error()
        }.bind { testValues.add(it) }.launchIn(this)

        assertEquals(errors.count(), 1)
        assertEquals(listOf(1, 2), testValues)
    }
}