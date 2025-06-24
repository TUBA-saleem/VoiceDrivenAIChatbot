package com.example.childeducation.ui.screens

import android.widget.Toast
import com.example.childeducation.navigation.NavigationRoutes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.childeducation.R
import com.example.childeducation.UserPreferences
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@Composable
fun ChatbotHomeScreen(
    onMicClick: () -> Unit,
    onChatClick: () -> Unit,
    onDrawerItemClick: (String) -> Unit,
    onLogout: () -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val user = FirebaseAuth.getInstance().currentUser
    val userEmail = user?.email
    val userName = user?.displayName ?: "Little Explorer"

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(250.dp)
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        "Menu",
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    DrawerItem("Profile", Icons.Default.Person) {
                        scope.launch { drawerState.close() }
                        onDrawerItemClick(NavigationRoutes.Profile.route)
                    }
                    DrawerItem("History", Icons.Default.History) {
                        scope.launch { drawerState.close() }
                        onDrawerItemClick(NavigationRoutes.History.route)
                    }
                    DrawerItem("Language", Icons.Default.Language) {
                        scope.launch { drawerState.close() }
                        onDrawerItemClick(NavigationRoutes.Language.route)
                    }
                    DrawerItem("Info", Icons.Default.Info) {
                        scope.launch { drawerState.close() }
                        onDrawerItemClick(NavigationRoutes.Info.route)
                    }
                    DrawerItem("Settings", Icons.Default.Settings) {
                        scope.launch { drawerState.close() }
                        onDrawerItemClick(NavigationRoutes.Settings.route)
                    }
                }

                DrawerItem("Logout", Icons.Default.ExitToApp) {
                    UserPreferences.clearEmail(context)
                    UserPreferences.clearPassword(context)
                    FirebaseAuth.getInstance().signOut()
                    scope.launch { drawerState.close() }
                    onLogout()
                }
            }
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colorScheme.surfaceVariant,
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { scope.launch { drawerState.open() } }) {
                        Icon(Icons.Filled.Menu, contentDescription = "Menu", tint = MaterialTheme.colorScheme.onBackground)
                    }
                    Text(
                        text = if (userEmail != null) "Welcome, $userName!\n($userEmail)" else "Hey, Dear!",
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(16.dp))
                            .padding(8.dp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Image(
                    painter = painterResource(id = R.drawable.chatbot_avatar),
                    contentDescription = "Chatbot",
                    modifier = Modifier.size(200.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text("Welcome Back", fontSize = 20.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    "How Can I Help You Today?",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(40.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Button(
                        onClick = { onMicClick() },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = CircleShape,
                        modifier = Modifier.size(75.dp)
                    ) {
                        Icon(Icons.Filled.Mic, contentDescription = "Mic", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                    Spacer(modifier = Modifier.width(20.dp))
                    Button(
                        onClick = { onChatClick() },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = CircleShape,
                        modifier = Modifier.size(75.dp)
                    ) {
                        Icon(Icons.Filled.Keyboard, contentDescription = "Keyboard", tint = MaterialTheme.colorScheme.onSurface)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text("Tap To Talk", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun DrawerItem(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = title, tint = MaterialTheme.colorScheme.onPrimary)
        Spacer(modifier = Modifier.width(12.dp))
        Text(title, fontSize = 18.sp, color = MaterialTheme.colorScheme.onPrimary)
    }
}