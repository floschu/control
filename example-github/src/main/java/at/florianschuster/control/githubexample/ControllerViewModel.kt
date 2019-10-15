package at.florianschuster.control.githubexample

import androidx.lifecycle.ViewModel
import at.florianschuster.control.Controller

abstract class ControllerViewModel<A, M, S> : ViewModel(), Controller<A, M, S> {
    override fun onCleared() {
        super.onCleared()
        cancel()
    }
}