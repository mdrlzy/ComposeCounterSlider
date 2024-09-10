package com.mdrlzy.counterslider

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun CounterSlider(
    value: String,
    onValueDecreaseClick: () -> Unit,
    onValueIncreaseClick: () -> Unit,
    onValueClearClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .width(200.dp)
            .height(80.dp)
    ) {
        val thumbOffsetX = remember { Animatable(0f) }
        val thumbOffsetY = remember { Animatable(0f) }
        val verticalDragButtonRevealPx = DRAG_CLEAR_ICON_REVEAL_DP.dp.dpToPx()

        ButtonContainer(
            thumbOffsetX = thumbOffsetX.value,
            thumbOffsetY = thumbOffsetY.value,
            onValueDecreaseClick = onValueDecreaseClick,
            onValueIncreaseClick = onValueIncreaseClick,
            onValueClearClick = onValueClearClick,
            clearButtonVisible = thumbOffsetY.value >= verticalDragButtonRevealPx,
            modifier = Modifier
        )

        DraggableThumbButton(
            value = value,
            thumbOffsetX = thumbOffsetX,
            thumbOffsetY = thumbOffsetY,
            onClick = onValueIncreaseClick,
            onValueDecreaseClick = onValueDecreaseClick,
            onValueIncreaseClick = onValueIncreaseClick,
            onValueReset = onValueClearClick,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}