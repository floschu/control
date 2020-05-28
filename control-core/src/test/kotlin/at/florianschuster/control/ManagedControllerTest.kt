package at.florianschuster.control

import io.mockk.mockk
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.flow.singleOrNull
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Test
import kotlin.coroutines.ContinuationInterceptor
import kotlin.test.assertEquals

internal class ManagedControllerTest {

    @Test
    fun `default parameters of managed controller builder`() = runBlockingTest {
        val expectedInitialState = 42
        val expectedTag = defaultTag()
        val sut = ManagedController<Int, Int, Int>(
            expectedInitialState,
            tag = expectedTag,
            dispatcher = coroutineContext[ContinuationInterceptor] as CoroutineDispatcher
        ) as ControllerImplementation<Int, Int, Int>

        assertEquals(scopeDispatcher, sut.scope.scopeDispatcher)
        assertEquals(expectedInitialState, sut.initialState)

        assertEquals(null, sut.mutator(mockk(), 3).singleOrNull())
        assertEquals(1, sut.reducer(0, 1))

        assertEquals(1, sut.actionsTransformer(flowOf(1)).single())
        assertEquals(2, sut.mutationsTransformer(flowOf(2)).single())
        assertEquals(3, sut.statesTransformer(flowOf(3)).single())

        assertEquals(expectedTag, sut.tag)
        assertEquals(ControllerLog.default, sut.controllerLog)

        assertEquals(ControllerStart.Managed, sut.controllerStart)
        assertEquals(scopeDispatcher, sut.dispatcher)
    }
}