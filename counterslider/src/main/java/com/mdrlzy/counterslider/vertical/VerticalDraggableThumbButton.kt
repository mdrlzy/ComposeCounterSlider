package com.mdrlzy.counterslider.vertical

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
internal fun VerticalDraggableThumbButton(
    modifier: Modifier = Modifier,
    sliderSize: VerticalSliderSize,
    colors: CounterSliderColors,
    customization: CounterSliderCustomization,
    allowLeftToReset: Boolean,
    allowRightToReset: Boolean,
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
                        allowLeftToReset,
                        allowRightToReset,
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
    allowLeftToReset: Boolean,
    allowRightToReset: Boolean,
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
                            pointerInputChange.positionChange().y.absoluteValue >= startDragThreshold) ||
                    dragDirection.value == DragDirection.VERTICAL
                ) {
                    // in case of the initial drag
                    if (dragDirection.value == DragDirection.NONE) {
                        counterJob = scope.launch {
                            listenToThumbPosition(
                                scope,
                                customization,
                                thumbOffsetY,
                                dragLimitVerticalPx,
                                onValueDecreaseClick,
                                onValueIncreaseClick
                            )
                        }
                    }

                    handleVerticalDrag(
                        pointerInputChange,
                        thumbOffsetY,
                        dragLimitVerticalPx,
                        dragDirection
                    )
                } else if (
                    (dragDirection.value != DragDirection.VERTICAL &&
                            pointerInputChange.positionChange().x.absoluteValue >= startDragThreshold)
                ) {
                    handleHorizontalDrag(
                        allowLeftToReset,
                        allowRightToReset,
                        pointerInputChange,
                        thumbOffsetX,
                        dragDirection,
                        dragLimitHorizontalPx
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
    thumbOffsetY: Animatable<Float, AnimationVector1D>,
    dragLimitVerticalPx: Float,
    onValueDecreaseClick: () -> Unit,
    onValueIncreaseClick: () -> Unit,
) {
    delay(customization.counterDelayInitialMS)

    val dragLimitVerticalPxWithFactor =
        (dragLimitVerticalPx * customization.dragLimitVerticalThresholdFactor)

    while (
        scope.isActive
    ) {
        if (
            thumbOffsetY.value.absoluteValue >= dragLimitVerticalPxWithFactor
        ) {
            if (thumbOffsetY.value.sign < 0) {
                onValueIncreaseClick()
            } else {
                onValueDecreaseClick()
            }
        }

        delay(customization.counterRepeatDelayMS)
    }
}

private suspend fun handleVerticalDrag(
    pointerInputChange: PointerInputChange,
    thumbOffsetY: Animatable<Float, AnimationVector1D>,
    dragLimitVerticalPx: Float,
    dragDirection: MutableState<DragDirection>,
) {
    // mark horizontal dragging direction to prevent vertical dragging until released
    dragDirection.value =
        DragDirection.VERTICAL

    val delta =
        pointerInputChange.positionChange().y

    val targetValue = thumbOffsetY.value + delta
    val targetValueWithinBounds =
        targetValue.coerceIn(
            -dragLimitVerticalPx,
            dragLimitVerticalPx
        )

    thumbOffsetY.snapTo(targetValueWithinBounds)
}

private suspend fun handleHorizontalDrag(
    allowLeftToReset: Boolean,
    allowRightToReset: Boolean,
    pointerInputChange: PointerInputChange,
    thumbOffsetX: Animatable<Float, AnimationVector1D>,
    dragDirection: MutableState<DragDirection>,
    dragLimitHorizontalPx: Float,
) {
    // mark vertical dragging direction to prevent horizontal dragging until released
    dragDirection.value = DragDirection.HORIZONTAL

    val delta = pointerInputChange.positionChange().x

    val leftBound = if (allowLeftToReset) -dragLimitHorizontalPx else 0f
    val rightBound = if (allowRightToReset) dragLimitHorizontalPx else 0f

    val targetValue = thumbOffsetX.value + delta
    val targetValueWithinBounds =
        targetValue.coerceIn(
            leftBound,
            rightBound
        )

    thumbOffsetX.snapTo(targetValueWithinBounds)
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
    if (thumbOffsetY.value.absoluteValue >= dragLimitVerticalPxWithFactor) {
        if (thumbOffsetY.value.sign < 0) {
            onValueIncreaseClick()
        } else {
            onValueDecreaseClick()
        }
    } else if (thumbOffsetX.value.absoluteValue >= dragLimitHorizontalPxWithFactor) {
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