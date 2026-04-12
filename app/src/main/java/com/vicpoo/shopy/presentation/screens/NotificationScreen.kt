//NotificationScreen.kt
package com.vicpoo.shopy.presentation.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.vicpoo.shopy.domain.model.Notification
import com.vicpoo.shopy.presentation.viewmodels.NotificationViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun NotificationScreen(
    navController: NavController,
    onBack: () -> Unit,
    onProductClick: (String) -> Unit,
    viewModel: NotificationViewModel = hiltViewModel()
) {
    val notifications by viewModel.notifications.collectAsState()
    val unreadCount by viewModel.unreadCount.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF050505), Color(0xFF0B0B0F))
                )
            )
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {

            // 🔥 HEADER PRO
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, null, tint = Color.White)
                    }

                    Text(
                        text = "Notificaciones",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }

                Row(
                    modifier = Modifier.padding(start = 56.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$unreadCount sin leer",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    if (notifications.isNotEmpty()) {
                        TextButton(
                            onClick = { viewModel.markAllAsRead() }
                        ) {
                            Text(
                                text = "Marcar todas",
                                color = Color(0xFFFF2E92)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 🔕 VACÍO
            if (notifications.isEmpty() && !isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.NotificationsNone,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(80.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "No hay notificaciones",
                            color = Color.Gray
                        )
                    }
                }
            } else {

                // 📋 LISTA
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = notifications,
                        key = { it.id }
                    ) { notification ->
                        NotificationItem(
                            notification = notification,
                            onMarkAsRead = { viewModel.markAsRead(notification.id) },
                            onDelete = { viewModel.deleteNotification(notification.id) },
                            onProductClick = {
                                notification.productId?.let { onProductClick(it) }
                            }
                        )
                    }
                }
            }
        }

        // ⏳ LOADING
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFFFF2E92))
            }
        }
    }
}

@Composable
fun NotificationItem(
    notification: Notification,
    onMarkAsRead: () -> Unit,
    onDelete: () -> Unit,
    onProductClick: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd MMM • HH:mm", Locale.getDefault())
    val formattedDate = dateFormat.format(Date(notification.timestamp))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.05f)
        )
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // 🟣 INDICADOR LATERAL
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(60.dp)
                    .background(
                        if (notification.read) Color.Transparent else Color(0xFFFF2E92),
                        RoundedCornerShape(10.dp)
                    )
            )

            Spacer(modifier = Modifier.width(10.dp))

            // 🔔 ICONO
            Box(
                modifier = Modifier
                    .size(45.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFFF2E92).copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (notification.productId != null)
                        Icons.Default.ShoppingBag
                    else
                        Icons.Default.Notifications,
                    contentDescription = null,
                    tint = Color(0xFFFF2E92),
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // 📝 TEXTO
            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(
                    text = notification.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = notification.message,
                    fontSize = 13.sp,
                    color = Color.Gray,
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = formattedDate,
                    fontSize = 11.sp,
                    color = Color.Gray.copy(alpha = 0.6f)
                )
            }

            // 🔘 BOTONES
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {

                if (!notification.read) {
                    IconButton(
                        onClick = onMarkAsRead,
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            Icons.Default.Done,
                            contentDescription = "Leída",
                            tint = Color(0xFFFF2E92),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                if (notification.productId != null) {
                    IconButton(
                        onClick = onProductClick,
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            Icons.Default.Visibility,
                            contentDescription = "Ver",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(30.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Eliminar",
                        tint = Color.Red,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}