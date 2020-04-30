package at.florianschuster.control.countercomposeexample

import android.os.Bundle
import androidx.compose.Composable
import androidx.compose.getValue
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import androidx.ui.core.ContextAmbient
import androidx.ui.core.setContent
import androidx.ui.foundation.Text
import androidx.ui.foundation.isSystemInDarkTheme
import androidx.ui.graphics.Color
import androidx.ui.material.ColorPalette
import androidx.ui.material.MaterialTheme
import androidx.ui.material.Scaffold
import androidx.ui.material.TopAppBar
import androidx.ui.material.darkColorPalette
import androidx.ui.material.lightColorPalette

class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val controller = CounterController(lifecycleScope)

        setContent {
            MaterialTheme(colors = Colors.currentColorPalette) {
                Scaffold(
                    topAppBar = {
                        TopAppBar(
                            title = {
                                val context = ContextAmbient.current
                                Text(text = context.getString(R.string.app_name))
                            }
                        )
                    },
                    bodyContent = {
                        val counterState by controller.state.collectAsState(controller.currentState)
                        CounterScreen(counterState, controller::dispatch)
                    }
                )
            }
        }
    }
}

internal object Colors {
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
