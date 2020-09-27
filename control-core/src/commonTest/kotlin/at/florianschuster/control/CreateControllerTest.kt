@file:Suppress("EXPERIMENTAL_API_USAGE", "UNCHECKED_CAST")

package at.florianschuster.control

import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.flow.singleOrNull
import kotlin.test.Test
import kotlin.test.assertEquals

internal class CreateControllerTest {

    @Test
    fun `default parameters of controller builder`() = suspendTest {

        val expectedInitialState = 42
        val sut = createController<Int, Int, Int>(
            initialState = expectedInitialState
        ) as ControllerImplementation<Int, Int, Int, Nothing>

        assertEquals(this, sut.scope)
        assertEquals(expectedInitialState, sut.initialState)

        assertEquals(null, sut.mutator(mutatorContext, 3).singleOrNull())
        assertEquals(1, sut.reducer(reducerContext, 0, 1))


        assertEquals(1, sut.actionsTransformer(transformerContext, flowOf(1)).single())
        assertEquals(2, sut.mutationsTransformer(transformerContext, flowOf(2)).single())
        assertEquals(3, sut.statesTransformer(transformerContext, flowOf(3)).single())

        assertEquals(defaultTag(), sut.tag)
        assertEquals(ControllerLog.default, sut.controllerLog)

        assertEquals(ControllerStart.Lazy, sut.controllerStart)
        assertEquals(defaultScopeDispatcher(), sut.dispatcher)
    }

    companion object {
        private val mutatorContext =
            ControllerImplementation.createMutatorContext<Int, Int, Nothing>({ 4 }, emptyFlow(), {})
        private val reducerContext =
            ControllerImplementation.createReducerContext<Nothing> { }
        private val transformerContext =
            ControllerImplementation.createTransformerContext<Nothing> { }
    }
}
