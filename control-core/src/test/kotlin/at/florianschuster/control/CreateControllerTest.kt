package at.florianschuster.control

import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.flow.singleOrNull
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Test
import kotlin.test.assertEquals

internal class CreateControllerTest {

    @Suppress("UNCHECKED_CAST")
    @Test
    fun `default parameters of controller builder`() = runBlockingTest {
        val expectedInitialState = 42
        val sut = createController<Int, Int, Int>(
            initialState = expectedInitialState
        ) as ControllerImplementation<Int, Int, Int, Nothing>

        assertEquals(this, sut.scope)
        assertEquals(expectedInitialState, sut.initialState)

        assertEquals(null, sut.mutator(mockk(), 3).singleOrNull())
        assertEquals(1, sut.reducer(mockk(), 0, 1))

        assertEquals(1, sut.actionsTransformer(mockk(), flowOf(1)).single())
        assertEquals(2, sut.mutationsTransformer(mockk(), flowOf(2)).single())
        assertEquals(3, sut.statesTransformer(mockk(), flowOf(3)).single())

        assertEquals(defaultTag(), sut.tag)
        assertEquals(ControllerLog.default, sut.controllerLog)

        assertEquals(ControllerStart.Lazy, sut.controllerStart)
        assertEquals(defaultScopeDispatcher(), sut.dispatcher)
    }
}
