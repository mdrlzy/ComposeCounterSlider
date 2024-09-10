package com.mdrlzy.counterslider

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpSize

@Composable
fun CounterSlider(
    modifier: Modifier = Modifier,
    size: DpSize,
    customization: CounterSliderCustomization = CounterSliderCustomization(),
    colors: CounterSliderColors = CounterSliderColors(),
    value: String,
    onValueDecreaseClick: () -> Unit,
    onValueIncreaseClick: () -> Unit,
    onValueClearClick: () -> Unit,
) {
    val sliderSize by remember {
        derivedStateOf{
            SliderSize(size)
        }
    }
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .width(sliderSize.size.width)
            .height(sliderSize.size.height)
    ) {
        val thumbOffsetX = remember { Animatable(0f) }
        val thumbOffsetY = remember { Animatable(0f) }
        val verticalDragButtonRevealPx = sliderSize.dragClearIconRevealDp.dpToPx()

        SliderContainer(
            sliderSize = sliderSize,
            colors = colors,
            customization = customization,
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
            colors = colors,
            customization = customization,
            thumbOffsetX = thumbOffsetX,
            thumbOffsetY = thumbOffsetY,
            onClick = onValueIncreaseClick,
            onValueDecreaseClick = onValueDecreaseClick,
            onValueIncreaseClick = onValueIncreaseClick,
            onValueReset = onValueClearClick,
            modifier = Modifier.align(Alignment.Center),
            sliderSize = sliderSize
        )
    }
}