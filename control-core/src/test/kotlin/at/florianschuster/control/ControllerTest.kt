package at.florianschuster.control

import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.flow.singleOrNull
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Test
import kotlin.test.assertEquals

internal class ControllerTest {

    @Test
    fun `default parameters of controller builder`() = runBlockingTest {
        val expectedInitialState = 42
        val expectedTag = defaultTag()
        val sut = createController<Int, Int, Int>(
            initialState = expectedInitialState,
            tag = expectedTag
        ) as ControllerImplementation<Int, Int, Int>

        assertEquals(this, sut.scope)
        assertEquals(expectedInitialState, sut.initialState)

        assertEquals(null, sut.mutator(mockk(), 3).singleOrNull())
        assertEquals(1, sut.reducer(0, 1))

        assertEquals(1, sut.actionsTransformer(flowOf(1)).single())
        assertEquals(2, sut.mutationsTransformer(flowOf(2)).single())
        assertEquals(3, sut.statesTransformer(flowOf(3)).single())

        assertEquals(expectedTag, sut.tag)
        assertEquals(ControllerLog.default, sut.controllerLog)

        assertEquals(ControllerStart.Lazy, sut.controllerStart)
        assertEquals(scopeDispatcher, sut.dispatcher)
    }

    @Test
    fun `default parameters of synchronous controller builder`() = runBlockingTest {
        val expectedInitialState = 42
        val expectedTag = defaultTag()
        val sut = createSynchronousController<Int, Int>(
            initialState = expectedInitialState,
            tag = expectedTag
        ) as ControllerImplementation<Int, Int, Int>

        assertEquals(this, sut.scope)
        assertEquals(expectedInitialState, sut.initialState)

        assertEquals(3, sut.mutator(mockk(), 3).single())
        assertEquals(1, sut.reducer(0, 1))

        assertEquals(1, sut.actionsTransformer(flowOf(1)).single())
        assertEquals(2, sut.mutationsTransformer(flowOf(2)).single())
        assertEquals(3, sut.statesTransformer(flowOf(3)).single())

        assertEquals(expectedTag, sut.tag)
        assertEquals(ControllerLog.default, sut.controllerLog)

        assertEquals(ControllerStart.Lazy, sut.controllerStart)
        assertEquals(scopeDispatcher, sut.dispatcher)
    }
}
