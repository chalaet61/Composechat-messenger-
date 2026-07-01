package com.example.presentation.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.presentation.auth.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    authViewModel: AuthViewModel,
    onNavigateToProfile: () -> Unit,
    onNavigateBack: () -> Unit,
    onLogoutSuccess: () -> Unit
) {
    val currentUser = remember { authViewModel.getCurrentUser() }
    val displayName = remember { currentUser?.email?.substringBefore("@") ?: "User" }
    var showNotificationDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var selectedTheme by remember { mutableStateOf("System default") }

    val gradientBrush = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
            MaterialTheme.colorScheme.background
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradientBrush)
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // User Profile Summary Row (Clickable to go to Profile Details)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .clickable { onNavigateToProfile() }
                        .padding(horizontal = 20.dp, vertical = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = displayName.take(2).uppercase(),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = displayName,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = currentUser?.email ?: "",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.QrCode,
                        contentDescription = "QR Code Placeholder",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Divider(color = MaterialTheme.colorScheme.surfaceVariant, thickness = 1.dp)

                Spacer(modifier = Modifier.height(12.dp))

                // Settings Categories List
                SettingsItem(
                    icon = Icons.Default.Key,
                    title = "Account",
                    subtitle = "Security notifications, change email/password",
                    onClick = { /* Account details */ }
                )

                SettingsItem(
                    icon = Icons.Default.ColorLens,
                    title = "Theme & Chats",
                    subtitle = "Wallpaper, dark mode toggle, chat history settings",
                    onClick = { showThemeDialog = true }
                )

                SettingsItem(
                    icon = Icons.Default.Notifications,
                    title = "Notifications",
                    subtitle = "FCM push, message alert sounds & system triggers",
                    onClick = { showNotificationDialog = true }
                )

                SettingsItem(
                    icon = Icons.Default.DataUsage,
                    title = "Storage and Data",
                    subtitle = "Network usage, auto-download media files",
                    onClick = { /* Storage */ }
                )

                SettingsItem(
                    icon = Icons.Default.Help,
                    title = "Help & About",
                    subtitle = "Help center, privacy policy, diagnostics",
                    onClick = { /* Help */ }
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Logout Button Action
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .clickable {
                            authViewModel.logout()
                            onLogoutSuccess()
                        }
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.ExitToApp,
                        contentDescription = "Logout",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Log Out",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Version display branding
                Text(
                    text = "ComposeChat v1.4.0-simulation\nGoogle AI Studio Build",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }

            // Notification Info dialog
            if (showNotificationDialog) {
                AlertDialog(
                    onDismissRequest = { showNotificationDialog = false },
                    title = { Text("FCM Notifications Config") },
                    text = {
                        Text(
                            text = "To enable Background Push Alerts, configure Firebase Cloud Messaging (FCM):\n\n" +
                                    "1. Add the firebase-messaging SDK.\n" +
                                    "2. Create a FirebaseMessagingService in your manifest.\n" +
                                    "3. Inject your server token to dispatch remote payloads securely.\n\n" +
                                    "ComposeChat automatically falls back to in-app real-time synchronization so you never miss a message while online!"
                        )
                    },
                    confirmButton = {
                        TextButton(onClick = { showNotificationDialog = false }) {
                            Text("Got it")
                        }
                    }
                )
            }

            // Theme dialog
            if (showThemeDialog) {
                AlertDialog(
                    onDismissRequest = { showThemeDialog = false },
                    title = { Text("Choose Theme") },
                    text = {
                        Column {
                            ThemeRadioButtonOption("System default", selectedTheme) { selectedTheme = it }
                            ThemeRadioButtonOption("Light mode", selectedTheme) { selectedTheme = it }
                            ThemeRadioButtonOption("Dark mode", selectedTheme) { selectedTheme = it }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showThemeDialog = false }) {
                            Text("OK")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun SettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(20.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
    Divider(color = MaterialTheme.colorScheme.surfaceVariant, thickness = 0.5.dp)
}

@Composable
fun ThemeRadioButtonOption(
    option: String,
    currentSelection: String,
    onOptionSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOptionSelected(option) }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = option == currentSelection,
            onClick = { onOptionSelected(option) }
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = option, fontSize = 15.sp)
    }
}
