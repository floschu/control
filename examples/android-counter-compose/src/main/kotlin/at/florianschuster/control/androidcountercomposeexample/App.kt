package at.florianschuster.control.androidcountercomposeexample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.compose.Composable
import androidx.compose.getValue
import androidx.compose.mutableStateOf
import androidx.compose.setValue
import androidx.ui.core.ContextAmbient
import androidx.ui.core.setContent
import androidx.ui.foundation.Text
import androidx.ui.foundation.isSystemInDarkTheme
import androidx.ui.graphics.Color
import androidx.ui.layout.Column
import androidx.ui.material.Button
import androidx.ui.material.ColorPalette
import androidx.ui.material.MaterialTheme
import androidx.ui.material.Scaffold
import androidx.ui.material.TopAppBar
import androidx.ui.material.darkColorPalette
import androidx.ui.material.lightColorPalette
import at.florianschuster.control.ControllerLog
import at.florianschuster.control.kotlincounter.CounterAction
import at.florianschuster.control.kotlincounter.CounterController
import at.florianschuster.control.kotlincounter.CounterMutation
import at.florianschuster.control.kotlincounter.CounterState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow

internal class AppActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // setContent { AppScreen() }
        setContent {
            var lel by mutableStateOf(true)
            Column {
                Button(onClick = { lel = !lel }) { Text(text = "Click me") }
                if (lel) AppScreen()
            }

        }
    }
}

@Composable
private fun AppScreen() {
    val controller: CounterController = ComposableController(
        initialState = CounterState(value = 0, loading = false),
        mutator = { action ->
            when (action) {
                CounterAction.Increment -> flow {
                    emit(CounterMutation.SetLoading(true))
                    delay(500)
                    emit(CounterMutation.IncreaseValue)
                    emit(CounterMutation.SetLoading(false))
                }
                CounterAction.Decrement -> flow {
                    emit(CounterMutation.SetLoading(true))
                    delay(500)
                    emit(CounterMutation.DecreaseValue)
                    emit(CounterMutation.SetLoading(false))
                }
            }
        },
        reducer = { mutation, previousState ->
            when (mutation) {
                is CounterMutation.IncreaseValue -> previousState.copy(value = previousState.value + 1)
                is CounterMutation.DecreaseValue -> previousState.copy(value = previousState.value - 1)
                is CounterMutation.SetLoading -> previousState.copy(loading = mutation.loading)
            }
        },
        controllerLog = ControllerLog.Println
    )
    MaterialTheme(colors = AppColors.currentColorPalette) {
        Scaffold(
            topAppBar = {
                TopAppBar(title = { Text(text = ContextAmbient.current.getString(R.string.app_name)) })
            },
            bodyContent = {
                val controllerState by controller.collectAsState()
                CounterScreen(counterState = controllerState, action = controller::dispatch)
            }
        )
    }
}

internal object AppColors {
    private val primary = Color(0xFF585858)
    private val primaryVariant = Color(0xFF303030)
    private val secondary = Color(0xFFD32F2F)

    @Composable
    val currentColorPalette: ColorPalette
        get() = if (isSystemInDarkTheme()) {
            darkColorPalette(
                primary = primary,
                primaryVariant = primaryVariant,
                secondary = secondary
            )
        } else {
            lightColorPalette(
                primary = primary,
                primaryVariant = primaryVariant,
                secondary = secondary
            )
        }
}