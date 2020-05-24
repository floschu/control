package at.florianschuster.control.androidcountercomposeexample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.compose.Composable
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

internal class AppActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { App() }
    }
}

@Composable
private fun App() {
    MaterialTheme(colors = AppColors.currentColorPalette) {
        Scaffold(
            topAppBar = {
                TopAppBar(title = { Text(text = ContextAmbient.current.getString(R.string.app_name)) })
            },
            bodyContent = { CounterScreen() }
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