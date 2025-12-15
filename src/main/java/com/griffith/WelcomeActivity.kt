package com.griffith

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.griffith.ui.theme.TasteTheme

class WelcomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TasteTheme {
                WelcomeScreen(
                    // routing to auth activity
                    onGetStarted = {
                        startActivity(
                            Intent(this, AuthActivity::class.java)
                                .putExtra("mode", "register")
                        )
                    },
                            onLogin = {
                                startActivity(
                                    Intent(this, AuthActivity::class.java)
                                        .putExtra("mode", "login"))
                    }
                )
            }
        }
    }
}

@Composable
fun WelcomeScreen(
    onGetStarted: () -> Unit,
    onLogin: () -> Unit
) {
    val pager = rememberPagerState(initialPage = 0) { 2 }

    Box(Modifier.fillMaxSize()) {
        VideoBackground(resId = R.raw.welcome)

        Box(
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background.copy(alpha = 0.25f))
        )

        Column(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(70.dp))

            Text(
                "Welcome",
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                "Taste Journal",
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold)
            )

            Spacer(Modifier.weight(1f))

            HorizontalPager(
                state = pager,
                userScrollEnabled = true,
                modifier = Modifier.fillMaxWidth()
            ) { page ->
                if (page == 0) {
                    WelcomeCopy(
                        title = "Capture the moment",
                        subtitle = "Because great meals deserve\nto be remembered"
                    )
                } else {
                    WelcomeCopy(
                        title = "Keep your favourites close",
                        subtitle = "Save recipes, photos and ratings\nin one place"
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Dot(active = pager.currentPage == 0)
                Dot(active = pager.currentPage == 1)
            }

            Spacer(Modifier.height(22.dp))

            Button(
                onClick = {
                    if (pager.currentPage == 0) onGetStarted() else onLogin()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp),
                shape = RoundedCornerShape(30.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onBackground
                )
            ) {
                Text(
                    if (pager.currentPage == 0) "Get Started" else "Log In",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                )
            }

            Spacer(Modifier.height(44.dp))
        }
    }
}

@Composable
private fun WelcomeCopy(title: String, subtitle: String) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            title,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(6.dp))
        Text(
            subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.85f),
            textAlign = TextAlign.Center
        )
    }
}


@Composable
private fun Dot(active: Boolean) {
    Surface(
        modifier = Modifier
            .size(8.dp)
            .clip(CircleShape),
        color = if (active) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.45f),
        shape = CircleShape
    ) {}
}
