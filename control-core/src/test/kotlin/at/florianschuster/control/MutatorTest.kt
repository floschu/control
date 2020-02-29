package at.florianschuster.control

import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Test
import kotlin.test.assertEquals

internal class MutatorTest {

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

    companion object {
        private val fakeStateAccessor = { 1 }
        private val fakeActionFlow = flowOf(1)
    }
}