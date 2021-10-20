package at.florianschuster.control

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import org.junit.Test
import java.lang.IllegalStateException
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@ExperimentalCoroutinesApi
internal class DefaultScopeDispatcherTest {

    @Test
    fun `regular dispatcher`() {
        val expectedDispatcher = Dispatchers.IO
        assertEquals(
            expectedDispatcher,
            CoroutineScope(expectedDispatcher).defaultScopeDispatcher()
        )
    }

    @Test
    fun `test dispatcher`() {
        val expectedDispatcher = TestCoroutineDispatcher()
        assertEquals(
            expectedDispatcher,
            CoroutineScope(expectedDispatcher).defaultScopeDispatcher()
        )
    }

    @Test
    fun `scope without interceptor fails`() {
        val scope = CoroutineScope(CoroutineName("name"))
        assertFailsWith<IllegalStateException> { scope.defaultScopeDispatcher() }
    }
}