package com.griffith

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.SupportAgent
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.griffith.data.AppDatabase
import com.griffith.data.DishRepository
import com.griffith.data.User
import com.griffith.util.Session
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(pad: PaddingValues) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    // db
    val db = remember { AppDatabase.get(ctx) }
    val userDao = remember { db.userDao() }
    val repo = remember { DishRepository(db.dishDao()) }

    var user by remember { mutableStateOf<User?>(null) }


    var showClearDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }


    LaunchedEffect(Unit) {
        val id = Session.userId(ctx)
        if (id != 0L) {
            user = userDao.byId(id)
        }
    }

    Column(
        Modifier
            .padding(pad)
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Text(
            "Profile",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onBackground
        )

        Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.avatar),
                    contentDescription = null,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )

                Spacer(Modifier.width(12.dp))

                // user details
                Column(Modifier.weight(1f)) {
                    Text(
                        user?.name ?: "User",
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Text(
                        user?.email ?: "",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                // edit icon inactive for now
                Icon(
                    imageVector = Icons.Outlined.Edit,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(20.dp)
                )
            }
        }


        ReviewCard {
            // linked to playstore
            val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/games?hl=en")
            )
            ctx.startActivity(intent)
        }


        Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .height(230.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.profile_banner),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }


        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(vertical = 6.dp)) {

                // open custom app site built for this project
                SettingRow(
                    icon = Icons.Outlined.SupportAgent,
                    title = "Contact Us",
                    titleColor = MaterialTheme.colorScheme.onSurface
                ) {
                    val intent = Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://sxaviour.github.io/Contact-Us-site-taste-journal/")
                    )
                    ctx.startActivity(intent)
                }


                SettingRow(
                    icon = Icons.Outlined.Lock,
                    title = "Change Password",
                    titleColor = MaterialTheme.colorScheme.onSurface
                ) {
                    ctx.startActivity(Intent(ctx, ChangePasswordActivity::class.java))
                }


                SettingRow(
                    icon = Icons.Outlined.DeleteOutline,
                    title = "Clear Cooking History",
                    titleColor = MaterialTheme.colorScheme.onSurface
                ) {
                    showClearDialog = true
                }

                // logout and clear session
                SettingRow(
                    icon = Icons.AutoMirrored.Outlined.Logout,
                    title = "Log Out",
                    titleColor = MaterialTheme.colorScheme.error
                ) {
                    showLogoutDialog = true
                }
            }
        }
    }

    // clear history dialog
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Clear history?") },
            text = { Text("This will delete all saved dishes.") },
            confirmButton = {
                TextButton(onClick = {
                    showClearDialog = false
                    scope.launch(Dispatchers.IO) { repo.clear() }
                }) { Text("Clear") }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) { Text("Cancel") }
            }
        )
    }


    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Log out?") },
            text = { Text("You’ll need to log in again next time.") },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutDialog = false
                    Session.clear(ctx)
                    ctx.startActivity(
                        Intent(ctx, WelcomeActivity::class.java)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    )
                }) { Text("Log out") }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun ReviewCard(onClick: () -> Unit) {

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = null,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    "Leave us a Review",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    repeat(5) {
                        Text(
                            "☆",
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingRow(
    icon: ImageVector,
    title: String,
    titleColor: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = titleColor, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(12.dp))
        Text(
            title,
            color = titleColor,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            modifier = Modifier.weight(1f)
        )
        Icon(
            Icons.Outlined.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
    }
}
