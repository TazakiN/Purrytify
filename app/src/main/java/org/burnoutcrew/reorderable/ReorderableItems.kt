package org.burnoutcrew.reorderable

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex

@Composable
fun ReorderableItem(
    reorderableState: ReorderableLazyListState,
    key: Any,
    modifier: Modifier = Modifier,
    index: Int? = null,
    content: @Composable BoxScope.(isDragging: Boolean) -> Unit
) {
    val isDragging = reorderableState.draggingItemKey == key
    val draggingModifier = if (isDragging) {
        Modifier.zIndex(1f)
    } else {
        Modifier
    }
    Box(
        modifier = modifier
            .then(draggingModifier),
    ) {
        content(isDragging)
    }
}