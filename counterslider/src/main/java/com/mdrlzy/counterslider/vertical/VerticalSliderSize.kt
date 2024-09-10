package com.mdrlzy.counterslider.vertical

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times

internal class VerticalSliderSize(
    val size: DpSize
) {
    val thumbButtonSizeDp = 0.8 * size.width
    val dragLimitHorizontalDp = 0.8 * size.width
    val dragLimitVerticalDp = ((size.height - thumbButtonSizeDp) / 2) * 1.06f
    val iconButtonSizeDp = 0.6 * size.width
    val iconSizeDp = 0.4 * size.width
    val dragClearIconRevealDp = 0.025 * size.width
    val startDragThresholdDp = 0.025 * size.width
    val dragHorizontalIconHighlightDp = 0.75 * size.width
    val dragVerticalIconHighlightDp = 0.18 * size.height
    val buttonContainerClipDp = 0.8 * size.width
    val buttonContainerVerticalPaddingDp = 0.04 * size.height
    val thumbButtonShadowElevationDp = 0.1 * size.width
    val valueFontSizeSp = (0.4 * size.width.value).sp
}