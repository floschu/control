package at.florianschuster.control

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals

internal class TakeUntilTest {

    @Test
    fun `takeUntil with predicate`() = runTest {
        val numberFlow = (0..10).asFlow()
        val list = numberFlow.takeUntil { it == 5 }.toList()
        val inclusiveList = numberFlow.takeUntil(inclusive = true) { it == 5 }.toList()

        assertEquals(listOf(0, 1, 2, 3, 4), list)
        assertEquals(listOf(0, 1, 2, 3, 4, 5), inclusiveList)
    }

    @Test
    fun `takeUntil with other flow`() = runTest {
        val numberFlow = (0..10).asFlow().map { delay(100); it }

        val shortResult = numberFlow.takeUntil(flow { delay(501); emit(Unit) }).toList()
        assertEquals(listOf(0, 1, 2, 3, 4), shortResult)

        val longResult = numberFlow.takeUntil(flow { delay(1101); emit(Unit) }).toList()
        assertEquals(listOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10), longResult)

        val emptyResult = emptyFlow<Int>().takeUntil(flow { delay(1101); emit(Unit) }).toList()
        assertEquals(emptyList(), emptyResult)
    }
}