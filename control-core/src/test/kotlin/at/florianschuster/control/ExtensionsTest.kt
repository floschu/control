package at.florianschuster.control

import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Test
import kotlin.test.assertEquals

internal class ExtensionsTest {

    @Test
    fun `bind lambda emits values correctly`() = runBlockingTest {
        val lambda = spyk<(Int) -> Unit>()
        flow {
            emit(1)
            emit(2)
        }.bind(to = lambda).launchIn(this)

        verify(exactly = 2) { lambda.invoke(any()) }
    }

    @Test(expected = IllegalStateException::class)
    fun `bind lambda throws error`() = runBlockingTest {
        flow<Int> { error("test") }.bind { }.launchIn(this)
    }

    @Test
    fun `distinctMap works`() = runBlockingTest {
        val result = listOf(0, 1, 1, 2, 2, 3, 4, 4, 5, 5).asFlow().distinctMap { it * 2 }.toList()
        assertEquals(listOf(0, 2, 4, 6, 8, 10), result)
    }

    @Test
    fun `takeUntil with predicate`() = runBlockingTest {
        val numberFlow = (0..10).asFlow()
        val list = numberFlow.takeUntil { it == 5 }.toList()
        val inclusiveList = numberFlow.takeUntil(inclusive = true) { it == 5 }.toList()

        assertEquals(listOf(0, 1, 2, 3, 4), list)
        assertEquals(listOf(0, 1, 2, 3, 4, 5), inclusiveList)
    }

    @Test
    fun `takeUntil with other flow`() = runBlockingTest {
        val numberFlow = (0..10).asFlow().map { delay(100); it }

        val shortResult = numberFlow.takeUntil(flow { delay(501); emit(Unit) }).toList()
        assertEquals(listOf(0, 1, 2, 3, 4), shortResult)

        val longResult = numberFlow.takeUntil(flow { delay(1101); emit(Unit) }).toList()
        assertEquals(listOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10), longResult)

        val emptyResult = emptyFlow<Int>().takeUntil(flow { delay(1101); emit(Unit) }).toList()
        assertEquals(emptyList(), emptyResult)
    }

    @Test
    fun `filterNotNullCast with empty flow`() = runBlockingTest {
        val result = emptyFlow<Int>().filterNotNullCast().toList()
        assertEquals(emptyList(), result)
    }

    @Test
    fun `filterNotNullCast with non-empty flow`() = runBlockingTest {
        val result = flowOf(null, 1, 2, null).filterNotNullCast().toList()
        assertEquals(listOf(1, 2), result)
    }
}