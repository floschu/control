package at.florianschuster.control

import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Test
import java.io.IOException
import kotlin.test.assertEquals

internal class ControlExtTest {

    private val logList = mutableListOf<String>()

    @Before
    fun setup() {
        logList.clear()
        ControlLogConfiguration.default = ControlLogConfiguration.Custom("tag", operations = { logList.add(it) })
    }

    @Test
    fun `bind lambda emits values correctly`() = runBlockingTest {
        assertEquals(0, logList.count())

        val lambda = spyk<(Int) -> Unit>()
        flow {
            emit(1)
            emit(2)
            throw IOException("test")
        }.bind(to = lambda).launchIn(this)
        verify(exactly = 2) { lambda.invoke(any()) }

        assertEquals(1, logList.count())
    }

    @Test
    fun `bind delegate emits values correctly`() = runBlockingTest {
        assertEquals(0, logList.count())

        val sut = spyk<ControllerDelegate<Int, Unit>>()
        flow {
            emit(1)
            emit(2)
            throw IOException("test")
        }.bind(to = sut).launchIn(this)
        verify(exactly = 2) { sut.dispatch(any()) }

        assertEquals(1, logList.count())
    }

    @Test
    fun `bind controller emits values correctly`() = runBlockingTest {
        assertEquals(0, logList.count())

        val controller = spyk(Controller<Int, Unit, Int>(0))
        flow {
            emit(1)
            emit(2)
            throw IOException("test")
        }.bind(to = controller).launchIn(this)
        verify(exactly = 2) { controller.dispatch(any()) }

        assertEquals(1, logList.filter { it.contains("IOException") }.count())
    }
}