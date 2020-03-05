package at.florianschuster.control

import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Test
import kotlin.test.assertEquals

internal class TypeHelpersTest {

    @Test
    fun `simple mutator`() = runBlockingTest {
        val sut = Mutator<Int, Int, Int> { flowOf(it * 2) }

        val result = flowOf(1, 2, 3)
            .flatMapConcat { sut(it, fakeStateAccessor, fakeActionFlow) }
            .toList()

        assertEquals(listOf(2, 4, 6), result)
    }

    @Test
    fun `complex mutator`() = runBlockingTest {
        val sut = ComplexMutator<Int, Int, Int> { action, stateAccessor, actionFlow ->
            assertEquals(fakeStateAccessor, stateAccessor)
            assertEquals(fakeActionFlow, actionFlow)
            flowOf(action * 2)
        }

        val result = flowOf(1, 2, 3)
            .flatMapConcat { sut(it, fakeStateAccessor, fakeActionFlow) }
            .toList()

        assertEquals(listOf(2, 4, 6), result)
    }

    @Test
    fun `simple reducer`() = runBlockingTest {
        val sut = Reducer<Int, Int> { mutation, previousState -> previousState + mutation }

        val result = flowOf(1, 2, 3)
            .scan(0) { mutation, previousState -> sut(mutation, previousState) }
            .toList()

        assertEquals(listOf(0, 1, 3, 6), result)
    }

    @Test
    fun `simple transformer`() = runBlockingTest {
        val sut = Transformer<Int> { emissions -> emissions.map { it * 2 } }

        val result = sut(flowOf(1, 2, 3)).toList()

        assertEquals(listOf(2, 4, 6), result)
    }

    companion object {
        private val fakeStateAccessor = { 1 }
        private val fakeActionFlow = flowOf(1)
    }
}