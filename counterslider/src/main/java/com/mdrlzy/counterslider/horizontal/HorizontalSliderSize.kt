package com.mdrlzy.counterslider.horizontal

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times

// The magic numbers are calculated based on sizes from original code
internal class HorizontalSliderSize(
    val size: DpSize
) {
    val thumbButtonSizeDp = 0.8 * size.height
    val dragLimitHorizontalDp = ((size.width - thumbButtonSizeDp) / 2) * 1.06f
    val dragLimitVerticalDp = 0.8 * size.height
    val iconButtonSizeDp = 0.6 * size.height
    val iconSizeDp = 0.4 * size.height
    val dragClearIconRevealDp = 0.025 * size.height
    val startDragThresholdDp = 0.025 * size.height
    val dragHorizontalIconHighlightDp = 0.18 * size.width
    val dragVerticalIconHighlightDp = 0.75 * size.height
    val buttonContainerClipDp = 0.8 * size.height
    val buttonContainerHorizontalPaddingDp = 0.04 * size.width
    val thumbButtonShadowElevationDp = 0.1 * size.height
    val valueFontSizeSp = (0.4 * size.height.value).sp
}