package at.florianschuster.control.countercomposeexample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.compose.Composable
import androidx.compose.getValue
import androidx.lifecycle.lifecycleScope
import androidx.ui.core.ContextAmbient
import androidx.ui.core.LifecycleOwnerAmbient
import androidx.ui.core.setContent
import androidx.ui.foundation.Text
import androidx.ui.material.MaterialTheme
import androidx.ui.material.Scaffold
import androidx.ui.material.TopAppBar

internal class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MainScreen() }
    }
}

@Composable
private fun MainScreen() {
    val controller = CounterController(LifecycleOwnerAmbient.current.lifecycleScope)

    MaterialTheme(colors = AppColors.currentColorPalette) {
        Scaffold(
            topAppBar = {
                TopAppBar(title = { Text(text = ContextAmbient.current.getString(R.string.app_name)) })
            },
            bodyContent = {
                val state by controller.collectAsState()
                CounterScreen(counterState = state, action = controller::dispatch)
            }
        )
    }
}