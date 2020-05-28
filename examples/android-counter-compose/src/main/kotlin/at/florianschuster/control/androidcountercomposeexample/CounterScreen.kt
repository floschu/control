package at.florianschuster.control.androidcountercomposeexample

import androidx.compose.Composable
import androidx.compose.getValue
import androidx.compose.remember
import androidx.ui.core.Alignment
import androidx.ui.core.Modifier
import androidx.ui.core.tag
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
    injectedController: CounterController = CounterController()
) {
    val controller = remember { injectedController }
    val counterState by controller.collectState()
    CounterComponent(counterState, controller::dispatch)
}

@Composable
private fun CounterComponent(
    counterState: CounterController.State,
    dispatch: (CounterController.Action) -> Unit = {}
) {
    Stack(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().gravity(Alignment.Center),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            TextButton(onClick = { dispatch(CounterController.Action.Decrement) }) {
                Text(text = "-", style = MaterialTheme.typography.h4)
            }
            Text(
                text = "${counterState.value}",
                color = MaterialTheme.colors.secondary,
                style = MaterialTheme.typography.h3,
                modifier = Modifier.tag("valueText")
            )
            TextButton(onClick = { dispatch(CounterController.Action.Increment) }) {
                Text(text = "+", style = MaterialTheme.typography.h4)
            }
        }
        if (counterState.loading) {
            CircularProgressIndicator(
                modifier = Modifier.padding(16.dp)
                    .gravity(Alignment.BottomCenter)
                    .tag("progressIndicator"),
                color = MaterialTheme.colors.secondary
            )
        }
    }
}

@Preview("not loading")
@Composable
private fun CounterComponentPreview() {
    MaterialTheme(colors = AppColors.currentColorPalette) {
        CounterComponent(CounterController.State(value = -1, loading = false))
    }
}

@Preview("loading")
@Composable
private fun CounterComponentPreviewLoading() {
    MaterialTheme(colors = AppColors.currentColorPalette) {
        CounterComponent(CounterController.State(value = 1, loading = true))
    }
}
