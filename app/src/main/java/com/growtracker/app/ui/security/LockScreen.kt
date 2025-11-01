package com.growtracker.app.ui.security

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material3.LocalTextStyle
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.growtracker.app.R
import com.growtracker.app.security.AppLockManager

@Composable
fun LockScreen(onUnlocked: () -> Unit) {
    val ctx = LocalContext.current
    // 4-stellige PIN via 4 Eingabefelder
    var d1 by remember { mutableStateOf("") }
    var d2 by remember { mutableStateOf("") }
    var d3 by remember { mutableStateOf("") }
    var d4 by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val fr1 = remember { FocusRequester() }
    val fr2 = remember { FocusRequester() }
    val fr3 = remember { FocusRequester() }
    val fr4 = remember { FocusRequester() }
    var error by remember { mutableStateOf<String?>(null) }
    val biometricEnabled = remember { AppLockManager.isBiometricEnabled(ctx) && AppLockManager.canUseBiometric(ctx) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 24.dp)
    ) {
        // Top: App Logo
        Image(
            painter = painterResource(id = R.drawable.ic_launcher_foreground),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 24.dp)
                .size(96.dp)
        )

        // Center: Title, PIN, Buttons
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("App gesperrt", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                DigitBox(
                    value = d1,
                    onValueChange = { v ->
                        val nv = v.filter { it.isDigit() }.take(1)
                        d1 = nv
                        if (nv.isNotEmpty()) fr2.requestFocus()
                    },
                    requester = fr1
                )
                DigitBox(
                    value = d2,
                    onValueChange = { v ->
                        val nv = v.filter { it.isDigit() }.take(1)
                        d2 = nv
                        if (nv.isNotEmpty()) fr3.requestFocus() else fr1.requestFocus()
                    },
                    requester = fr2
                )
                DigitBox(
                    value = d3,
                    onValueChange = { v ->
                        val nv = v.filter { it.isDigit() }.take(1)
                        d3 = nv
                        if (nv.isNotEmpty()) fr4.requestFocus() else fr2.requestFocus()
                    },
                    requester = fr3
                )
                DigitBox(
                    value = d4,
                    onValueChange = { v ->
                        val nv = v.filter { it.isDigit() }.take(1)
                        d4 = nv
                        if (nv.isEmpty()) fr3.requestFocus()
                    },
                    requester = fr4
                )
            }
            Spacer(Modifier.height(8.dp))
            Button(onClick = {
                val pin = (d1 + d2 + d3 + d4)
                if (pin.length == 4 && AppLockManager.verifyPin(ctx, pin)) {
                    onUnlocked()
                } else {
                    error = if (pin.length < 4) "PIN unvollständig" else "Falscher PIN"
                }
            }) { Text("Entsperren") }
            if (error != null) {
                Spacer(Modifier.height(6.dp))
                Text(error!!, color = MaterialTheme.colorScheme.error)
            }
            if (biometricEnabled) {
                Spacer(Modifier.height(12.dp))
                Button(onClick = { launchBiometric(ctx) { success -> if (success) { AppLockManager.unlock(); onUnlocked() } } }) {
                    Text("Mit Biometrie entsperren")
                }
            }
        }

        // Bottom: Social icons + caption
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(20.dp), verticalAlignment = Alignment.CenterVertically) {
                // Discord
                IconButton(onClick = {
                    val webInvite = "https://discord.gg/yE2Es4gsUb"
                    val discordIntent = ctx.packageManager.getLaunchIntentForPackage("com.discord")
                    try {
                        if (discordIntent != null) {
                            ctx.startActivity(discordIntent)
                        } else {
                            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(webInvite))
                            ctx.startActivity(browserIntent)
                        }
                    } catch (e: Exception) {
                        try {
                            val fallback = Intent(Intent.ACTION_VIEW, Uri.parse(webInvite))
                            ctx.startActivity(fallback)
                        } catch (ex: Exception) {
                            Toast.makeText(ctx, "Link kann nicht geöffnet werden", Toast.LENGTH_SHORT).show()
                        }
                    }
                }, modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color.Transparent)) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_discord),
                        contentDescription = "Discord",
                        tint = Color.Unspecified,
                        modifier = Modifier.size(36.dp)
                    )
                }

                // TikTok
                IconButton(onClick = {
                    val url = "https://www.tiktok.com/@growtracker"
                    try {
                        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        ctx.startActivity(browserIntent)
                    } catch (e: Exception) {
                        Toast.makeText(ctx, "Link kann nicht geöffnet werden", Toast.LENGTH_SHORT).show()
                    }
                }, modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color.Transparent)) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_tiktok),
                        contentDescription = "TikTok",
                        tint = Color.Unspecified,
                        modifier = Modifier.size(36.dp)
                    )
                }

                // Instagram
                IconButton(onClick = {
                    val url = "https://www.instagram.com/grow.tracker"
                    try {
                        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        ctx.startActivity(browserIntent)
                    } catch (e: Exception) {
                        Toast.makeText(ctx, "Link kann nicht geöffnet werden", Toast.LENGTH_SHORT).show()
                    }
                }, modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color.Transparent)) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_instagram),
                        contentDescription = "Instagram",
                        tint = Color.Unspecified,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Text("Tritt unseren Socials bei", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun DigitBox(
    value: String,
    onValueChange: (String) -> Unit,
    requester: FocusRequester
) {
    OutlinedTextField(
        value = value,
        onValueChange = { new ->
            onValueChange(new)
        },
        singleLine = true,
        textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
        modifier = Modifier
            .width(48.dp)
            .height(56.dp)
            .focusRequester(requester),
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        placeholder = { Text("•") }
    )
}

private fun launchBiometric(context: Context, onResult: (Boolean) -> Unit) {
    val activity = context as? FragmentActivity ?: run { onResult(false); return }
    val executor = ContextCompat.getMainExecutor(activity)
    val prompt = BiometricPrompt(activity, executor, object : BiometricPrompt.AuthenticationCallback() {
        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
            super.onAuthenticationSucceeded(result)
            onResult(true)
        }
        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
            super.onAuthenticationError(errorCode, errString)
            onResult(false)
        }
        override fun onAuthenticationFailed() {
            super.onAuthenticationFailed()
        }
    })
    val info = BiometricPrompt.PromptInfo.Builder()
        .setTitle("App entsperren")
        .setSubtitle("Biometrische Authentifizierung")
        .setNegativeButtonText("Abbrechen")
        .build()
    prompt.authenticate(info)
}
