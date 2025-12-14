// https://github.com/SXaviour/Taste-Journal
package com.griffith

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.griffith.data.AppDatabase
import com.griffith.data.Dish
import com.griffith.data.DishRepository
import com.griffith.ui.theme.TasteTheme
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


// removed hard coded colors
class DetailsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val id = intent.getLongExtra("id", 0L)
        setContent {
            TasteTheme {
                DetailsScreen(id)
            }
        }
    }
}

@Composable
fun DetailsScreen(id: Long) {
    val repo = remember { DishRepository(AppDatabase.get(App.app).dishDao()) }
    val scope = rememberCoroutineScope()
    var dish by remember { mutableStateOf<Dish?>(null) }

    // Observe dish from database
    LaunchedEffect(id) {
        repo.byId(id).collectLatest { dish = it }
    }

    val d = dish ?: return
    val ctx = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { pad ->
        Column(
            Modifier
                .padding(pad)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            AsyncImage(
                model = d.imageUri,
                contentDescription = d.dishName,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
            )

            Column(
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 20.dp)
            ) {
                Text(
                    d.dishName,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                val subtitle = listOfNotNull(d.mealType, d.cuisine).joinToString(" | ")
                if (subtitle.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                    )
                }

                d.sourceLink?.let { link ->
                    Spacer(Modifier.height(4.dp))
                    Text(
                        link,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        textDecoration = TextDecoration.Underline,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .clickable {
                                ctx.startActivity(
                                    Intent(Intent.ACTION_VIEW, Uri.parse(link))
                                )
                            }
                    )
                }

                Spacer(Modifier.height(16.dp))

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    StatCard(
                        icon = "🔥",
                        label = "${d.kcal ?: 0} Kcal",
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        icon = "⏱️",
                        label = d.cookMinutes?.let {
                            if (it < 60) "$it mins" else "${it / 60} hr ${it % 60} mins"
                        } ?: "0 mins",
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(Modifier.height(12.dp))

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    d.sourceLink?.let { link ->
                        TextButton(onClick = {
                            ctx.startActivity(
                                Intent(Intent.ACTION_VIEW, Uri.parse(link))
                            )
                        }) {
                            Text("Open source")
                        }
                    }

                    TextButton(onClick = {
                        val text = buildString {
                            appendLine(d.dishName)
                            d.sourceLink?.let { appendLine(it) }
                            appendLine()
                            appendLine("Ingredients:")
                            appendLine(d.ingredients)
                            appendLine()
                            appendLine("Instructions:")
                            appendLine(d.steps)
                        }
                        ctx.startActivity(
                            Intent.createChooser(
                                Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, text)
                                },
                                "Share recipe"
                            )
                        )
                    }) {
                        Text("Share")
                    }

                    Spacer(Modifier.weight(1f))

                    // Delete dish from database
                    val activity = ctx as? ComponentActivity
                    Button(
                        onClick = {
                            scope.launch {
                                repo.delete(d)
                                activity?.finish()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        )
                    ) {
                        Text("Delete")
                    }
                }

                Spacer(Modifier.height(12.dp))

                Box(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TabChip("Ingredients", selectedTab == 0) { selectedTab = 0 }
                        TabChip("Instructions", selectedTab == 1) { selectedTab = 1 }
                        TabChip("Notes", selectedTab == 2) { selectedTab = 2 }
                    }
                }

                Spacer(Modifier.height(16.dp))

                Box(
                    Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(16.dp)
                ) {
                    when (selectedTab) {
                        0 -> IngredientsList(d.ingredients)
                        1 -> InstructionsText(d.steps)
                        else -> NotesText(d.notes)
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(icon: String, label: String, modifier: Modifier = Modifier) {
    Column(
        modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(icon, fontSize = MaterialTheme.typography.titleLarge.fontSize)
        Spacer(Modifier.height(8.dp))
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun TabChip(title: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(
                if (selected) MaterialTheme.colorScheme.primary
                else Color.Transparent
            )
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            title,
            style = MaterialTheme.typography.bodySmall,
            color = if (selected)
                MaterialTheme.colorScheme.onPrimary
            else
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun IngredientsList(raw: String) {
    val items = raw.split("\n").map { it.trim() }.filter { it.isNotEmpty() }
    LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        items(items) { line ->
            Row {
                Text("• ", color = MaterialTheme.colorScheme.onSurface)
                Text(
                    line,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun InstructionsText(raw: String) {
    Text(
        raw.ifBlank { "No instructions added yet." },
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface
    )
}

@Composable
private fun NotesText(raw: String?) {
    Text(
        raw?.takeIf { it.isNotBlank() } ?: "No notes added yet.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface
    )
}

