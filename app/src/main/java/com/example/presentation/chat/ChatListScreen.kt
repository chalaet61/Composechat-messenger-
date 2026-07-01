package com.example.presentation.chat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Chat
import com.example.data.model.User
import com.example.presentation.auth.AuthViewModel
import java.text.SimpleDateFormat
import java.util.*

import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(
    chatViewModel: ChatViewModel,
    authViewModel: AuthViewModel,
    onNavigateToChatDetail: (userId: String, userName: String) -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onLogout: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) } // 0 = Chats, 1 = Contacts/People

    val chats by chatViewModel.chats.collectAsState()
    val users by chatViewModel.users.collectAsState()
    val isLoadingChats by chatViewModel.isLoadingChats.collectAsState()
    val isLoadingUsers by chatViewModel.isLoadingUsers.collectAsState()
    val error by chatViewModel.error.collectAsState()

    // Trigger loads on entry
    LaunchedEffect(Unit) {
        chatViewModel.loadChats()
        chatViewModel.loadUsers()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "ComposeChat",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                actions = {
                    IconButton(
                        onClick = onNavigateToSearch,
                        modifier = Modifier.testTag("search_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(
                        onClick = onNavigateToSettings,
                        modifier = Modifier.testTag("settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        floatingActionButton = {
            if (selectedTab == 0) {
                FloatingActionButton(
                    onClick = { selectedTab = 1 },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.testTag("new_chat_fab")
                ) {
                    Icon(
                        imageVector = Icons.Default.ChatBubble,
                        contentDescription = "New Chat"
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Tabs Row
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Message, contentDescription = "Chats Tab", modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Chats", fontWeight = FontWeight.Bold)
                        }
                    },
                    modifier = Modifier.testTag("chats_tab")
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.People, contentDescription = "Contacts Tab", modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Contacts", fontWeight = FontWeight.Bold)
                        }
                    },
                    modifier = Modifier.testTag("contacts_tab")
                )
            }

            // Error display
            AnimatedVisibility(visible = error != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = error ?: "",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            // Tabs Content
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                if (selectedTab == 0) {
                    // Chats List
                    if (isLoadingChats) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    } else if (chats.isEmpty()) {
                        EmptyStateView(
                            title = "No active conversations",
                            subtitle = "Tap on the contacts tab or the floating action button below to start messaging!"
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 80.dp)
                        ) {
                            items(chats) { chat ->
                                ChatItem(
                                    chat = chat,
                                    onClick = {
                                        onNavigateToChatDetail(chat.otherUser.uid, chat.otherUser.displayName)
                                    }
                                )
                            }
                        }
                    }
                } else {
                    // Contacts List
                    if (isLoadingUsers) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    } else if (users.isEmpty()) {
                        EmptyStateView(
                            title = "No contacts found",
                            subtitle = "Register other accounts to see contacts listed here!"
                        )
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(users) { user ->
                                ContactItem(
                                    user = user,
                                    onClick = {
                                        onNavigateToChatDetail(user.uid, user.displayName)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChatItem(
    chat: Chat,
    onClick: () -> Unit
) {
    val formatter = remember { SimpleDateFormat("h:mm a", Locale.getDefault()) }
    val formattedTime = if (chat.lastMessageTimestamp > 0) {
        formatter.format(Date(chat.lastMessageTimestamp))
    } else ""

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("chat_item_${chat.otherUser.uid}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar with initials
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = chat.otherUser.displayName.take(2).uppercase(),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontSize = 18.sp
            )
            // Online badge
            if (chat.otherUser.isOnline) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF4CAF50)) // Elegant emerald green
                        .align(Alignment.BottomEnd)
                        .background(MaterialTheme.colorScheme.background)
                        .padding(2.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = chat.otherUser.displayName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = formattedTime,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = chat.lastMessage,
                    fontSize = 14.sp,
                    color = if (chat.unreadCount > 0) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    fontWeight = if (chat.unreadCount > 0) FontWeight.Bold else FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                if (chat.unreadCount > 0) {
                    Box(
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = chat.unreadCount.toString(),
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
    HorizontalDivider(
        modifier = Modifier.padding(start = 84.dp, end = 16.dp),
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    )
}

@Composable
fun ContactItem(
    user: User,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("contact_item_${user.uid}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.secondaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = user.displayName.take(2).uppercase(),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                fontSize = 16.sp
            )
            if (user.isOnline) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF4CAF50))
                        .align(Alignment.BottomEnd)
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column {
            Text(
                text = user.displayName,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = if (user.isOnline) "Online" else "Offline",
                fontSize = 13.sp,
                color = if (user.isOnline) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
            )
        }
    }
    HorizontalDivider(
        modifier = Modifier.padding(start = 80.dp, end = 16.dp),
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
    )
}

@Composable
fun EmptyStateView(
    title: String,
    subtitle: String
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.ChatBubble,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
            modifier = Modifier.size(72.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = subtitle,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            textAlign = TextAlign.Center
        )
    }
}
