package at.florianschuster.control.counterexample

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import at.florianschuster.control.bind
import kotlinx.android.synthetic.main.activity_counter.*
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import reactivecircus.flowbinding.android.view.clicks

class CounterActivity : AppCompatActivity(R.layout.activity_counter) {
    private val controller: CounterController by lazy { CounterController() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // action
        btnIncrease.clicks()
            .map { CounterAction.Increment }
            .bind(to = controller.action)
            .launchIn(scope = lifecycleScope)

        btnDecrease.clicks()
            .map { CounterAction.Decrement }
            .bind(to = controller.action)
            .launchIn(scope = lifecycleScope)

        // state
        controller.state.map { it.value }
            .distinctUntilChanged()
            .map { "$it" }
            .bind(to = tvValue::setText)
            .launchIn(scope = lifecycleScope)

        controller.state.map { it.loading }
            .distinctUntilChanged()
            .map { if (it) View.VISIBLE else View.GONE }
            .bind(to = progressLoading::setVisibility)
            .launchIn(scope = lifecycleScope)
    }

    override fun onDestroy() {
        super.onDestroy()
        controller.cancel()
    }
}
