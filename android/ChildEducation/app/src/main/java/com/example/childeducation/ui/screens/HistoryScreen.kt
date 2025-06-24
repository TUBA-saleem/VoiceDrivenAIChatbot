package com.example.childeducation.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.childeducation.viewmodel.HistoryEntry
import com.example.childeducation.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(viewModel: MainViewModel = viewModel()) {
    val historyList by viewModel.history.collectAsState()
    val selectedLanguage by viewModel.selectedLanguage.collectAsState()

    // Dynamic titles/texts
    val titleText = if (selectedLanguage == "اردو") "تاریخ" else "History"
    val clearAllText = if (selectedLanguage == "اردو") "تمام حذف کریں" else "Clear All"
    val noHistoryText = if (selectedLanguage == "اردو") "کوئی تاریخ موجود نہیں ہے۔" else "No history found."

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(titleText) },
                actions = {
                    TextButton(onClick = { viewModel.clearHistory() }) {
                        Text(clearAllText, color = MaterialTheme.colorScheme.primary)
                    }
                }
            )
        }
    ) { padding ->
        if (historyList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(noHistoryText)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp)
            ) {
                items(historyList) { entry ->
                    HistoryItem(entry)
                }
            }
        }
    }
}

@Composable
fun HistoryItem(entry: HistoryEntry) {
    val sdf = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()) }
    val dateText = sdf.format(Date(entry.timestamp))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = entry.entry)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = dateText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
