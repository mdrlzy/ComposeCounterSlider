package com.mdrlzy.counterslider

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp

@Composable
internal fun Dp.dpToPx() = with(LocalDensity.current) { this@dpToPx.toPx() }

internal enum class DragDirection {
    NONE, HORIZONTAL, VERTICAL
}