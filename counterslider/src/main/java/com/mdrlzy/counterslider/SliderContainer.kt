package com.mdrlzy.counterslider

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntOffset
import kotlin.math.absoluteValue

@Composable
internal fun SliderContainer(
    sliderSize: SliderSize,
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

    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .offset {
                IntOffset(
                    (thumbOffsetX * CONTAINER_OFFSET_FACTOR).toInt(),
                    (thumbOffsetY * CONTAINER_OFFSET_FACTOR).toInt(),
                )
            }
            .fillMaxSize()
            .clip(RoundedCornerShape(sliderSize.buttonContainerClipDp))
            .background(
                Color.Black.copy(
                    alpha = if (thumbOffsetX.absoluteValue > 0.0f) {
                        // horizontal
                        (CONTAINER_BACKGROUND_ALPHA_INITIAL + ((thumbOffsetX.absoluteValue / horizontalHighlightLimitPx) / 20f))
                            .coerceAtMost(CONTAINER_BACKGROUND_ALPHA_MAX)
                    } else if (thumbOffsetY.absoluteValue > 0.0f) {
                        // vertical
                        (CONTAINER_BACKGROUND_ALPHA_INITIAL + ((thumbOffsetY.absoluteValue / verticalHighlightLimitPx) / 10f))
                            .coerceAtMost(CONTAINER_BACKGROUND_ALPHA_MAX)
                    } else {
                        CONTAINER_BACKGROUND_ALPHA_INITIAL
                    }
                )
            )
            .padding(horizontal = sliderSize.buttonContainerHorizontalPaddingDp)
    ) {
        // decrease button
        IconControlButton(
            icon = Icons.Outlined.Remove,
            sliderSize = sliderSize,
            contentDescription = "Decrease count",
            onClick = onValueDecreaseClick,
            enabled = !clearButtonVisible,
            tintColor = Color.White.copy(
                alpha = if (clearButtonVisible) {
                    0.0f
                } else if (thumbOffsetX < 0) {
                    (thumbOffsetX.absoluteValue / horizontalHighlightLimitPx).coerceIn(
                        ICON_BUTTON_ALPHA_INITIAL,
                        1f
                    )
                } else {
                    ICON_BUTTON_ALPHA_INITIAL
                }
            )
        )

        // clear button
        if (clearButtonVisible) {
            IconControlButton(
                icon = Icons.Outlined.Clear,
                sliderSize = sliderSize,
                contentDescription = "Clear count",
                onClick = onValueClearClick,
                enabled = false,
                tintColor = Color.White.copy(
                    alpha = (thumbOffsetY.absoluteValue / verticalHighlightLimitPx).coerceIn(
                        ICON_BUTTON_ALPHA_INITIAL,
                        1f
                    )
                )
            )
        }

        // increase button
        IconControlButton(
            icon = Icons.Outlined.Add,
            sliderSize = sliderSize,
            contentDescription = "Increase count",
            onClick = onValueIncreaseClick,
            enabled = !clearButtonVisible,
            tintColor = Color.White.copy(
                alpha = if (clearButtonVisible) {
                    0.0f
                } else if (thumbOffsetX > 0) {
                    (thumbOffsetX.absoluteValue / horizontalHighlightLimitPx).coerceIn(
                        ICON_BUTTON_ALPHA_INITIAL,
                        1f
                    )
                } else {
                    ICON_BUTTON_ALPHA_INITIAL
                }
            )
        )
    }
}