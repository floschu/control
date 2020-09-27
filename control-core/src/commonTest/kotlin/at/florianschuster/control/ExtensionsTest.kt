@file:Suppress("EXPERIMENTAL_API_USAGE")

package at.florianschuster.control

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

internal class ExtensionsTest {

    @Test
    fun `bind lambda emits values correctly`() = suspendTest {
        val emits = mutableListOf<Int>()

        val lambda: (Int) -> Unit = { emits.add(it) }
        flow {
            emit(1)
            emit(2)
        }.bind(to = lambda).launchIn(this)

        assertEquals(2, emits.count())
    }

    @Test
    fun `bind lambda throws error`() = suspendTest {
        assertFailsWith<IllegalStateException> {
            flow<Int> { error("test") }.bind { }.first()
        }
    }

    @Test
    fun `distinctMap works`() = suspendTest {
        val result = listOf(0, 1, 1, 2, 2, 3, 4, 4, 5, 5).asFlow().distinctMap { it * 2 }.toList()
        assertEquals(listOf(0, 2, 4, 6, 8, 10), result)
    }

    @Test
    fun `takeUntil with predicate`() = suspendTest {
        val numberFlow = (0..10).asFlow()
        val list = numberFlow.takeUntil { it == 5 }.toList()
        val inclusiveList = numberFlow.takeUntil(inclusive = true) { it == 5 }.toList()

        assertEquals(listOf(0, 1, 2, 3, 4), list)
        assertEquals(listOf(0, 1, 2, 3, 4, 5), inclusiveList)
    }

    @Test
    fun `takeUntil with other flow`() = suspendTest {
        val numberFlow = (0..10).asFlow().map { delay(100); it }

        val shortResult = numberFlow.takeUntil(flow { delay(501); emit(Unit) }).toList()
        assertEquals(listOf(0, 1, 2, 3, 4), shortResult)

        val longResult = numberFlow.takeUntil(flow { delay(1101); emit(Unit) }).toList()
        assertEquals(listOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10), longResult)

        val emptyResult = emptyFlow<Int>().takeUntil(flow { delay(1101); emit(Unit) }).toList()
        assertEquals(emptyList(), emptyResult)
    }
}