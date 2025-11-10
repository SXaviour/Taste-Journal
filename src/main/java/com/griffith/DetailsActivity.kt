package com.griffith

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.griffith.data.AppDatabase
import com.griffith.data.Dish
import com.griffith.data.DishRepository
import com.griffith.ui.theme.components.BoltRating
import com.griffith.ui.theme.TasteTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*



class DetailsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val id = intent.getLongExtra("id", 0L)
        setContent { TasteTheme(dark = false) { DetailsUI(id) } }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsUI(id: Long) {
    val repo = remember { DishRepository(AppDatabase.get(App.app).dishDao()) }
    val dish by repo.byId(id).collectAsState(initial = null)
    if (dish == null) { CircularProgressIndicator(); return }
    val d: Dish = dish!!

    Scaffold(
        topBar = { TopAppBar(title = { Text(d.dishName, maxLines = 1, overflow = TextOverflow.Ellipsis) }) },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                val text = "🍽 ${d.dishName}\n${d.sourceLink ?: ""}\n\nIngredients:\n${d.ingredients}\n\nSteps:\n${d.steps}"
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, text)
                }
                App.app.startActivity(Intent.createChooser(intent, "Share recipe").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            }) { Text("↗") }
        }
    ) { pad ->
        Column(Modifier.padding(pad).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            AsyncImage(model = d.imageUri, contentDescription = d.dishName, modifier = Modifier.fillMaxWidth().height(220.dp))
            val fmt = SimpleDateFormat("EEEE, MMM d, yyyy • h:mm a", Locale.getDefault())
            Text(fmt.format(Date(d.dateCooked)), style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("${d.kcal ?: 0} kcal"); Text("${d.cookMinutes ?: 0} mins")
            }
            BoltRating(current = d.rating) { r ->
                CoroutineScope(Dispatchers.IO).launch { repo.save(d.copy(rating = r)) }
            }
            Text("Ingredients:\n${d.ingredients}")
            Text("Steps:\n${d.steps}")
            d.sourceLink?.let { link ->
                TextButton(onClick = {
                    App.app.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(link)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                }) { Text("Open source") }
            }
        }
    }
}