package at.florianschuster.control.android

import androidx.lifecycle.ViewModel
import at.florianschuster.control.Controller
import at.florianschuster.control.EventController
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

/**
 * An [EventController] that implements AAC [ViewModel] and automatically calls [Controller.cancel]
 * when [ViewModel.onCleared] is called.
 */
@ExperimentalCoroutinesApi
@FlowPreview
abstract class EventControllerViewModel<Action, Mutation, State, Event> : ViewModel(),
    EventController<Action, Mutation, State, Event> {

    override fun onCleared() {
        super.onCleared()
        cancel()
    }
}
