package com.example.childeducation.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth

@Composable
fun ProfileScreen() {
    val user = FirebaseAuth.getInstance().currentUser
    val email = remember { mutableStateOf(user?.email ?: "Not available") }
    val name = remember { mutableStateOf(user?.displayName ?: "User") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background) // Dynamic background color
            .padding(24.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Top
    ) {
        // Profile heading with vibrant color
        Text(
            "Profile",
            fontSize = 28.sp,
            color = MaterialTheme.colorScheme.primary, // Primary color for heading
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Name section with bright text colors for light and dark mode
        Text(
            "Name: ${name.value}",
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onBackground // Adaptive color for text
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Email section with complementary color for text
        Text(
            "Email: ${email.value}",
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onBackground // Adaptive color for text
        )
    }
}
