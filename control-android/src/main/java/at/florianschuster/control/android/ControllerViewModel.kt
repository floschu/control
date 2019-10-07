package at.florianschuster.control.android

import androidx.lifecycle.ViewModel
import at.florianschuster.control.Controller
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

/**
 * A [Controller] that implements AAC [ViewModel] and automatically calls [Controller.cancel]
 * when [ViewModel.onCleared] is called.
 */
@ExperimentalCoroutinesApi
@FlowPreview
abstract class ControllerViewModel<Action : Any, Mutation : Any, State : Any> :
    ViewModel(), Controller<Action, Mutation, State> {

    override fun onCleared() {
        super.onCleared()
        cancel()
    }
}
