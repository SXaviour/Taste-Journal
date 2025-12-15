package com.griffith.ui.theme.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.griffith.data.Dish
import kotlinx.coroutines.delay

enum class AutoScrollDirection { LeftToRight, RightToLeft }

@Composable
fun TimelineSection(
    title: String,
    data: List<Dish>,
    direction: AutoScrollDirection,
    onOpen: (Dish) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onBackground
        )

        AutoScrollingRow(
            items = data,
            direction = direction,
            onOpen = onOpen
        )
    }
}

@Composable
private fun AutoScrollingRow(
    items: List<Dish>,
    direction: AutoScrollDirection,
    onOpen: (Dish) -> Unit
) {
    val state = rememberLazyListState()

    LaunchedEffect(items, direction) {
        if (items.isEmpty()) return@LaunchedEffect

        if (direction == AutoScrollDirection.LeftToRight) {
            state.scrollToItem(items.lastIndex.coerceAtLeast(0))
        }

        while (true) {
            delay(1200L)

            val current = state.firstVisibleItemIndex
            val lastIndex = items.lastIndex.coerceAtLeast(0)

            if (direction == AutoScrollDirection.RightToLeft) {
                if (current >= lastIndex) state.scrollToItem(0) else state.animateScrollToItem(current + 1)
            } else {
                if (current <= 0) state.scrollToItem(lastIndex) else state.animateScrollToItem(current - 1)
            }
        }
    }

    LazyRow(
        state = state,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        items(items) { d ->
            TimelineCard(d, onOpen)
        }
    }
}

@Composable
private fun TimelineCard(d: Dish, onOpen: (Dish) -> Unit) {
    Column(
        Modifier
            .width(120.dp)
            .clickable { onOpen(d) }
    ) {
        AsyncImage(
            model = d.imageUri,
            contentDescription = d.dishName,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .height(120.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
        )

        Spacer(Modifier.height(8.dp))

        Text(
            d.dishName,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(Modifier.height(2.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                d.kcal?.let { "$it kcal" } ?: "— kcal",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
            Text(
                d.cookMinutes?.let { "$it mins" } ?: "— mins",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
        }
    }
}
