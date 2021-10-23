package at.florianschuster.control.composeexample

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import at.florianschuster.control.kotlincounter.CounterAction
import at.florianschuster.control.kotlincounter.CounterController
import at.florianschuster.control.kotlincounter.createCounterController
import kotlinx.coroutines.CoroutineScope

@Composable
internal fun CounterScreen(
    scope: CoroutineScope = rememberCoroutineScope(),
    controller: CounterController = remember(scope) { scope.createCounterController() }
) {
    val state by controller.state.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Button(
                onClick = { controller.dispatch(CounterAction.Decrement) },
                content = { Text(text = "-") },
                enabled = !state.loading
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = "Value: ${state.value}")
            Spacer(modifier = Modifier.width(16.dp))
            Button(
                onClick = { controller.dispatch(CounterAction.Increment) },
                content = { Text(text = "+") },
                enabled = !state.loading
            )
        }
        if (state.loading) CircularProgressIndicator()
    }
}