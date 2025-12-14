// https://github.com/SXaviour/Taste-Journal
package com.griffith

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.*
import com.griffith.data.AppDatabase
import com.griffith.data.DishRepository
import com.griffith.ui.theme.TasteTheme
import kotlinx.coroutines.delay

class ShuffleActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TasteTheme {
                ShuffleScreen()
            }
        }
    }
}

@Composable
fun ShuffleScreen() {
    val ctx = LocalContext.current
    val repo = remember { DishRepository(AppDatabase.get(App.app).dishDao()) }

    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.cooking)
    )
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever
    )

    // Pick random dish after short delay
    LaunchedEffect(Unit) {
        val dish = repo.random()
        if (dish != null) {
            delay(1500L)
            ctx.startActivity(
                Intent(ctx, DetailsActivity::class.java)
                    .putExtra("id", dish.id)
            )
            (ctx as? Activity)?.finish()
        } else {
            delay(800L)
            (ctx as? Activity)?.finish()
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .aspectRatio(1f),
            shape = RoundedCornerShape(24.dp),
            // removed hard coded colors
            color = MaterialTheme.colorScheme.surface
        ) {
            Box(
                Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                if (composition != null) {
                    LottieAnimation(
                        composition = composition,
                        progress = { progress },
                        modifier = Modifier.fillMaxSize(0.8f)
                    )
                } else {
                    Text(
                        "Shuffling...",
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}


