package org.burnoutcrew.reorderable

import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

data class ItemPosition(val index: Int, val key: Any)

class ReorderableLazyListState(
    val listState: LazyListState,
    private val scope: CoroutineScope,
    private val onMove: (fromPos: ItemPosition, toPos: ItemPosition) -> Unit,
    private val canDragOver: ((draggedOver: ItemPosition, dragging: ItemPosition) -> Boolean)? = null,
) {
    var draggingItemIndex by mutableStateOf<Int?>(null)
    val draggingItemKey: Any?
        get() = draggingItemIndex?.let { itemKeyFor(it) }

    private var draggedDistance by mutableStateOf(0f)
    private var draggingJob by mutableStateOf<Job?>(null)

    fun calculateOffset(index: Int): Float {
        return if (index == draggingItemIndex) draggedDistance else 0f
    }

    fun onDragStart(offset: Offset, index: Int) {
        draggingItemIndex = index
        draggingJob?.cancel()
    }

    fun onDragInterrupted() {
        draggedDistance = 0f
        draggingItemIndex = null
        draggingJob?.cancel()
    }

    fun onDrag(offset: Offset, index: Int) {
        draggingItemIndex?.let {
            draggedDistance += offset.y

            val currentVisibleItems = listState.layoutInfo.visibleItemsInfo
            val currentItem = currentVisibleItems.firstOrNull { it.index == draggingItemIndex }
            val currentPosition = currentItem?.let { ItemPosition(it.index, it.key) } ?: return@let

            val startOffset = currentItem.offset.toFloat()
            val endOffset = startOffset + currentItem.size
            val middleOffset = (startOffset + endOffset) / 2f + draggedDistance

            val targetItem = currentVisibleItems.firstOrNull { itemInRange(it, middleOffset) }

            val targetPosition = targetItem?.let {
                ItemPosition(it.index, it.key)
            }

            if (targetItem != null && targetPosition != null &&
                targetPosition.index != currentPosition.index) {
                if (canDragOver?.invoke(targetPosition, currentPosition) != false) {
                    onMove(currentPosition, targetPosition)
                    draggingItemIndex = targetPosition.index
                    draggedDistance = 0f
                }
            }

            // Auto-scroll
            val listHeight = listState.layoutInfo.viewportEndOffset
            val distFromTop = middleOffset
            val distFromBottom = listHeight - middleOffset

            when {
                distFromTop < 150 -> {
                    autoScroll(-10f)
                }
                distFromBottom < 150 -> {
                    autoScroll(10f)
                }
                else -> {
                    draggingJob?.cancel()
                }
            }
        }
    }

    private fun autoScroll(pixels: Float) {
        draggingJob?.cancel()

        if (pixels != 0f) {
            draggingJob = scope.launch {
                while (true) {
                    listState.scrollBy(pixels)
                    kotlinx.coroutines.delay(10L)
                }
            }
        }
    }

    private fun itemInRange(item: LazyListItemInfo, offset: Float): Boolean {
        return offset >= item.offset && offset <= item.offset + item.size
    }

    private fun itemKeyFor(index: Int) =
        listState.layoutInfo.visibleItemsInfo.firstOrNull { it.index == index }?.key
}

@Composable
fun rememberReorderableLazyListState(
    onMove: (ItemPosition, ItemPosition) -> Unit,
    listState: LazyListState = rememberLazyListState(),
    canDragOver: ((draggedOver: ItemPosition, dragging: ItemPosition) -> Boolean)? = null,
): ReorderableLazyListState {
    val scope = rememberCoroutineScope()
    val state = remember { ReorderableLazyListState(listState, scope, onMove, canDragOver) }
    return state
}