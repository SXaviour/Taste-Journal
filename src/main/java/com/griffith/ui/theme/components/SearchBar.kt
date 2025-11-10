package com.griffith.ui.theme.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SearchBar(
    value: String,
    onChange: (String) -> Unit,
    onSearch: () -> Unit,
    onFilter: () -> Unit
) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = value,
            onValueChange = onChange,
            placeholder = { Text("Explore your dishes …..") },
            singleLine = true,
            modifier = Modifier.weight(1f)
        )
        FilledTonalButton(onClick = onSearch) { Text("🔎") }
        Button(onClick = onFilter) { Text("≡") }
    }
}