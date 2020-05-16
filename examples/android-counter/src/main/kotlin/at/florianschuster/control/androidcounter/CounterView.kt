package at.florianschuster.control.androidcounter

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import at.florianschuster.control.kotlincounter.CounterAction
import at.florianschuster.control.kotlincounter.CounterController
import at.florianschuster.control.kotlincounter.createCounterController
import kotlinx.android.synthetic.main.view_counter.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import reactivecircus.flowbinding.android.view.clicks

internal class CounterView : Fragment(R.layout.view_counter) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val controller = CounterControllerProvider(viewLifecycleOwner.lifecycleScope)

        // action
        increaseButton.clicks()
            .map { CounterAction.Increment }
            .onEach { controller.dispatch(it) }
            .launchIn(scope = viewLifecycleOwner.lifecycleScope)

        decreaseButton.clicks()
            .map { CounterAction.Decrement }
            .onEach { controller.dispatch(it) }
            .launchIn(scope = viewLifecycleOwner.lifecycleScope)

        // state
        controller.state.map { it.value }
            .distinctUntilChanged()
            .map { "$it" }
            .onEach { valueTextView.text = it }
            .launchIn(scope = viewLifecycleOwner.lifecycleScope)

        controller.state.map { it.loading }
            .distinctUntilChanged()
            .map { if (it) View.VISIBLE else View.GONE }
            .onEach { loadingProgressBar.visibility = it }
            .launchIn(scope = viewLifecycleOwner.lifecycleScope)
    }

    companion object {
        internal var CounterControllerProvider: (
            scope: CoroutineScope
        ) -> CounterController = { it.createCounterController() }
    }
}
