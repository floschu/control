package at.florianschuster.control

import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
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
        val lambda = spyk<(Int) -> Unit>()
        flow<Int> { error("test") }.bind(to = lambda).launchIn(this)
    }

    @Test
    fun `bind controller emits values correctly`() = runBlockingTest {
        val sut = spyk<Controller<Int, Unit, Unit>>()
        flow {
            emit(1)
            emit(2)
        }.bind(to = sut).launchIn(this)

        verify(exactly = 1) { sut.dispatch(1) }
        verify(exactly = 1) { sut.dispatch(2) }
    }

    @Test
    fun `distinctMap works`() = runBlockingTest {
        val result = listOf(0, 1, 1, 2, 2, 3, 4, 4, 5, 5).asFlow().distinctMap { it * 2 }.toList()
        assertEquals(listOf(0, 2, 4, 6, 8, 10), result)
    }

    @Test
    fun `takeUntil works`() = runBlockingTest {
        val numberFlow = (0..10).asFlow().map { delay(100); it }

        val shortResult = numberFlow.takeUntil(flow { delay(501); emit(Unit) }).toList()
        assertEquals(listOf(0, 1, 2, 3, 4), shortResult)

        val longResult = numberFlow.takeUntil(flow { delay(1101); emit(Unit) }).toList()
        assertEquals(listOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10), longResult)

        val emptyResult = emptyFlow<Int>().takeUntil(flow { delay(1101); emit(Unit) }).toList()
        assertEquals(emptyList(), emptyResult)
    }
}