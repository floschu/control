package at.florianschuster.control.android

import androidx.lifecycle.ViewModel
import at.florianschuster.control.Controller
import at.florianschuster.control.DirectController
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

/**
 * A [DirectController] that implements AAC [ViewModel] and automatically calls [Controller.cancel]
 * when [ViewModel.onCleared] is called.
 */
@ExperimentalCoroutinesApi
@FlowPreview
abstract class DirectControllerViewModel<Action, State> : ViewModel(),
    DirectController<Action, State> {

    override fun onCleared() {
        super.onCleared()
        cancel()
    }
}
