package com.griffith

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.griffith.data.AppDatabase
import com.griffith.ui.theme.TasteTheme
import com.griffith.util.Session
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ChangePasswordActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { TasteTheme { ChangePasswordScreen { finish() } } }
    }
}

@Composable
fun ChangePasswordScreen(onDone: () -> Unit) {
    val ctx = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()
    val userDao = remember { AppDatabase.get(ctx).userDao() }

    var current by remember { mutableStateOf("") }
    var newPass by remember { mutableStateOf("") }
    var msg by remember { mutableStateOf<String?>(null) }

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { pad ->
        Column(
            Modifier
                .padding(pad)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Change Password", style = MaterialTheme.typography.titleLarge)

            OutlinedTextField(
                value = current,
                onValueChange = { current = it },
                label = { Text("Current password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = newPass,
                onValueChange = { newPass = it },
                label = { Text("New password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )

            msg?.let { Text(it, color = MaterialTheme.colorScheme.error) }

            Button(
                onClick = {
                    msg = null
                    scope.launch(Dispatchers.IO) {
                        val id = Session.userId(ctx)
                        val u = userDao.byId(id)
                        val ok = (u != null && u.password == current && newPass.isNotBlank())

                        if (ok) {
                            userDao.insert(u.copy(password = newPass))
                            launch(Dispatchers.Main) { onDone() }
                        } else {
                            launch(Dispatchers.Main) { msg = "Password change failed" }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save")
            }
        }
    }
}
