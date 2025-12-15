package com.griffith

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.griffith.data.AppDatabase
import com.griffith.data.User
import com.griffith.ui.theme.TasteTheme
import com.griffith.util.Session
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// modes for login / register
private const val EXTRA_MODE = "mode"
private const val MODE_LOGIN = "login"
private const val MODE_REGISTER = "register"

class AuthActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { TasteTheme { AuthScreen() } }
    }
}

@Composable
fun AuthScreen() {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val userDao = remember { AppDatabase.get(ctx).userDao() }

    val startMode = (LocalContext.current as? ComponentActivity)
        ?.intent
        ?.getStringExtra(EXTRA_MODE)

    var isLogin by remember {
        mutableStateOf(startMode != MODE_REGISTER)
    }


    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.auth_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )

        Box(
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background.copy(alpha = 0.2f))
        )

        Column(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 26.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(483.dp))

            Text(
                if (isLogin) "Welcome back" else "Welcome",
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)
            )

            Spacer(Modifier.height(15.dp))

            if (!isLogin) {
                AuthField(
                    value = name,
                    onValue = { name = it },
                    placeholder = "Name"
                )
                Spacer(Modifier.height(14.dp))
            }

            AuthField(
                value = email,
                onValue = { email = it },
                placeholder = "Email"
            )

            Spacer(Modifier.height(14.dp))

            AuthField(
                value = password,
                onValue = { password = it },
                placeholder = "Password",
                isPassword = true
            )

            error?.let {
                Spacer(Modifier.height(10.dp))
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(Modifier.height(26.dp))

            Button(
                enabled = !loading,
                onClick = {
                    error = null
                    loading = true

                    scope.launch(Dispatchers.IO) {
                        val e = email.trim()
                        val p = password

                        val result = if (isLogin) {
                            if (e.isBlank() || p.isBlank()) null else userDao.login(e, p)
                        } else {
                            val n = name.trim()
                            if (n.isBlank() || e.isBlank() || p.isBlank()) null
                            else {
                                try {
                                    val id = userDao.insert(User(name = n, email = e, password = p))
                                    User(id = id, name = n, email = e, password = p)
                                } catch (_: Exception) {
                                    null
                                }
                            }
                        }

                        launch(Dispatchers.Main) {
                            loading = false
                            if (result != null) {
                                Session.setUser(ctx, result.id)
                                ctx.startActivity(Intent(ctx, MainActivity::class.java))
                                (ctx as? ComponentActivity)?.finish()
                            } else {
                                error = if (isLogin) "Invalid email or password"
                                else "Registration failed (email may already exist)"
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onBackground
                )
            ) {
                Text(if (isLogin) "Log In" else "Register", fontWeight = FontWeight.SemiBold)
            }

            Spacer(Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    if (isLogin) "No account? " else "Already have an account? ",
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                )
                Text(
                    if (isLogin) "Register" else "Log In",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable {
                        error = null
                        password = ""
                        if (!isLogin) name = ""
                        isLogin = !isLogin
                    }
                )
            }
        }
    }
}

@Composable
private fun AuthField(
    value: String,
    onValue: (String) -> Unit,
    placeholder: String,
    isPassword: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValue,
        placeholder = { Text(placeholder) },
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp)),
        shape = RoundedCornerShape(10.dp),
        singleLine = true,
        visualTransformation = if (isPassword) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None
    )
}
