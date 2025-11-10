package com.griffith.ui.theme.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun BoltRating(current: Int?, onSet: (Int) -> Unit) {
    Row {
        (1..5).forEach { n ->
            Text(text = if ((current ?: 0) >= n) "⚡" else "⚪",
                modifier = androidx.compose.ui.Modifier.clickable { onSet(n) })
        }
    }
}