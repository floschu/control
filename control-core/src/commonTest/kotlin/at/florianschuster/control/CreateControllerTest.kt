package at.florianschuster.control

import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.flow.singleOrNull
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

@Suppress("UNCHECKED_CAST")
internal class CreateControllerTest {

    @Test
    fun `controller builder`() = runTest {
        val expectedInitialState = 42
        val sut = createController<Int, Int, Int>(
            initialState = expectedInitialState
        ) as ControllerImplementation<Int, Int, Int, Nothing>

        assertEquals(this, sut.scope)
        assertEquals(expectedInitialState, sut.initialState)

        val mutatorContext = object : EffectMutatorContext<Int, Int, Nothing> {
            override val currentState: Int
                get() = notImplemented()
            override val actions: Flow<Int>
                get() = notImplemented()

            override fun emitEffect(effect: Nothing) {
                notImplemented()
            }
        }
        assertEquals(null, sut.mutator(mutatorContext, 3).singleOrNull())

        val reducerContext = object : EffectReducerContext<Nothing> {
            override fun emitEffect(effect: Nothing) {
                notImplemented()
            }
        }
        assertEquals(1, sut.reducer(reducerContext, 0, 1))

        val transformerContext = object : EffectTransformerContext<Nothing> {
            override fun emitEffect(effect: Nothing) {
                notImplemented()
            }
        }
        assertEquals(1, sut.actionsTransformer(transformerContext, flowOf(1)).single())
        assertEquals(2, sut.mutationsTransformer(transformerContext, flowOf(2)).single())
        assertEquals(3, sut.statesTransformer(transformerContext, flowOf(3)).single())

        assertEquals(defaultControllerTag(), sut.tag)
        assertEquals(ControllerLog.None, sut.controllerLog)

        assertEquals(ControllerStart.Lazy, sut.controllerStart)
        assertEquals(defaultScopeDispatcher(), sut.dispatcher)

        coroutineContext.cancelChildren()
    }

    @Test
    fun `effect controller builder`() = runTest {
        val expectedInitialState = 42
        val sut = createEffectController<Int, Int, Int, Int>(
            initialState = expectedInitialState
        ) as ControllerImplementation<Int, Int, Int, Int>

        assertEquals(this, sut.scope)
        assertEquals(expectedInitialState, sut.initialState)

        val mutatorContext = object : EffectMutatorContext<Int, Int, Int> {
            override val currentState: Int
                get() = notImplemented()
            override val actions: Flow<Int>
                get() = notImplemented()

            override fun emitEffect(effect: Int) {
                notImplemented()
            }
        }
        assertEquals(null, sut.mutator(mutatorContext, 3).singleOrNull())

        val reducerContext = object : EffectReducerContext<Int> {
            override fun emitEffect(effect: Int) {
                notImplemented()
            }
        }
        assertEquals(1, sut.reducer(reducerContext, 0, 1))

        val transformerContext = object : EffectTransformerContext<Int> {
            override fun emitEffect(effect: Int) {
                notImplemented()
            }
        }
        assertEquals(1, sut.actionsTransformer(transformerContext, flowOf(1)).single())
        assertEquals(2, sut.mutationsTransformer(transformerContext, flowOf(2)).single())
        assertEquals(3, sut.statesTransformer(transformerContext, flowOf(3)).single())

        assertEquals(defaultControllerTag(), sut.tag)
        assertEquals(ControllerLog.None, sut.controllerLog)

        assertEquals(ControllerStart.Lazy, sut.controllerStart)
        assertEquals(defaultScopeDispatcher(), sut.dispatcher)

        coroutineContext.cancelChildren()
    }
}
