package com.example.childeducation.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.childeducation.viewmodel.SettingsViewModel
import com.example.childeducation.viewmodel.SettingsViewModelFactory

@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val settingsViewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModelFactory(context)
    )
    val notificationsEnabled by settingsViewModel.notificationsEnabled.collectAsState()
    val darkModeEnabled by settingsViewModel.darkModeEnabled.collectAsState()

    val colorScheme = MaterialTheme.colorScheme

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
            .padding(24.dp)
    ) {
        Text(
            "Settings",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Enable Notifications", fontSize = 18.sp, color = colorScheme.onBackground)
            Switch(
                checked = notificationsEnabled,
                onCheckedChange = { settingsViewModel.setNotificationsEnabled(it) },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = colorScheme.primary,
                    uncheckedThumbColor = colorScheme.onSurfaceVariant
                )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Dark Mode", fontSize = 18.sp, color = colorScheme.onBackground)
            Switch(
                checked = darkModeEnabled,
                onCheckedChange = { settingsViewModel.setDarkModeEnabled(it) },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = colorScheme.primary,
                    uncheckedThumbColor = colorScheme.onSurfaceVariant
                )
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text(
            "App Version: 1.0.0",
            fontSize = 14.sp,
            color = colorScheme.onSurfaceVariant
        )
    }
}