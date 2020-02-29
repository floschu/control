package at.florianschuster.control

import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Test
import kotlin.test.assertEquals

internal class ReducerTest {

    @Test
    fun `simple reducer`() = runBlockingTest {
        val sut = Reducer<Int, Int> { previousState, mutation -> previousState + mutation }

        val result = flowOf(1, 2, 3)
            .scan(0) { previousState, mutation -> sut(previousState, mutation) }
            .toList()

        assertEquals(listOf(0, 1, 3, 6), result)
    }
}