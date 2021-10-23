package at.florianschuster.control.composeexample

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.florianschuster.control.kotlincounter.CounterAction
import at.florianschuster.control.kotlincounter.CounterController
import at.florianschuster.control.kotlincounter.CounterState
import at.florianschuster.control.kotlincounter.createCounterController
import kotlinx.coroutines.CoroutineScope

@Composable
internal fun CounterScreen(
    scope: CoroutineScope = rememberCoroutineScope(),
    controller: CounterController = remember(scope) { scope.createCounterController() }
) {
    val state by controller.state.collectAsState()
    CounterComponent(state = state, onAction = controller::dispatch)
}

@Composable
internal fun CounterComponent(
    modifier: Modifier = Modifier,
    state: CounterState,
    onAction: (CounterAction) -> Unit = {}
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Button(
                modifier = Modifier.semantics { contentDescription = "decrement" },
                onClick = { onAction(CounterAction.Decrement) },
                content = { Text(text = "-") },
                enabled = !state.loading
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = "Value: ${state.value}")
            Spacer(modifier = Modifier.width(16.dp))
            Button(
                modifier = Modifier.semantics { contentDescription = "increment" },
                onClick = { onAction(CounterAction.Increment) },
                content = { Text(text = "+") },
                enabled = !state.loading
            )
        }
        if (state.loading) CircularProgressIndicator()
    }
}

@Preview
@Composable
fun CounterComponentPreview() {
    MaterialTheme {
        Surface {
            CounterComponent(state = CounterState(value = 2, loading = false))
        }
    }
}