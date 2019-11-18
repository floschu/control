package at.florianschuster.control

import at.florianschuster.control.util.AssociatedObject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

/**
 * An [EventController] can be used when there is a need for [Event]'s that are not part of the
 * [State]. These events could be a sealed class that defines navigation events or UI feedback
 * events that should only be triggered once for a single collector.
 */
@FlowPreview
@ExperimentalCoroutinesApi
interface EventController<Action, Mutation, State, Event> : Controller<Action, Mutation, State> {

    val events: PublishProcessor<Event>
        get() = AssociatedMap.Events().valueForOrCreate(this) {
            PublishProcessor(singleCollector = true)
        }

    override fun cancel() {
        super.cancel()
        AssociatedMap.values().forEach { it().clearFor(this) }
    }

    companion object {
        private enum class AssociatedMap(
            private val associatedObject: AssociatedObject = AssociatedObject()
        ) {
            Events;

            operator fun invoke(): AssociatedObject = associatedObject
        }
    }
}