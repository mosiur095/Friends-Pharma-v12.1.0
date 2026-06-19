package com.friendspharma.app.features.presentation.notification

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.friendspharma.app.features.NavigationActions

@Composable
fun NotificationScreen(
    navAction: NavigationActions,
    viewModel: NotificationViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    NotificationContent(
        state = state,
        onBack = { navAction.pop() },
        onEvent = viewModel::onEvent,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NotificationContent(
    state: NotificationState,
    onBack: () -> Unit,
    onEvent: (NotificationEvent) -> Unit,
) {
    var showClearAllDialog by remember { mutableStateOf(false) }

    if (showClearAllDialog) {
        AlertDialog(
            onDismissRequest = { showClearAllDialog = false },
            shape = RoundedCornerShape(16.dp),
            title = { Text("Clear all notifications?", fontWeight = FontWeight.SemiBold) },
            text = { Text("This will permanently delete all notifications.", color = MaterialTheme.colorScheme.onSurfaceVariant) },
            confirmButton = {
                TextButton(onClick = {
                    onEvent(NotificationEvent.ClearAll)
                    showClearAllDialog = false
                }) { Text("Clear all", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.SemiBold) }
            },
            dismissButton = {
                TextButton(onClick = { showClearAllDialog = false }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                title = {
                    Column {
                        Text("Notifications", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        if (state.unreadCount > 0) {
                            Text("${state.unreadCount} unread", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                },
                actions = {
                    if (state.unreadCount > 0) {
                        TextButton(onClick = { onEvent(NotificationEvent.MarkAllAsRead) }) {
                            Text("Mark all read", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).background(Color(0xFFF5F6FA))
        ) {
            when {
                state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                state.error != null -> ErrorState(state.error) { onEvent(NotificationEvent.Refresh) }
                state.isEmpty -> EmptyState()
                else -> {
                    // Action bar
                    Row(
                        Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface)
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Notifications, contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("${state.notifications.size} notifications  •  swipe to delete",
                                fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        TextButton(
                            onClick = { showClearAllDialog = true },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("Clear all", fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Medium)
                        }
                    }
                    HorizontalDivider(thickness = 0.5.dp)
                    LazyColumn(
                        Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(items = state.notifications, key = { it.id }) { item ->
                            SwipeToDeleteNotification(
                                item = item,
                                onDelete = { onEvent(NotificationEvent.DeleteNotification(item)) },
                                onClick = { onEvent(NotificationEvent.NotificationClicked(item)) }
                            )
                        }
                        item { Spacer(Modifier.height(8.dp)) }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeToDeleteNotification(
    item: NotificationUi,
    onDelete: () -> Unit,
    onClick: () -> Unit,
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.StartToEnd) { onDelete(); true } else false
        }
    )
    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = true,
        enableDismissFromEndToStart = false,
        backgroundContent = {
            val color by animateColorAsState(
                targetValue = when (dismissState.targetValue) {
                    SwipeToDismissBoxValue.StartToEnd -> Color(0xFFD32F2F)
                    else -> Color(0xFFFFEBEE)
                }, label = "swipe_bg"
            )
            val scale by animateFloatAsState(
                targetValue = if (dismissState.targetValue == SwipeToDismissBoxValue.StartToEnd) 1.2f else 0.8f,
                label = "icon_scale"
            )
            Box(
                Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp))
                    .background(color).padding(start = 20.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete",
                        tint = if (dismissState.targetValue == SwipeToDismissBoxValue.StartToEnd)
                            Color.White else Color(0xFFD32F2F),
                        modifier = Modifier.scale(scale).size(22.dp))
                    if (dismissState.targetValue == SwipeToDismissBoxValue.StartToEnd) {
                        Spacer(Modifier.height(2.dp))
                        Text("Delete", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        },
        content = { NotificationCard(item = item, onClick = onClick) }
    )
}

@Composable
private fun NotificationCard(item: NotificationUi, onClick: () -> Unit) {
    val (icon, iconBg, iconTint) = notificationIconConfig(item.type)

    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (item.isRead) MaterialTheme.colorScheme.surface else Color(0xFFF0F4FF)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (item.isRead) 1.dp else 3.dp)
    ) {
        Column(Modifier.fillMaxWidth()) {
            Row(
                Modifier.fillMaxWidth().padding(14.dp),
                verticalAlignment = Alignment.Top
            ) {
                // Icon circle
                Box(
                    modifier = Modifier.size(42.dp).clip(CircleShape).background(iconBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = null,
                        tint = iconTint, modifier = Modifier.size(20.dp))
                }

                Spacer(Modifier.width(12.dp))

                Column(Modifier.weight(1f)) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = item.title,
                            fontWeight = if (item.isRead) FontWeight.Medium else FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(Modifier.width(8.dp))
                        if (!item.isRead) {
                            Box(Modifier.size(8.dp).clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary))
                        }
                    }

                    Spacer(Modifier.height(4.dp))

                    Text(
                        text = item.body,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = if (item.imageUrl != null) 1 else 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 18.sp
                    )

                    Spacer(Modifier.height(6.dp))

                    // Type chip
                    Box(
                        modifier = Modifier.clip(RoundedCornerShape(4.dp))
                            .background(iconBg.copy(alpha = 0.6f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(text = notificationTypeLabel(item.type), fontSize = 10.sp,
                            color = iconTint, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            // Promotional banner image — shown when imageUrl is present
            if (!item.imageUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = item.imageUrl,
                    contentDescription = "Promotional banner",
                    contentScale = ContentScale.FillWidth,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 12.dp, end = 12.dp, bottom = 12.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .height(140.dp)
                )
            }

            // Unread accent line at bottom
            if (!item.isRead) {
                Box(
                    Modifier.fillMaxWidth().height(2.dp).background(
                        Brush.horizontalGradient(
                            listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.primary.copy(alpha = 0f)
                            )
                        )
                    )
                )
            }
        }
    }
}

@Composable
private fun EmptyState() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier.size(80.dp).clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Notifications, contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                    modifier = Modifier.size(40.dp))
            }
            Spacer(Modifier.height(16.dp))
            Text("No notifications yet", fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(6.dp))
            Text("You're all caught up!", fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(message, style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error)
            Spacer(Modifier.height(12.dp))
            Button(onClick = onRetry) { Text("Retry") }
        }
    }
}

@Composable
private fun notificationIconConfig(type: NotificationType): Triple<ImageVector, Color, Color> {
    return when (type) {
        NotificationType.OFFER         -> Triple(Icons.Default.LocalOffer,   Color(0xFFFFF3E0), Color(0xFFE65100))
        NotificationType.ORDER_STATUS  -> Triple(Icons.Default.Campaign,     Color(0xFFE8F5E9), Color(0xFF2E7D32))
        NotificationType.BACK_IN_STOCK -> Triple(Icons.Default.Notifications,Color(0xFFE8F5E9), Color(0xFF388E3C))
        NotificationType.PRICE_CHANGE  -> Triple(Icons.Default.LocalOffer,   Color(0xFFF3E5F5), Color(0xFF7B1FA2))
        NotificationType.GENERAL       -> Triple(Icons.Default.Info,         Color(0xFFE3F2FD), Color(0xFF1565C0))
    }
}

private fun notificationTypeLabel(type: NotificationType): String = when (type) {
    NotificationType.OFFER         -> "OFFER"
    NotificationType.ORDER_STATUS  -> "ORDER"
    NotificationType.BACK_IN_STOCK -> "STOCK"
    NotificationType.PRICE_CHANGE  -> "PRICE"
    NotificationType.GENERAL       -> "GENERAL"
}