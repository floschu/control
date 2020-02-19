package at.florianschuster.control

import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Test

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
}