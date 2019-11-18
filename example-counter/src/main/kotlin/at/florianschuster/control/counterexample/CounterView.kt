package at.florianschuster.control.counterexample

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import at.florianschuster.control.bind
import kotlinx.android.synthetic.main.view_counter.*
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import reactivecircus.flowbinding.android.view.clicks

class CounterView : Fragment(R.layout.view_counter) {
    private val controller: CounterController = ControllerProvider()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // action
        increaseButton.clicks()
            .map { CounterAction.Increment }
            .bind(to = controller.action)
            .launchIn(scope = lifecycleScope)

        decreaseButton.clicks()
            .map { CounterAction.Decrement }
            .bind(to = controller.action)
            .launchIn(scope = lifecycleScope)

        // state
        controller.state.map { it.value }
            .distinctUntilChanged()
            .map { "$it" }
            .bind(to = valueTextView::setText)
            .launchIn(scope = lifecycleScope)

        controller.state.map { it.loading }
            .distinctUntilChanged()
            .map { if (it) View.VISIBLE else View.GONE }
            .bind(to = loadingProgressBar::setVisibility)
            .launchIn(scope = lifecycleScope)
    }

    override fun onDestroy() {
        super.onDestroy()
        controller.cancel()
    }

    companion object {
        internal var ControllerProvider: () -> CounterController = { CounterController() }
    }
}
