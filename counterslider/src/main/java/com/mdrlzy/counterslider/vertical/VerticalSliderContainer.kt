package com.mdrlzy.counterslider.vertical

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.IntOffset
import com.mdrlzy.counterslider.customization.CounterSliderColors
import com.mdrlzy.counterslider.customization.CounterSliderCustomization
import com.mdrlzy.counterslider.dpToPx
import com.mdrlzy.counterslider.ui.IconControlButton
import kotlin.math.absoluteValue

@Composable
internal fun VerticalSliderContainer(
    sliderSize: VerticalSliderSize,
    colors: CounterSliderColors,
    customization: CounterSliderCustomization,
    thumbOffsetX: Float,
    thumbOffsetY: Float,
    onValueDecreaseClick: () -> Unit,
    onValueIncreaseClick: () -> Unit,
    onValueClearClick: () -> Unit,
    modifier: Modifier = Modifier,
    clearButtonVisible: Boolean = false,
) {
    // at which point the icon should be fully visible
    val horizontalHighlightLimitPx = sliderSize.dragHorizontalIconHighlightDp.dpToPx()
    val verticalHighlightLimitPx = sliderSize.dragVerticalIconHighlightDp.dpToPx()

    Box(
        modifier = modifier
            .offset {
                IntOffset(
                    (thumbOffsetX * customization.containerOffsetFactor).toInt(),
                    (thumbOffsetY * customization.containerOffsetFactor).toInt(),
                )
            }
            .fillMaxSize()
            .clip(RoundedCornerShape(sliderSize.buttonContainerClipDp))
            .background(
                colors.containerColor.copy(
                    alpha = if (thumbOffsetX.absoluteValue > 0.0f) {
                        // horizontal
                        (customization.containerBackgroundAlphaInitial + ((thumbOffsetX.absoluteValue / horizontalHighlightLimitPx) / 20f))
                            .coerceAtMost(customization.containerBackgroundAlphaMax)
                    } else if (thumbOffsetY.absoluteValue > 0.0f) {
                        // vertical
                        (customization.containerBackgroundAlphaInitial + ((thumbOffsetY.absoluteValue / verticalHighlightLimitPx) / 10f))
                            .coerceAtMost(customization.containerBackgroundAlphaMax)
                    } else {
                        customization.containerBackgroundAlphaInitial
                    }
                )
            )
            .padding(horizontal = sliderSize.buttonContainerVerticalPaddingDp)
    ) {
        // decrease button
        IconControlButton(
            modifier = Modifier.align(Alignment.BottomCenter),
            icon = Icons.Outlined.Remove,
            iconButtonSize = sliderSize.iconButtonSizeDp,
            iconSize = sliderSize.iconSizeDp,
            contentDescription = "Decrease count",
            onClick = onValueDecreaseClick,
            enabled = !clearButtonVisible,
            tintColor = colors.iconColor.copy(
                alpha = if (clearButtonVisible) {
                    0.0f
                } else if (thumbOffsetY > 0) {
                    (thumbOffsetY.absoluteValue / horizontalHighlightLimitPx).coerceIn(
                        customization.iconButtonInitialAlpha,
                        1f
                    )
                } else {
                    customization.iconButtonInitialAlpha
                }
            ),
            clickTintColor = colors.clickedIconColor
        )

        // clear button
        if (clearButtonVisible) {
            IconControlButton(
                modifier = Modifier.align(Alignment.Center),
                icon = Icons.Outlined.Clear,
                iconButtonSize = sliderSize.iconButtonSizeDp,
                iconSize = sliderSize.iconSizeDp,
                contentDescription = "Clear count",
                onClick = onValueClearClick,
                enabled = false,
                tintColor = colors.iconColor.copy(
                    alpha = (thumbOffsetX.absoluteValue / verticalHighlightLimitPx).coerceIn(
                        customization.iconButtonInitialAlpha,
                        1f
                    )
                ),
                clickTintColor = colors.clickedIconColor
            )
        }

        // increase button
        IconControlButton(
            modifier = Modifier.align(Alignment.TopCenter),
            icon = Icons.Outlined.Add,
            iconButtonSize = sliderSize.iconButtonSizeDp,
            iconSize = sliderSize.iconSizeDp,
            contentDescription = "Increase count",
            onClick = onValueIncreaseClick,
            enabled = !clearButtonVisible,
            tintColor = colors.iconColor.copy(
                alpha = if (clearButtonVisible) {
                    0.0f
                } else if (thumbOffsetY < 0) {
                    (thumbOffsetY.absoluteValue / horizontalHighlightLimitPx).coerceIn(
                        customization.iconButtonInitialAlpha,
                        1f
                    )
                } else {
                    customization.iconButtonInitialAlpha
                }
            ),
            clickTintColor = colors.clickedIconColor
        )
    }
}