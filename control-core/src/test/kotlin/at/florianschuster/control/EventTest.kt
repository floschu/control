package at.florianschuster.control

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@FlowPreview
@ExperimentalCoroutinesApi
internal class EventTest {

    @Test
    fun `event message contains library name and tag`() {
        val tag = "some_tag"
        val event = ControllerEvent.Completed(tag)

        assertTrue(event.toString().contains("control"))
        assertTrue(event.toString().contains(tag))
    }

    @Test
    fun `ControllerImplementation logs events correctly`() {
        val events = mutableListOf<ControllerEvent>()
        val sut = TestScope(UnconfinedTestDispatcher()).eventsController(
            events,
            controllerStart = ControllerStart.Manual
        )

        assertTrue(events.last() is ControllerEvent.Created)
        assertTrue(events.last().toString().contains(ControllerStart.Manual.logName))

        sut.start()
        events.takeLast(2).let { lastEvents ->
            assertTrue(lastEvents[0] is ControllerEvent.Started)
            assertTrue(lastEvents[1] is ControllerEvent.State)
        }

        sut.dispatch(1)
        events.takeLast(3).let { lastEvents ->
            assertTrue(lastEvents[0] is ControllerEvent.Action)
            assertTrue(lastEvents[1] is ControllerEvent.Mutation)
            assertTrue(lastEvents[2] is ControllerEvent.State)
        }

        sut.dispatch(effectValue)
        events.takeLast(4).let { lastEvents ->
            assertTrue(lastEvents[0] is ControllerEvent.Action)
            assertTrue(lastEvents[1] is ControllerEvent.Effect)
            assertTrue(lastEvents[2] is ControllerEvent.Mutation)
            assertTrue(lastEvents[3] is ControllerEvent.State)
        }

        sut.cancel()
        assertTrue(events.last() is ControllerEvent.Completed)
    }

    @Test
    fun `ControllerStub logs event correctly`() {
        val events = mutableListOf<ControllerEvent>()
        val sut: Controller<Int, Int> = TestScope().eventsController(
            events,
            controllerStart = ControllerStart.Manual
        )
        sut.toStub()
        assertTrue(events.last() is ControllerEvent.Stub)

        events.clear()
        sut.toStub()
        assertEquals(0, events.count())
    }

    @Test
    fun `EffectControllerStub logs event correctly`() {
        val events = mutableListOf<ControllerEvent>()
        val sut: EffectController<Int, Int, Int> = TestScope().eventsController(
            events,
            controllerStart = ControllerStart.Manual
        )
        sut.toStub()
        assertTrue(events.last() is ControllerEvent.Stub)

        events.clear()
        sut.toStub()
        assertEquals(0, events.count())
    }

    @Test
    fun `ControllerImplementation logs mutator error correctly`() {
        val events = mutableListOf<ControllerEvent>()
        val sut = TestScope(UnconfinedTestDispatcher()).eventsController(events)

        sut.dispatch(mutatorErrorValue)
        events.takeLast(2).let { lastEvents ->
            assertTrue(lastEvents[0] is ControllerEvent.Error)
            assertTrue(lastEvents[1] is ControllerEvent.Completed)
        }
    }

    @Test
    fun `ControllerImplementation logs reducer error correctly`() {
        val events = mutableListOf<ControllerEvent>()
        val sut = TestScope(UnconfinedTestDispatcher()).eventsController(events)

        sut.dispatch(reducerErrorValue)
        events.takeLast(2).let { lastEvents ->
            assertTrue(lastEvents[0] is ControllerEvent.Error)
            assertTrue(lastEvents[1] is ControllerEvent.Completed)
        }
    }

    @Test
    fun `ControllerImplementation logs effect error correctly`() {
        val events = mutableListOf<ControllerEvent>()
        val sut = TestScope(UnconfinedTestDispatcher()).eventsController(events)

        repeat(ControllerImplementation.CAPACITY) { sut.dispatch(effectValue) }
        sut.dispatch(effectValue)

        events.takeLast(2).let { lastEvents ->
            assertTrue(lastEvents[0] is ControllerEvent.Error)
            assertTrue(lastEvents[1] is ControllerEvent.Completed)
        }
    }

    private fun CoroutineScope.eventsController(
        events: MutableList<ControllerEvent>,
        controllerStart: ControllerStart = ControllerStart.Lazy
    ) = ControllerImplementation<Int, Int, Int, Int>(
        scope = this,
        dispatcher = defaultScopeDispatcher(),
        controllerStart = controllerStart,
        initialState = 0,
        mutator = { action ->
            flow {
                if (action == effectValue) emitEffect(effectValue)
                check(action != mutatorErrorValue)
                emit(action)
            }
        },
        reducer = { mutation, previousState ->
            check(mutation != reducerErrorValue)
            previousState
        },
        actionsTransformer = { it },
        mutationsTransformer = { it },
        statesTransformer = { it },
        tag = "ImplementationEventTest.EventsController",
        controllerLog = ControllerLog.Custom { events.add(event) }
    )

    companion object {
        private const val mutatorErrorValue = 42
        private const val reducerErrorValue = 69
        private const val effectValue = 420
    }
}