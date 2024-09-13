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
import androidx.compose.ui.unit.dp
import com.mdrlzy.counterslider.customization.CounterSliderColors
import com.mdrlzy.counterslider.customization.CounterSliderCustomization
import com.mdrlzy.counterslider.horizontal.HorizontalSliderSize
import com.mdrlzy.counterslider.horizontal.HorizontalDraggableThumbButton
import com.mdrlzy.counterslider.horizontal.HorizontalSliderContainer
import kotlin.math.absoluteValue

@Composable
fun HorizontalCounterSlider(
    modifier: Modifier = Modifier,
    size: DpSize = DpSize(200.dp, 80.dp),
    customization: CounterSliderCustomization = CounterSliderCustomization(),
    colors: CounterSliderColors = CounterSliderColors(),
    allowTopToReset: Boolean = true,
    allowBottomToReset: Boolean = true,
    value: String,
    onValueDecreaseClick: () -> Unit,
    onValueIncreaseClick: () -> Unit,
    onValueClearClick: () -> Unit,
) {
    val sliderSize by remember {
        derivedStateOf{
            HorizontalSliderSize(size)
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

        HorizontalSliderContainer(
            sliderSize = sliderSize,
            colors = colors,
            customization = customization,
            thumbOffsetX = thumbOffsetX.value,
            thumbOffsetY = thumbOffsetY.value,
            onValueDecreaseClick = onValueDecreaseClick,
            onValueIncreaseClick = onValueIncreaseClick,
            onValueClearClick = onValueClearClick,
            clearButtonVisible = thumbOffsetY.value.absoluteValue >= verticalDragButtonRevealPx,
            modifier = Modifier
        )

        HorizontalDraggableThumbButton(
            modifier = Modifier.align(Alignment.Center),
            sliderSize = sliderSize,
            allowTopToReset = allowTopToReset,
            allowBottomToReset = allowBottomToReset,
            value = value,
            colors = colors,
            customization = customization,
            thumbOffsetX = thumbOffsetX,
            thumbOffsetY = thumbOffsetY,
            onClick = onValueIncreaseClick,
            onValueDecreaseClick = onValueDecreaseClick,
            onValueIncreaseClick = onValueIncreaseClick,
            onValueReset = onValueClearClick,
        )
    }
}