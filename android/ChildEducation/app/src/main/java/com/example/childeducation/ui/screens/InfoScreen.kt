package com.example.childeducation.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun InfoScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background) // Background will adjust dynamically based on light or dark mode
            .padding(24.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Top
    ) {
        // Project Advisor heading with purple color (primary color from the theme)
        Text(
            "Project Advisor: Dr. Asif Sohail",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary // Primary color for heading
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Developed by heading with purple color (primary color from the theme)
        Text(
            "Developed by Team CS Fall 2021",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary // Primary color for heading
        )

        Spacer(modifier = Modifier.height(16.dp))

        // List of team members with standard text color
        Text("• Muhammad Umar Qureshi (BCSF21M003)", fontSize = 16.sp, color = MaterialTheme.colorScheme.onBackground)
        Text("• Haider Humayoun (BCSF21M011)", fontSize = 16.sp, color = MaterialTheme.colorScheme.onBackground)
        Text("• Husna Sarwar (BCSF21M018)", fontSize = 16.sp, color = MaterialTheme.colorScheme.onBackground)
        Text("• Tuba Saleem (BCSF20M015)", fontSize = 16.sp, color = MaterialTheme.colorScheme.onBackground)
    }
}