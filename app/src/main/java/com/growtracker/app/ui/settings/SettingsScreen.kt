@file:Suppress("DEPRECATION")

package com.growtracker.app.ui.settings

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
// use fully-qualified material icon references to avoid receiver/import mismatches
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import com.growtracker.app.ui.theme.ThemeManager
import com.growtracker.app.ui.language.LanguageManager
import com.growtracker.app.ui.language.Language
import com.growtracker.app.ui.language.Strings
import com.growtracker.app.ui.language.getString

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    themeManager: ThemeManager,
    languageManager: LanguageManager
) {
    var notificationsEnabled by remember { mutableStateOf(true) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showSystemInfoDialog by remember { mutableStateOf(false) }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        // Samsung-style clean background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
        )
        
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                // Compact header - Samsung style
                Column(
                    horizontalAlignment = Alignment.Start,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                ) {
                    Text(
                        text = getString(Strings.settings_title, languageManager),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            
            // Design & Display - Samsung Style
            item {
                SamsungSettingsItem(
                    title = getString(Strings.settings_dark_mode, languageManager),
                    subtitle = getString(Strings.settings_dark_mode_subtitle, languageManager),
                    icon = Icons.Filled.DarkMode,
                    isSwitch = true,
                    switchChecked = themeManager.isDarkMode,
                    onSwitchChange = { themeManager.setDarkMode(it) }
                )
            }

            // Notifications - Samsung Style
            item {
                SamsungSettingsItem(
                    title = getString(Strings.settings_push_notifications, languageManager),
                    subtitle = getString(Strings.settings_push_notifications_subtitle, languageManager),
                    icon = Icons.Filled.Notifications,
                    isSwitch = true,
                    switchChecked = notificationsEnabled,
                    onSwitchChange = { notificationsEnabled = it }
                )
            }

            // Language - Samsung Style
            item {
                SamsungSettingsItem(
                    title = getString(Strings.settings_language, languageManager),
                    subtitle = "${languageManager.currentLanguage.flag} ${languageManager.currentLanguage.displayName}",
                    icon = Icons.Filled.Language,
                    onClick = { showLanguageDialog = true }
                )
            }

            // Divider
            item {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )
            }

                    // Growbox / Plant related settings removed from navigator settings per request

            // System Information - Samsung Style
            item {
                SamsungSettingsItem(
                    title = getString(Strings.settings_system_info, languageManager),
                    subtitle = "Version 1.0.0 • Android ${Build.VERSION.RELEASE}",
                    icon = Icons.Filled.Info,
                    onClick = { showSystemInfoDialog = true }
                )
            }

            // Social / Support row (centered Discord icon)
            item {
                val ctx = LocalContext.current
                Box(modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(onClick = {
                            val webInvite = "https://discord.gg/yE2Es4gsUb"
                            // First try to open the Discord app via package name (deep link). If not installed, fall back to browser.
                            val discordIntent = ctx.packageManager.getLaunchIntentForPackage("com.discord")
                            try {
                                if (discordIntent != null) {
                                    // Attempt to open app; if it accepts VIEW intents you could also use a deep link
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
                                    Toast.makeText(ctx, "Kann Link nicht öffnen", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }, modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Color.Transparent)) {
                            Icon(
                                painter = androidx.compose.ui.res.painterResource(id = com.growtracker.app.R.drawable.ic_discord),
                                contentDescription = "Discord",
                                tint = Color.Unspecified,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Join us on Discord", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }

    // Language Selection Dialog
    if (showLanguageDialog) {
        LanguageSelectionDialog(
            currentLanguage = languageManager.currentLanguage,
            onLanguageSelected = { language ->
                languageManager.setLanguage(language)
                showLanguageDialog = false
            },
            onDismiss = { showLanguageDialog = false },
            languageManager = languageManager
        )
    }

    // System Info Dialog
    if (showSystemInfoDialog) {
        SystemInfoDialog(
            onDismiss = { showSystemInfoDialog = false },
            languageManager = languageManager
        )
    }
}

@Composable
fun FertilizerSelectionDialog(onDismiss: () -> Unit, onSelect: (String) -> Unit) {
    var selected by remember { mutableStateOf("") }
    val options = listOf("FoxFarm", "General Hydroponics", "Advanced Nutrients", "Technaflora")
    Dialog(onDismissRequest = onDismiss) {
        Card(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("Dünger-Hersteller wählen", style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(8.dp))
                options.forEach { opt ->
                    Row(modifier = Modifier.fillMaxWidth().clickable { selected = opt }, verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = selected == opt, onClick = { selected = opt })
                        Spacer(Modifier.width(8.dp))
                        Text(opt)
                    }
                }
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    TextButton(onClick = onDismiss) { Text("Abbrechen") }
                    TextButton(onClick = { if (selected.isNotBlank()) onSelect(selected) }) { Text("OK") }
                }
            }
        }
    }
}

@Composable
fun LightTypeDialog(onDismiss: () -> Unit, onSelect: (String) -> Unit) {
    var selected by remember { mutableStateOf("") }
    val options = listOf("LED", "HPS", "CFL", "Fluorescent")
    Dialog(onDismissRequest = onDismiss) {
        Card(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("Lichtart wählen", style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(8.dp))
                options.forEach { opt ->
                    Row(modifier = Modifier.fillMaxWidth().clickable { selected = opt }, verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = selected == opt, onClick = { selected = opt })
                        Spacer(Modifier.width(8.dp))
                        Text(opt)
                    }
                }
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    TextButton(onClick = onDismiss) { Text("Abbrechen") }
                    TextButton(onClick = { if (selected.isNotBlank()) onSelect(selected) }) { Text("OK") }
                }
            }
        }
    }
}

@Composable
fun WattInputDialog(onDismiss: () -> Unit, onSave: (Int) -> Unit) {
    var value by remember { mutableStateOf(0) }
    Dialog(onDismissRequest = onDismiss) {
        Card(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("Watt einstellen", style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = if (value == 0) "" else value.toString(), onValueChange = { v -> value = v.filter { it.isDigit() }.toIntOrNull() ?: 0 }, label = { Text("Watt") })
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    TextButton(onClick = onDismiss) { Text("Abbrechen") }
                    TextButton(onClick = { if (value > 0) onSave(value) }) { Text("OK") }
                }
            }
        }
    }
}

@Composable
fun SamsungSettingsItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isSwitch: Boolean = false,
    switchChecked: Boolean = false,
    onSwitchChange: ((Boolean) -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) Modifier.clickable { onClick() }
                else if (isSwitch && onSwitchChange != null) Modifier.clickable { onSwitchChange(!switchChecked) }
                else Modifier
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(20.dp)
                        .padding(10.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Text content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Switch or Arrow
            if (isSwitch && onSwitchChange != null) {
                Switch(
                    checked = switchChecked,
                    onCheckedChange = onSwitchChange,
                    modifier = Modifier.padding(start = 8.dp)
                )
            } else if (onClick != null) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Öffnen",
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
            }
        }
    }
}

@Composable
fun LanguageSelectionDialog(
    currentLanguage: Language,
    onLanguageSelected: (Language) -> Unit,
    onDismiss: () -> Unit,
    languageManager: LanguageManager
) {
    val languages = languageManager.getAllLanguages()

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 20.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Language,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .size(24.dp)
                                .padding(12.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = getString(Strings.language_select_title, languageManager),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                languages.forEach { language ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onLanguageSelected(language) }
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (language == currentLanguage) 
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f)
                            else 
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = language == currentLanguage,
                                onClick = { onLanguageSelected(language) }
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = language.flag,
                                style = MaterialTheme.typography.headlineSmall,
                                modifier = Modifier.padding(end = 12.dp)
                            )
                            Text(
                                text = language.displayName,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (language == currentLanguage) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    FilledTonalButton(onClick = onDismiss) {
                        Text(getString(Strings.dialog_cancel, languageManager))
                    }
                }
            }
        }
    }
}

@Composable
fun SystemInfoDialog(
    onDismiss: () -> Unit,
    languageManager: LanguageManager
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 20.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        modifier = Modifier.size(48.dp)
                    ) {
                Icon(
                    imageVector = Icons.Filled.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                    .size(24.dp)
                    .padding(12.dp)
                )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = getString(Strings.system_info_title, languageManager),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                SystemInfoItem(
                    getString(Strings.system_info_app_version, languageManager), 
                    "1.0.0"
                )
                SystemInfoItem(
                    getString(Strings.system_info_android_version, languageManager), 
                    "${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})"
                )
                SystemInfoItem(
                    getString(Strings.system_info_device, languageManager), 
                    "${Build.MANUFACTURER} ${Build.MODEL}"
                )
                SystemInfoItem(
                    getString(Strings.system_info_build, languageManager), 
                    Build.DISPLAY
                )
                SystemInfoItem(
                    getString(Strings.system_info_kernel, languageManager), 
                    System.getProperty("os.version") ?: getString(Strings.system_info_unknown, languageManager)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    FilledTonalButton(onClick = onDismiss) {
                        Text(getString(Strings.dialog_close, languageManager))
                    }
                }
            }
        }
    }
}

@Composable
fun SystemInfoItem(label: String, value: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$label:",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

