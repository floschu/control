package at.florianschuster.control

import at.florianschuster.control.test.TestCoroutineScopeRule
import at.florianschuster.control.test.test
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import org.junit.Rule
import org.junit.Test

class EventControllerTest  {

    @get:Rule
     val testScopeRule = TestCoroutineScopeRule()

    @Test
    fun `events are triggered correctly`() {
        val controller = TestEventController(
            testScopeRule,
            mutateEventIndex = 2,
            reduceEventIndex = 4
        )
        val eventsCollector = controller.events.test(testScopeRule)

        controller.action(Unit)
        controller.action(Unit)
        eventsCollector.assertValuesCount(0)
        controller.action(Unit)
        eventsCollector.assertValuesCount(1)
        controller.action(Unit)
        eventsCollector.assertValuesCount(2)

        with(eventsCollector) {
            assertNoErrors()
            assertValue(0, TestEventController.EVENT_MUTATE)
            assertValue(1, TestEventController.EVENT_REDUCE)
        }
    }

    private class TestEventController(
        override var scope: CoroutineScope,
        private val mutateEventIndex: Int? = null,
        private val reduceEventIndex: Int? = null
    ) : EventController<Unit, Unit, Int, String> {

        override val initialState: Int = 0

        override fun mutate(action: Unit): Flow<Unit> = when (currentState) {
            mutateEventIndex -> flow {
                events(EVENT_MUTATE)
                emit(Unit)
            }
            else -> flowOf(Unit)
        }

        override fun reduce(previousState: Int, mutation: Unit): Int {
            val reducedState = previousState + 1
            if (reducedState == reduceEventIndex) events(EVENT_REDUCE)
            return reducedState
        }

        companion object {
            const val EVENT_MUTATE = "event_mutate"
            const val EVENT_REDUCE = "event_reduce"
        }
    }
}