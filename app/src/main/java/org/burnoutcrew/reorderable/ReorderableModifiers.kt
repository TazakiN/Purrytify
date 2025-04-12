package org.burnoutcrew.reorderable

import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt

fun Modifier.detectReorder(
    state: ReorderableLazyListState
): Modifier = composed {
    var currentIndex by remember { mutableStateOf<Int?>(null) }

    this.pointerInput(Unit) {
        detectDragGesturesAfterLongPress(
            onDragStart = { offset ->
                state.layoutInfo.visibleItemsInfo.firstOrNull {
                    offset.y.roundToInt() in it.offset..it.offset + it.size
                }?.also {
                    currentIndex = it.index
                    state.onDragStart(offset, it.index)
                }
            },
            onDragEnd = {
                currentIndex = null
                state.onDragInterrupted()
            },
            onDragCancel = {
                currentIndex = null
                state.onDragInterrupted()
            },
            onDrag = { change, dragAmount ->
                change.consume()
                currentIndex?.let { state.onDrag(change.positionChange(), it) }
            })
    }
}

fun Modifier.reorderable(
    state: ReorderableLazyListState
) = this
    .offset {
        val index = state.layoutInfo.visibleItemsInfo.firstOrNull {
            it.key == state.draggingItemKey
        }?.index

        if (index != null) {
            IntOffset(0, state.calculateOffset(index).roundToInt())
        } else {
            IntOffset.Zero
        }
    }

val ReorderableLazyListState.layoutInfo
    get() = listState.layoutInfo