// https://github.com/SXaviour/Taste-Journal
package com.griffith

import android.os.Bundle
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.griffith.data.AppDatabase
import com.griffith.data.Dish
import com.griffith.data.DishRepository
import com.griffith.ui.theme.components.BoltRating
import com.griffith.ui.theme.TasteTheme
import com.griffith.util.saveImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AddDishActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { TasteTheme(dark = true) { AddDishUI() } }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDishUI() {
    val repo = remember { DishRepository(AppDatabase.get(App.app).dishDao()) }
    val ctx = LocalContext.current

    var name by remember { mutableStateOf("") }
    var kcal by remember { mutableStateOf("") }
    var mins by remember { mutableStateOf("") }
    var ingredients by remember { mutableStateOf("") }
    var steps by remember { mutableStateOf("") }
    var link by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<String?>(null) }
    var rating by remember { mutableStateOf<Int?>(null) }

    val picker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        imageUri = uri?.toString()
    }

    Scaffold(topBar = { TopAppBar(title = { Text("Add Dish") }) }) { pad ->
        Column(
            Modifier.padding(pad).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(name, { name = it }, label = { Text("Dish name") }, modifier = Modifier.fillMaxWidth())
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(kcal, { kcal = it }, label = { Text("kcal") }, modifier = Modifier.weight(1f))
                OutlinedTextField(mins, { mins = it }, label = { Text("mins") }, modifier = Modifier.weight(1f))
            }
            OutlinedTextField(ingredients, { ingredients = it }, label = { Text("Ingredients (one per line)") }, minLines = 3, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(steps, { steps = it }, label = { Text("Steps") }, minLines = 3, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(link, { link = it }, label = { Text("Source link (optional)") }, modifier = Modifier.fillMaxWidth())

            Text("Rating")
            BoltRating(current = rating, onSet = { rating = it })

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = {
                    picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                }) {
                    Text("Pick photo")
                }

                Button(
                    enabled = name.isNotBlank(),
                    onClick = {
                        CoroutineScope(Dispatchers.IO).launch {
                            val stableUri = imageUri?.let { raw ->
                                saveImage(ctx, Uri.parse(raw))
                            }
                            repo.save(
                                Dish(
                                    dishName = name.trim(),
                                    kcal = kcal.toIntOrNull(),
                                    cookMinutes = mins.toIntOrNull(),
                                    ingredients = ingredients.trim(),
                                    steps = steps.trim(),
                                    sourceLink = link.ifBlank { null },
                                    imageUri = stableUri,
                                    rating = rating
                                )
                            )
                        }


                        App.app.startActivity(
                            android.content.Intent(App.app, MainActivity::class.java)
                                .addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                        )
                    }
                ) {
                    Text("Save")
                }
            }
        }
    }
}
