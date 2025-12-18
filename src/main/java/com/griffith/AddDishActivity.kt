// https://github.com/SXaviour/Taste-Journal
package com.griffith

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.imePadding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.griffith.data.AppDatabase
import com.griffith.data.Dish
import com.griffith.data.DishRepository
import com.griffith.ui.theme.TasteTheme
import com.griffith.ui.theme.components.BoltRating
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

    Scaffold(
        topBar = { TopAppBar(title = { Text("Add Dish") }) },
        containerColor = MaterialTheme.colorScheme.background
    ) { pad ->
        Column(
            Modifier
                .padding(pad)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Dish name") },
                modifier = Modifier.fillMaxWidth()
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = kcal,
                    onValueChange = { kcal = it },
                    label = { Text("kcal") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = mins,
                    onValueChange = { mins = it },
                    label = { Text("mins") },
                    modifier = Modifier.weight(1f)
                )
            }

            OutlinedTextField(
                value = ingredients,
                onValueChange = { ingredients = it },
                label = { Text("Ingredients (one per line)") },
                minLines = 3,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = steps,
                onValueChange = { steps = it },
                label = { Text("Steps") },
                minLines = 3,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = link,
                onValueChange = { link = it },
                label = { Text("Source link (optional)") },
                modifier = Modifier.fillMaxWidth()
            )

            Text("Rating", color = MaterialTheme.colorScheme.onBackground)
            BoltRating(current = rating, onSet = { rating = it })


            PhotoPickerBox(
                hasImage = imageUri != null,
                onPick = {
                    picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                }
            )

            Button(
                enabled = name.isNotBlank(),
                onClick = {
                    CoroutineScope(Dispatchers.IO).launch {
                        val stableUri = imageUri?.let { raw -> saveImage(ctx, Uri.parse(raw)) }

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
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Text("Save")

            }
        }
    }
}

@Composable
private fun PhotoPickerBox(hasImage: Boolean, onPick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clip(RoundedCornerShape(14.dp))
            .dashedBorder(
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.35f),
                radius = 14.dp
            )
            .clickable(onClick = onPick),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "+",
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.9f),
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(Modifier.height(4.dp))
            Text(
                if (hasImage) "Change photo" else "Add photo",
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.75f),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

private fun Modifier.dashedBorder(color: Color, radius: Dp): Modifier = this.then(
    Modifier.drawBehind {
        val stroke = Stroke(
            width = 3f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f), 0f)
        )
        drawRoundRect(
            color = color,
            size = size,
            cornerRadius = CornerRadius(radius.toPx(), radius.toPx()),
            style = stroke
        )
    }
)
