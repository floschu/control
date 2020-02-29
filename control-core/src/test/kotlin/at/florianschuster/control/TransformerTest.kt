package at.florianschuster.control

import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Test
import kotlin.test.assertEquals

internal class TransformerTest {

    @Test
    fun `simple transformer`() = runBlockingTest {
        val sut = Transformer<Int> { emissions -> emissions.map { it * 2 } }

        val result = sut(flowOf(1, 2, 3)).toList()

        assertEquals(listOf(2, 4, 6), result)
    }
}