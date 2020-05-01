package at.florianschuster.control

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.flow.singleOrNull
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Test
import kotlin.coroutines.ContinuationInterceptor
import kotlin.test.assertEquals

internal class BuildersTest {

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

        assertEquals(null, sut.mutator.invoke(MutatorScopeImpl({ 1 }, flowOf(2)), 3).singleOrNull())
        assertEquals(1, sut.reducer.invoke(0, 1))

        assertEquals(1, sut.actionsTransformer.invoke(flowOf(1)).single())
        assertEquals(2, sut.mutationsTransformer.invoke(flowOf(2)).single())
        assertEquals(3, sut.statesTransformer.invoke(flowOf(3)).single())

        assertEquals(expectedTag, sut.tag)
        assertEquals(ControllerLog.default, sut.controllerLog)

        assertEquals(CoroutineStart.LAZY, sut.coroutineStart)
        assertEquals(
            coroutineContext[ContinuationInterceptor] as CoroutineDispatcher,
            sut.dispatcher
        )
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

        assertEquals(3, sut.mutator.invoke(MutatorScopeImpl({ 1 }, flowOf(2)), 3).single())
        assertEquals(1, sut.reducer.invoke(0, 1))

        assertEquals(1, sut.actionsTransformer.invoke(flowOf(1)).single())
        assertEquals(2, sut.mutationsTransformer.invoke(flowOf(2)).single())
        assertEquals(3, sut.statesTransformer.invoke(flowOf(3)).single())

        assertEquals(expectedTag, sut.tag)
        assertEquals(ControllerLog.default, sut.controllerLog)

        assertEquals(CoroutineStart.LAZY, sut.coroutineStart)
        assertEquals(
            coroutineContext[ContinuationInterceptor] as CoroutineDispatcher,
            sut.dispatcher
        )
    }
}