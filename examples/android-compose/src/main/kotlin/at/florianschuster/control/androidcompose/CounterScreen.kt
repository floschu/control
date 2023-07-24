package at.florianschuster.control.androidcompose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.florianschuster.control.kotlincounter.CounterAction
import at.florianschuster.control.kotlincounter.CounterController
import at.florianschuster.control.kotlincounter.CounterState
import at.florianschuster.control.kotlincounter.createCounterController
import kotlinx.coroutines.CoroutineScope

@Suppress("RememberReturnType") // this is a bug with Kotlin 1.9.0
@Composable
internal fun CounterScreen(
    scope: CoroutineScope = rememberCoroutineScope(),
    controller: CounterController = remember(scope) { scope.createCounterController() }
) {
    val state by controller.state.collectAsState()
    CounterComponent(state = state, onAction = controller::dispatch)
}

@Composable
private fun CounterComponent(
    modifier: Modifier = Modifier,
    state: CounterState,
    onAction: (CounterAction) -> Unit = {}
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            CounterButton(
                modifier = Modifier.semantics { contentDescription = "decrement" },
                imageVector = Icons.Default.RemoveCircle,
                enabled = !state.loading,
                onClick = { onAction(CounterAction.Decrement) },
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                modifier = Modifier.semantics { contentDescription = "value" },
                text = "Value: ${state.value}"
            )
            Spacer(modifier = Modifier.width(16.dp))
            CounterButton(
                modifier = Modifier.semantics { contentDescription = "increment" },
                imageVector = Icons.Default.AddCircle,
                enabled = !state.loading,
                onClick = { onAction(CounterAction.Increment) },
            )
        }
        if (state.loading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp)
                    .semantics { contentDescription = "loading" },
            )
        }
    }
}

@Composable
private fun CounterButton(
    modifier: Modifier = Modifier,
    imageVector: ImageVector,
    enabled: Boolean,
    onClick: () -> Unit
) {
    IconButton(
        modifier = modifier,
        onClick = { onClick() },
        enabled = enabled,
        content = {
            Icon(
                imageVector = imageVector,
                contentDescription = null,
                tint = MaterialTheme.colors.primary,
            )
        }
    )
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