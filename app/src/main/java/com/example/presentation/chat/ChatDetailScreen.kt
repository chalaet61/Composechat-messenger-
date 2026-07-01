package com.example.presentation.chat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Reply
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Message
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(
    userId: String,
    userName: String,
    viewModel: ChatViewModel,
    onNavigateBack: () -> Unit
) {
    var textMessage by remember { mutableStateOf("") }
    val messages by viewModel.messages.collectAsState()
    val isLoading by viewModel.isLoadingMessages.collectAsState()
    val error by viewModel.error.collectAsState()
    val myUserId = remember { viewModel.getMyUserId() }
    
    var isTyping by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    // Interactive message actions states (Reply, Edit, Delete, Copy)
    var selectedMessageForActions by remember { mutableStateOf<Message?>(null) }
    var replyingToMessage by remember { mutableStateOf<Message?>(null) }
    var editingMessage by remember { mutableStateOf<Message?>(null) }
    var deletedMessageIds by remember { mutableStateOf(setOf<String>()) }
    var editedMessageTexts by remember { mutableStateOf(mapOf<String, String>()) }

    val finalMessages = remember(messages, deletedMessageIds, editedMessageTexts) {
        messages.filter { !deletedMessageIds.contains(it.messageId) }
            .map { msg ->
                if (editedMessageTexts.containsKey(msg.messageId)) {
                    msg.copy(text = editedMessageTexts[msg.messageId]!!)
                } else {
                    msg
                }
            }
    }

    val listState = rememberLazyListState()

    // Load messages and mark them as read immediately on entry
    LaunchedEffect(userId) {
        viewModel.loadMessages(userId)
    }

    // Scroll to the latest message automatically when finalMessages list changes
    LaunchedEffect(finalMessages) {
        if (finalMessages.isNotEmpty()) {
            listState.animateScrollToItem(finalMessages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = userName.take(2).uppercase(),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = userName,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (isTyping) "typing..." else "Online",
                                fontSize = 11.sp,
                                fontWeight = if (isTyping) FontWeight.Bold else FontWeight.Normal,
                                color = if (isTyping) MaterialTheme.colorScheme.primary else Color(0xFF4CAF50)
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Messages Area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (isLoading && finalMessages.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (finalMessages.isEmpty()) {
                    EmptyConversationView(userName = userName)
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(finalMessages) { message ->
                            val isMe = message.senderId == myUserId
                            MessageBubble(
                                message = message,
                                isMe = isMe,
                                onClick = { selectedMessageForActions = message }
                            )
                        }
                    }
                }

                // Temporary error popups
                if (error != null) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        modifier = Modifier
                            .padding(16.dp)
                            .align(Alignment.BottomCenter)
                    ) {
                        Text(
                            text = error ?: "",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(12.dp),
                            fontSize = 13.sp
                        )
                    }
                }
            }

            // Input Bar Area (Respects Soft Keyboard)
            Surface(
                tonalElevation = 4.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .imePadding()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Reply Banner Layout
                    if (replyingToMessage != null) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Reply,
                                contentDescription = "Replying",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Replying to Message",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = replyingToMessage!!.text,
                                    fontSize = 13.sp,
                                    maxLines = 1,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(
                                onClick = { replyingToMessage = null },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Cancel reply",
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }

                    // Edit Banner Layout
                    if (editingMessage != null) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Editing",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Editing Message",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = editingMessage!!.text,
                                    fontSize = 13.sp,
                                    maxLines = 1,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(
                                onClick = {
                                    editingMessage = null
                                    textMessage = ""
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Cancel edit",
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = textMessage,
                            onValueChange = { textMessage = it },
                            placeholder = { Text(if (editingMessage != null) "Edit message..." else "Message...") },
                            maxLines = 4,
                            shape = RoundedCornerShape(24.dp),
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Sentences,
                                imeAction = ImeAction.Send
                            ),
                            keyboardActions = KeyboardActions(
                                onSend = {
                                    if (textMessage.isNotBlank()) {
                                        if (editingMessage != null) {
                                            editedMessageTexts = editedMessageTexts + (editingMessage!!.messageId to textMessage)
                                            editingMessage = null
                                            textMessage = ""
                                        } else {
                                            val prefix = if (replyingToMessage != null) "⤷ [Reply]: \"${replyingToMessage!!.text.take(20)}...\"\n" else ""
                                            viewModel.sendMessage(userId, prefix + textMessage)
                                            textMessage = ""
                                            replyingToMessage = null

                                            coroutineScope.launch {
                                                delay(500)
                                                isTyping = true
                                                delay(1200)
                                                isTyping = false
                                            }
                                        }
                                    }
                                }
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("message_input")
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                                .clickable {
                                    if (textMessage.isNotBlank()) {
                                        if (editingMessage != null) {
                                            editedMessageTexts = editedMessageTexts + (editingMessage!!.messageId to textMessage)
                                            editingMessage = null
                                            textMessage = ""
                                        } else {
                                            val prefix = if (replyingToMessage != null) "⤷ [Reply]: \"${replyingToMessage!!.text.take(20)}...\"\n" else ""
                                            viewModel.sendMessage(userId, prefix + textMessage)
                                            textMessage = ""
                                            replyingToMessage = null

                                            coroutineScope.launch {
                                                delay(500)
                                                isTyping = true
                                                delay(1200)
                                                isTyping = false
                                            }
                                        }
                                    }
                                }
                                .testTag("send_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // Message Actions Options Alert Dialog
    if (selectedMessageForActions != null) {
        val selectedMessage = selectedMessageForActions!!
        val isSelectedMe = selectedMessage.senderId == myUserId
        val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
        val context = androidx.compose.ui.platform.LocalContext.current

        AlertDialog(
            onDismissRequest = { selectedMessageForActions = null },
            title = { Text("Message Options", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "\"${selectedMessage.text}\"",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Copy
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(selectedMessage.text))
                                android.widget.Toast.makeText(context, "Copied to clipboard", android.widget.Toast.LENGTH_SHORT).show()
                                selectedMessageForActions = null
                            }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(16.dp))
                        Text("Copy Text", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    }

                    // Reply
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                replyingToMessage = selectedMessage
                                editingMessage = null // Cancel editing
                                selectedMessageForActions = null
                            }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Reply, contentDescription = "Reply", tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(16.dp))
                        Text("Reply to Message", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    }

                    // Edit (Only for own messages)
                    if (isSelectedMe) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    editingMessage = selectedMessage
                                    replyingToMessage = null // Cancel replying
                                    textMessage = selectedMessage.text
                                    selectedMessageForActions = null
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(16.dp))
                            Text("Edit Message", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    // Delete
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                deletedMessageIds = deletedMessageIds + selectedMessage.messageId
                                selectedMessageForActions = null
                            }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.width(16.dp))
                        Text("Delete Message", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.error)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedMessageForActions = null }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
fun MessageBubble(
    message: Message,
    isMe: Boolean,
    onClick: () -> Unit
) {
    val formatter = remember { SimpleDateFormat("h:mm a", Locale.getDefault()) }
    val formattedTime = formatter.format(Date(message.timestamp))

    // Dynamic styled chat shapes (Asymmetric WhatsApp bubble corners)
    val bubbleShape = if (isMe) {
        RoundedCornerShape(16.dp, 16.dp, 0.dp, 16.dp)
    } else {
        RoundedCornerShape(16.dp, 16.dp, 16.dp, 0.dp)
    }

    // Modern WhatsApp Colors
    val bubbleColor = if (isMe) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)
    }

    val alignment = if (isMe) Alignment.End else Alignment.Start

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(if (isMe) "sent_message_bubble" else "received_message_bubble"),
        horizontalAlignment = alignment
    ) {
        Card(
            shape = bubbleShape,
            colors = CardDefaults.cardColors(containerColor = bubbleColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clickable { onClick() }
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                Text(
                    text = message.text,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.align(Alignment.End),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formattedTime,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )

                    if (isMe) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = if (message.isRead) Icons.Default.DoneAll else Icons.Default.Check,
                            contentDescription = if (message.isRead) "Read" else "Sent",
                            tint = if (message.isRead) Color(0xFF2196F3) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyConversationView(userName: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = userName.take(2).uppercase(),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                fontSize = 20.sp
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Conversation started with $userName",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Messages are secured with real-time Firestore sync. Tap below to send a friendly hello!",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp, start = 16.dp, end = 16.dp)
        )
    }
}
