package at.florianschuster.control

import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Test
import kotlin.test.assertEquals

internal class ControllerExtTest {

    @Test
    fun `bind emits values correctly`() = runBlockingTest {
        val testValues = mutableListOf<Int>()
        flow {
            emit(1)
            emit(2)
            emit(3)
            emit(4)
        }.bind(to = { testValues.add(it) }).launchIn(this)

        assertEquals(listOf(1, 2, 3, 4), testValues)
    }
}