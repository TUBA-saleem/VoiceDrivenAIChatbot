package com.example.childeducation.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.childeducation.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageScreen(viewModel: MainViewModel = viewModel()) {
    val selectedLanguage by viewModel.selectedLanguage.collectAsState()
    val languages = listOf("English", "اردو")
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Dynamic title
    val titleText = if (selectedLanguage == "اردو") "زبان منتخب کریں" else "Select Language"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(titleText) }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
        ) {
            languages.forEach { language ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    RadioButton(
                        selected = language == selectedLanguage,
                        onClick = {
                            viewModel.setLanguage(language) {
                                scope.launch {
                                    val message = if (language == "اردو") {
                                        "✅ زبان اردو منتخب ہو گئی ہے"
                                    } else {
                                        "✅ Language set to English"
                                    }
                                    snackbarHostState.showSnackbar(message)
                                }
                            }
                        }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = language)
                }
            }
        }
    }
}
