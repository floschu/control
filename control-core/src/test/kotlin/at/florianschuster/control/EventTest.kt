package at.florianschuster.control

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.TestCoroutineScope
import org.junit.Test
import kotlin.test.assertTrue

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
        val sut = TestCoroutineScope().eventsController(
            events,
            controllerStart = ControllerStart.Managed
        )

        assertTrue(events.last() is ControllerEvent.Created)
        assertTrue(events.last().toString().contains(ControllerStart.Managed.toString()))

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

        sut.stub()
        assertTrue(events.last() is ControllerEvent.Stub)

        sut.cancel()
        assertTrue(events.last() is ControllerEvent.Completed)
    }

    @Test
    fun `ControllerImplementation logs mutator error correctly`() {
        val events = mutableListOf<ControllerEvent>()
        val sut = TestCoroutineScope().eventsController(events)

        sut.dispatch(mutatorErrorValue)
        events.takeLast(2).let { lastEvents ->
            assertTrue(lastEvents[0] is ControllerEvent.Error)
            assertTrue(lastEvents[1] is ControllerEvent.Completed)
        }
    }

    @Test
    fun `ControllerImplementation logs reducer error correctly`() {
        val events = mutableListOf<ControllerEvent>()
        val sut = TestCoroutineScope().eventsController(events)

        sut.dispatch(reducerErrorValue)
        events.takeLast(2).let { lastEvents ->
            assertTrue(lastEvents[0] is ControllerEvent.Error)
            assertTrue(lastEvents[1] is ControllerEvent.Completed)
        }
    }

    private fun CoroutineScope.eventsController(
        events: MutableList<ControllerEvent>,
        controllerStart: ControllerStart = ControllerStart.Lazy
    ) = ControllerImplementation<Int, Int, Int>(
        scope = this,
        dispatcher = scopeDispatcher,
        controllerStart = controllerStart,
        initialState = 0,
        mutator = { action ->
            flow {
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
    }
}