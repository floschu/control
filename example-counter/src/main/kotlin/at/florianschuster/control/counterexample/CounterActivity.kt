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
import ru.ldralighieri.corbind.view.clicks

class CounterActivity : AppCompatActivity(R.layout.activity_counter) {
    private var savedCounterValue: Int = 0
    private val controller: CounterController by lazy { CounterController(savedCounterValue) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        savedCounterValue = savedInstanceState?.getInt(counterValueKey) ?: 0

        // action
        btnIncrease.clicks()
            .map { CounterAction.Increase }
            .bind(to = controller.action)
            .launchIn(scope = lifecycleScope)

        btnDecrease.clicks()
            .map { CounterAction.Decrease }
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
            .bind(progressLoading::setVisibility)
            .launchIn(scope = lifecycleScope)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(counterValueKey, controller.currentState.value)
    }

    override fun onDestroy() {
        super.onDestroy()
        controller.destroy()
    }

    companion object {
        private const val counterValueKey: String = "CounterActivity.counter"
    }
}
