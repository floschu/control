package at.florianschuster.control.counterexample

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import at.florianschuster.control.Controller
import kotlinx.android.synthetic.main.view_counter.*
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import reactivecircus.flowbinding.android.view.clicks

class CounterView : Fragment(R.layout.view_counter) {

    private val controller: Controller<CounterAction, CounterMutation, CounterState> =
        ControllerProvider()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // action
        increaseButton.clicks()
            .map { CounterAction.Increment }
            .onEach { controller.dispatch(it) }
            .launchIn(scope = lifecycleScope)

        decreaseButton.clicks()
            .map { CounterAction.Decrement }
            .onEach { controller.dispatch(it) }
            .launchIn(scope = lifecycleScope)

        // state
        controller.state.map { it.value }
            .distinctUntilChanged()
            .map { "$it" }
            .onEach { valueTextView.text = it }
            .launchIn(scope = lifecycleScope)

        controller.state.map { it.loading }
            .distinctUntilChanged()
            .map { if (it) View.VISIBLE else View.GONE }
            .onEach { loadingProgressBar.visibility = it }
            .launchIn(scope = lifecycleScope)
    }

    override fun onDestroy() {
        super.onDestroy()
        controller.cancel()
    }

    companion object {
        internal var ControllerProvider: () -> Controller<CounterAction, CounterMutation, CounterState> =
            { CounterController() }
    }
}
