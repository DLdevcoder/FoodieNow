@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.foodienow.feature.notification

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun NotificationScreen(
    onBack: () -> Unit,
    viewModel: NotificationViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Thong bao") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Chua doc: ${uiState.unreadCount}")
                TextButton(onClick = viewModel::markAllAsRead) {
                    Text("Danh dau da doc")
                }
            }

            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(uiState.notifications, key = { it.id }) { item ->
                    NotificationCard(
                        item = item,
                        onClick = { viewModel.markAsRead(item.id) }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    TextButton(
                        onClick = onBack,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Quay lai")
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationCard(
    item: AppNotification,
    onClick: () -> Unit
) {
    val cardColor = if (item.isRead) {
        MaterialTheme.colorScheme.surfaceVariant
    } else {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(cardColor)
            .clickable(onClick = onClick)
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        val dotColor = if (item.isRead) Color.Gray else MaterialTheme.colorScheme.primary
        Spacer(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(dotColor)
                .align(Alignment.CenterVertically)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(item.title, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(2.dp))
            Text(item.message)
            Spacer(modifier = Modifier.height(4.dp))
            Text(item.timestampLabel, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        }
    }
}

