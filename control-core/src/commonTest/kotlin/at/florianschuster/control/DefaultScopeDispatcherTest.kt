package at.florianschuster.control

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

internal class DefaultScopeDispatcherTest {

    @Test
    fun `regular dispatcher`() {
        val expectedDispatcher = Dispatchers.Default
        assertEquals(
            expectedDispatcher,
            CoroutineScope(expectedDispatcher).defaultScopeDispatcher()
        )
    }

    @Test
    fun `test dispatcher`() {
        val expectedDispatcher = StandardTestDispatcher()
        assertEquals(
            expectedDispatcher,
            CoroutineScope(expectedDispatcher).defaultScopeDispatcher()
        )
    }

    @Test
    fun `scope without interceptor fails`() {
        val scope = CoroutineScope(CoroutineName("name"))
        assertFailsWith<IllegalArgumentException> { scope.defaultScopeDispatcher() }
    }
}