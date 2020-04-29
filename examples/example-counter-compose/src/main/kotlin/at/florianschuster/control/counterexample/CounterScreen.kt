package at.florianschuster.control.counterexample

import androidx.compose.Composable
import androidx.ui.core.Modifier
import androidx.ui.foundation.Text
import androidx.ui.layout.Arrangement
import androidx.ui.layout.Column
import androidx.ui.layout.Row
import androidx.ui.layout.fillMaxSize
import androidx.ui.material.CircularProgressIndicator
import androidx.ui.material.MaterialTheme
import androidx.ui.material.TextButton
import androidx.ui.tooling.preview.Preview

@Composable
internal fun CounterScreen(
    state: CounterState,
    action: (CounterAction) -> Unit = {}
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center
    ) {
        Row {
            TextButton(onClick = { action(CounterAction.Increment) }) {
                Text(text = "+", style = MaterialTheme.typography.h4)
            }
            Text(text = "${state.value}", style = MaterialTheme.typography.h3)
            TextButton(onClick = { action(CounterAction.Decrement) }) {
                Text(text = "-", style = MaterialTheme.typography.h4)
            }
        }
        if (state.loading) CircularProgressIndicator()
    }
}

@Preview
@Composable
private fun CounterScreenPreviewLoading() {
    CounterScreen(state = CounterState(value = 21, loading = true))
}

@Preview
@Composable
private fun CounterScreenPreviewNotLoading() {
    CounterScreen(state = CounterState(value = 21, loading = false))
}