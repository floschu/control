package at.florianschuster.control.countercomposeexample

import androidx.compose.Composable
import androidx.ui.core.Alignment
import androidx.ui.core.Modifier
import androidx.ui.foundation.Text
import androidx.ui.layout.Arrangement
import androidx.ui.layout.Row
import androidx.ui.layout.Stack
import androidx.ui.layout.fillMaxSize
import androidx.ui.layout.fillMaxWidth
import androidx.ui.layout.padding
import androidx.ui.material.CircularProgressIndicator
import androidx.ui.material.MaterialTheme
import androidx.ui.material.TextButton
import androidx.ui.tooling.preview.Preview
import androidx.ui.unit.dp

@Composable
internal fun CounterScreen(
    counterState: CounterState,
    action: (CounterAction) -> Unit = {}
) {
    Stack(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth() + Modifier.gravity(Alignment.Center),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            TextButton(onClick = { action(CounterAction.Decrement) }) {
                Text(text = "-", style = MaterialTheme.typography.h4)
            }
            Text(
                text = "${counterState.value}",
                color = MaterialTheme.colors.secondary,
                style = MaterialTheme.typography.h3
            )
            TextButton(onClick = { action(CounterAction.Increment) }) {
                Text(text = "+", style = MaterialTheme.typography.h4)
            }
        }
        if (counterState.loading) {
            CircularProgressIndicator(
                modifier = Modifier.padding(16.dp) + Modifier.gravity(Alignment.BottomCenter),
                color = MaterialTheme.colors.secondary
            )
        }
    }
}

@Preview
@Composable
private fun CounterScreenPreviewLoading() {
    MaterialTheme(colors = AppColors.currentColorPalette) {
        CounterScreen(counterState = CounterState(value = 21, loading = true))
    }
}

@Preview
@Composable
private fun CounterScreenPreviewNotLoading() {
    MaterialTheme(colors = AppColors.currentColorPalette) {
        CounterScreen(counterState = CounterState(value = 21, loading = false))
    }
}