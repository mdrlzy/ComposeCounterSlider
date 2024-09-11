package com.mdrlzy.counterslider.horizontal

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.Spring.StiffnessLow
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.input.pointer.AwaitPointerEventScope
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import com.mdrlzy.counterslider.customization.CounterSliderColors
import com.mdrlzy.counterslider.customization.CounterSliderCustomization
import com.mdrlzy.counterslider.DragDirection
import com.mdrlzy.counterslider.dpToPx
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue
import kotlin.math.sign

@Composable
internal fun HorizontalDraggableThumbButton(
    modifier: Modifier = Modifier,
    sliderSize: HorizontalSliderSize,
    colors: CounterSliderColors,
    customization: CounterSliderCustomization,
    allowTopToReset: Boolean,
    allowBottomToReset: Boolean,
    value: String,
    thumbOffsetX: Animatable<Float, AnimationVector1D>,
    thumbOffsetY: Animatable<Float, AnimationVector1D>,
    onClick: () -> Unit,
    onValueDecreaseClick: () -> Unit,
    onValueIncreaseClick: () -> Unit,
    onValueReset: () -> Unit,
) {
    val dragLimitHorizontalPx = sliderSize.dragLimitHorizontalDp.dpToPx()
    val dragLimitVerticalPx = sliderSize.dragLimitVerticalDp.dpToPx()
    val startDragThreshold = sliderSize.startDragThresholdDp.dpToPx()
    val scope = rememberCoroutineScope()

    val dragDirection = remember {
        mutableStateOf(DragDirection.NONE)
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            // change the x and y position of the composable
            .offset {
                IntOffset(
                    thumbOffsetX.value.toInt(),
                    thumbOffsetY.value.toInt(),
                )
            }
            .shadow(sliderSize.thumbButtonShadowElevationDp, shape = CircleShape)
            .size(sliderSize.thumbButtonSizeDp)
            .clip(CircleShape)
            .clickable {
                // only allow clicks while not dragging
                if (thumbOffsetX.value.absoluteValue <= startDragThreshold &&
                    thumbOffsetY.value.absoluteValue <= startDragThreshold
                ) {
                    onClick()
                }
            }
            .background(colors.thumbColor)
            .pointerInput(Unit) {
                awaitEachGesture {
                    handlePointerEvents(
                        dragDirection,
                        customization,
                        allowTopToReset,
                        allowBottomToReset,
                        scope,
                        startDragThreshold,
                        dragLimitHorizontalPx,
                        dragLimitVerticalPx,
                        thumbOffsetX,
                        thumbOffsetY,
                        onValueDecreaseClick,
                        onValueIncreaseClick
                    )

                    changeCounterUponRelease(
                        customization,
                        thumbOffsetX,
                        thumbOffsetY,
                        dragLimitHorizontalPx,
                        dragLimitVerticalPx,
                        onValueDecreaseClick,
                        onValueIncreaseClick,
                        onValueReset
                    )

                    animateToStart(scope, dragDirection, thumbOffsetX, thumbOffsetY)
                }
            }
    ) {
        Text(
            text = value,
            color = colors.textColor,
            fontSize = sliderSize.valueFontSizeSp,
            textAlign = TextAlign.Center,
        )
    }
}

private suspend fun AwaitPointerEventScope.handlePointerEvents(
    dragDirection: MutableState<DragDirection>,
    customization: CounterSliderCustomization,
    allowTopToReset: Boolean,
    allowBottomToReset: Boolean,
    scope: CoroutineScope,
    startDragThreshold: Float,
    dragLimitHorizontalPx: Float,
    dragLimitVerticalPx: Float,
    thumbOffsetX: Animatable<Float, AnimationVector1D>,
    thumbOffsetY: Animatable<Float, AnimationVector1D>,
    onValueDecreaseClick: () -> Unit,
    onValueIncreaseClick: () -> Unit,
) {
    awaitFirstDown()

    // reset drag direction
    dragDirection.value = DragDirection.NONE

    var counterJob: Job? = null

    do {
        val event = awaitPointerEvent()
        event.changes.forEach { pointerInputChange ->
            // update logic inside DraggableThumbButton.Modifier.pointerInput
            scope.launch {
                if ((dragDirection.value == DragDirection.NONE &&
                            pointerInputChange.positionChange().x.absoluteValue >= startDragThreshold) ||
                    dragDirection.value == DragDirection.HORIZONTAL
                ) {
                    // in case of the initial drag
                    if (dragDirection.value == DragDirection.NONE) {
                        counterJob = scope.launch {
                            listenToThumbPosition(
                                scope,
                                customization,
                                thumbOffsetX,
                                dragLimitHorizontalPx,
                                onValueDecreaseClick,
                                onValueIncreaseClick
                            )
                        }
                    }

                    handleHorizontalDrag(
                        pointerInputChange,
                        thumbOffsetX,
                        dragLimitHorizontalPx,
                        dragDirection
                    )
                } else if (
                    (dragDirection.value != DragDirection.HORIZONTAL &&
                            pointerInputChange.positionChange().y.absoluteValue >= startDragThreshold)
                ) {
                    handleVerticalDrag(
                        allowTopToReset,
                        allowBottomToReset,
                        pointerInputChange,
                        thumbOffsetY,
                        dragDirection,
                        dragLimitVerticalPx
                    )
                }
            }
        }
    } while (event.changes.any { it.pressed })

    counterJob?.cancel()
}

private suspend fun listenToThumbPosition(
    scope: CoroutineScope,
    customization: CounterSliderCustomization,
    thumbOffsetX: Animatable<Float, AnimationVector1D>,
    dragLimitHorizontalPx: Float,
    onValueDecreaseClick: () -> Unit,
    onValueIncreaseClick: () -> Unit,
) {
    delay(customization.counterDelayInitialMS)

    val dragLimitHorizontalPxWithFactor =
        (dragLimitHorizontalPx * customization.dragLimitHorizontalThresholdFactor)

    while (
        scope.isActive
    ) {
        if (
            thumbOffsetX.value.absoluteValue >= dragLimitHorizontalPxWithFactor
        ) {
            if (thumbOffsetX.value.sign > 0) {
                onValueIncreaseClick()
            } else {
                onValueDecreaseClick()
            }
        }

        delay(customization.counterRepeatDelayMS)
    }
}

private suspend fun handleHorizontalDrag(
    pointerInputChange: PointerInputChange,
    thumbOffsetX: Animatable<Float, AnimationVector1D>,
    dragLimitHorizontalPx: Float,
    dragDirection: MutableState<DragDirection>,
) {
    // mark horizontal dragging direction to prevent vertical dragging until released
    dragDirection.value =
        DragDirection.HORIZONTAL

    val delta =
        pointerInputChange.positionChange().x

    val targetValue = thumbOffsetX.value + delta
    val targetValueWithinBounds =
        targetValue.coerceIn(
            -dragLimitHorizontalPx,
            dragLimitHorizontalPx
        )

    thumbOffsetX.snapTo(targetValueWithinBounds)
}

private suspend fun handleVerticalDrag(
    allowTopToReset: Boolean,
    allowBottomToReset: Boolean,
    pointerInputChange: PointerInputChange,
    thumbOffsetY: Animatable<Float, AnimationVector1D>,
    dragDirection: MutableState<DragDirection>,
    dragLimitVerticalPx: Float,
) {
    // mark vertical dragging direction to prevent horizontal dragging until released
    dragDirection.value = DragDirection.VERTICAL

    val delta = pointerInputChange.positionChange().y

    val topBound = if (allowTopToReset) -dragLimitVerticalPx else 0f
    val bottomBound = if (allowBottomToReset) dragLimitVerticalPx else 0f

    val targetValue = thumbOffsetY.value + delta
    val targetValueWithinBounds =
        targetValue.coerceIn(
            topBound,
            bottomBound
        )

    thumbOffsetY.snapTo(targetValueWithinBounds)
}

private fun changeCounterUponRelease(
    customization: CounterSliderCustomization,
    thumbOffsetX: Animatable<Float, AnimationVector1D>,
    thumbOffsetY: Animatable<Float, AnimationVector1D>,
    dragLimitHorizontalPx: Float,
    dragLimitVerticalPx: Float,
    onValueDecreaseClick: () -> Unit,
    onValueIncreaseClick: () -> Unit,
    onValueReset: () -> Unit,
) {
    val dragLimitHorizontalPxWithFactor =
        (dragLimitHorizontalPx * customization.dragLimitHorizontalThresholdFactor)
    val dragLimitVerticalPxWithFactor =
        (dragLimitVerticalPx * customization.dragLimitVerticalThresholdFactor)

    // detect drag to limit
    if (thumbOffsetX.value.absoluteValue >= dragLimitHorizontalPxWithFactor) {
        if (thumbOffsetX.value.sign > 0) {
            onValueIncreaseClick()
        } else {
            onValueDecreaseClick()
        }
    } else if (thumbOffsetY.value.absoluteValue >= dragLimitVerticalPxWithFactor) {
        onValueReset()
    }
}

private fun animateToStart(
    scope: CoroutineScope,
    dragDirection: MutableState<DragDirection>,
    thumbOffsetX: Animatable<Float, AnimationVector1D>,
    thumbOffsetY: Animatable<Float, AnimationVector1D>,
) = scope.launch {
    if (dragDirection.value == DragDirection.HORIZONTAL && thumbOffsetX.value != 0f) {
        thumbOffsetX.animateTo(
            targetValue = 0f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = StiffnessLow
            )
        )
    } else if (dragDirection.value == DragDirection.VERTICAL && thumbOffsetY.value != 0f) {
        thumbOffsetY.animateTo(
            targetValue = 0f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = StiffnessLow
            )
        )
    }
}