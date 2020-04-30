package at.florianschuster.control.countercomposeexample

import androidx.compose.Composable
import androidx.ui.foundation.isSystemInDarkTheme
import androidx.ui.graphics.Color
import androidx.ui.material.ColorPalette
import androidx.ui.material.darkColorPalette
import androidx.ui.material.lightColorPalette

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
