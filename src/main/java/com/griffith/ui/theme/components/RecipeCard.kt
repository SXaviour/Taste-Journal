package com.griffith.ui.theme.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.griffith.data.Dish

@Composable
fun RecipeCard(d: Dish, onClick: (Dish) -> Unit) {
    Box(
        Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick(d) }
    ) {
        AsyncImage(
            model = d.imageUri,
            contentDescription = d.dishName,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
        )

        // Rating shown top-right
        Row(
            Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
        ) {
            repeat(d.rating ?: 0) {
                Text("⚡")
            }
        }

        // Gradient overlay for text readability
        Box(
            Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .height(64.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Transparent,
                            MaterialTheme.colorScheme.background.copy(alpha = 0.85f)
                        )
                    )
                )
        )

        Column(
            Modifier
                .align(Alignment.BottomStart)
                .padding(10.dp)
        ) {
            Text(
                d.dishName,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Pill(d.kcal?.let { "$it kcal" } ?: "— kcal")
                val time = d.cookMinutes?.let {
                    if (it < 60) "$it mins" else "${it / 60} hr ${it % 60} mins"
                } ?: "— mins"
                Pill(time)
            }
        }
    }
}

@Composable
private fun Pill(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier
            .background(
                MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp)
    )
}
