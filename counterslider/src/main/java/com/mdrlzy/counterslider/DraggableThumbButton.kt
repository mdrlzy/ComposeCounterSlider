package com.mdrlzy.counterslider

import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.Spring.StiffnessLow
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.forEachGesture
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.AwaitPointerEventScope
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.mdrlzy.counterslider.COUNTER_DELAY_FAST_MS
import com.mdrlzy.counterslider.COUNTER_DELAY_INITIAL_MS
import com.mdrlzy.counterslider.DRAG_LIMIT_HORIZONTAL_DP
import com.mdrlzy.counterslider.DRAG_LIMIT_HORIZONTAL_THRESHOLD_FACTOR
import com.mdrlzy.counterslider.DRAG_LIMIT_VERTICAL_DP
import com.mdrlzy.counterslider.DRAG_LIMIT_VERTICAL_THRESHOLD_FACTOR
import com.mdrlzy.counterslider.DragDirection
import com.mdrlzy.counterslider.START_DRAG_THRESHOLD_DP
import com.mdrlzy.counterslider.dpToPx
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue
import kotlin.math.sign

@Composable
fun DraggableThumbButton(
    value: String,
    thumbOffsetX: Animatable<Float, AnimationVector1D>,
    thumbOffsetY: Animatable<Float, AnimationVector1D>,
    onClick: () -> Unit,
    onValueDecreaseClick: () -> Unit,
    onValueIncreaseClick: () -> Unit,
    onValueReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dragLimitHorizontalPx = DRAG_LIMIT_HORIZONTAL_DP.dp.dpToPx()
    val dragLimitVerticalPx = DRAG_LIMIT_VERTICAL_DP.dp.dpToPx()
    val startDragThreshold = START_DRAG_THRESHOLD_DP.dp.dpToPx()
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
            .shadow(8.dp, shape = CircleShape)
            .size(64.dp)
            .clip(CircleShape)
            .clickable {
                // only allow clicks while not dragging
                if (thumbOffsetX.value.absoluteValue <= startDragThreshold &&
                    thumbOffsetY.value.absoluteValue <= startDragThreshold
                ) {
                    onClick()
                }
            }
            .background(Color.Gray)
            .pointerInput(Unit) {
                forEachGesture {
                    awaitPointerEventScope {
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
                                                delay(COUNTER_DELAY_INITIAL_MS)

                                                var elapsed = COUNTER_DELAY_INITIAL_MS
                                                while (isActive && thumbOffsetX.value.absoluteValue >= (dragLimitHorizontalPx * DRAG_LIMIT_HORIZONTAL_THRESHOLD_FACTOR)) {
                                                    if (thumbOffsetX.value.sign > 0) {
                                                        onValueIncreaseClick()
                                                    } else {
                                                        onValueDecreaseClick()
                                                    }

                                                    delay(COUNTER_DELAY_FAST_MS)
                                                    elapsed += COUNTER_DELAY_FAST_MS
                                                }
                                            }
                                        }

                                        // mark horizontal dragging direction to prevent vertical dragging until released
                                        dragDirection.value = DragDirection.HORIZONTAL

                                        // calculate the drag factor so the more the thumb
                                        // is closer to the border, the more effort it takes to drag it
                                        val dragFactor =
                                            1 - (thumbOffsetX.value / dragLimitHorizontalPx).absoluteValue
                                        val delta =
                                            pointerInputChange.positionChange().x * dragFactor

                                        val targetValue = thumbOffsetX.value + delta
                                        val targetValueWithinBounds =
                                            targetValue.coerceIn(
                                                -dragLimitHorizontalPx,
                                                dragLimitHorizontalPx
                                            )

                                        thumbOffsetX.snapTo(targetValueWithinBounds)
                                    } else if (
                                        (dragDirection.value != DragDirection.HORIZONTAL &&
                                                pointerInputChange.positionChange().y >= startDragThreshold)
                                    ) {
                                        // mark vertical dragging direction to prevent horizontal dragging until released
                                        dragDirection.value = DragDirection.VERTICAL

                                        val dragFactor =
                                            1 - (thumbOffsetY.value / dragLimitVerticalPx).absoluteValue
                                        val delta =
                                            pointerInputChange.positionChange().y * dragFactor

                                        val targetValue = thumbOffsetY.value + delta
                                        val targetValueWithinBounds =
                                            targetValue.coerceIn(
                                                -dragLimitVerticalPx,
                                                dragLimitVerticalPx
                                            )

                                        thumbOffsetY.snapTo(targetValueWithinBounds)
                                    }
                                }
                            }
                        } while (event.changes.any { it.pressed })

                        counterJob?.cancel()
                    }

                    // detect drag to limit
                    if (thumbOffsetX.value.absoluteValue >= (dragLimitHorizontalPx * DRAG_LIMIT_HORIZONTAL_THRESHOLD_FACTOR)) {
                        if (thumbOffsetX.value.sign > 0) {
                            onValueIncreaseClick()
                        } else {
                            onValueDecreaseClick()
                        }
                    } else if (thumbOffsetY.value.absoluteValue >= (dragLimitVerticalPx * DRAG_LIMIT_VERTICAL_THRESHOLD_FACTOR)) {
                        onValueReset()
                    }

                    scope.launch {
                        if (dragDirection.value == DragDirection.HORIZONTAL && thumbOffsetX.value != 0f) {
                            thumbOffsetX.animateTo(
                                targetValue = 0f,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = StiffnessLow
                                )
                            )
                        } else if (dragDirection.value == DragDirection.VERTICAL && thumbOffsetY.value != 0f) {
                            thumbOffsetY.animateTo(
                                targetValue = 0f,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = StiffnessLow
                                )
                            )
                        }
                    }
                }
            }
    ) {
        Text(
            text = value,
            color = Color.White,
            style = MaterialTheme.typography.headlineLarge,
            textAlign = TextAlign.Center,
        )
    }
}