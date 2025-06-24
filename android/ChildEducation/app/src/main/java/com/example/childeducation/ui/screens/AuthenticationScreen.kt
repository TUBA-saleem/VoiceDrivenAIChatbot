package com.example.childeducation.ui.screens

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.childeducation.R
import com.example.childeducation.navigation.NavigationRoutes
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

@Composable
fun AuthenticationScreen(navController: androidx.navigation.NavController) {
    val context = LocalContext.current
    val activity = context as Activity
    val auth = remember { FirebaseAuth.getInstance() }

    // ✅ Check if user already logged in
    LaunchedEffect(Unit) {
        auth.currentUser?.let {
            navController.navigate(NavigationRoutes.Home.route) {
                popUpTo(NavigationRoutes.Login.route) { inclusive = true }
            }
        }
    }

    // ─── Google Sign‑In setup ─────────────────────────────────────────────
    val webClientId =
        "847172548467-0f93dg4old1vor790lda1614bqti7ac8.apps.googleusercontent.com"
    val gso = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestEmail()
            .build()
    }
    val googleClient = remember { GoogleSignIn.getClient(activity, gso) }
    val googleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(Exception::class.java)!!
                val cred = GoogleAuthProvider.getCredential(account.idToken, null)
                auth.signInWithCredential(cred)
                    .addOnCompleteListener(activity) { t ->
                        if (t.isSuccessful) {
                            navController.navigate(NavigationRoutes.Home.route) {
                                popUpTo(NavigationRoutes.Login.route) { inclusive = true }
                            }
                        } else {
                            Toast.makeText(activity, "Google login failed.", Toast.LENGTH_SHORT).show()
                        }
                    }
            } catch (_: Exception) {
                Toast.makeText(activity, "Google sign‑in error.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ─── Email/Password state & logic ───────────────────────────────────
    var email     by remember { mutableStateOf("") }
    var password  by remember { mutableStateOf("") }
    var isSignUp  by remember { mutableStateOf(false) }
    var errorMsg  by remember { mutableStateOf("") }

    fun doEmailAuth() {
        errorMsg = ""
        if (email.isBlank() || password.isBlank()) {
            errorMsg = "Oops! Fill in all fields."
            return
        }
        val call = if (isSignUp)
            auth.createUserWithEmailAndPassword(email.trim(), password.trim())
        else
            auth.signInWithEmailAndPassword(email.trim(), password.trim())

        call.addOnCompleteListener(activity) { t ->
            if (t.isSuccessful) {
                if (isSignUp) {
                    Toast.makeText(context, "🎉 Sign‑up successful!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "🎊 Login successful!", Toast.LENGTH_SHORT).show()
                }
                navController.navigate(NavigationRoutes.Home.route) {
                    popUpTo(NavigationRoutes.Login.route) { inclusive = true }
                }
            } else {
                errorMsg = t.exception?.localizedMessage ?: "Authentication failed."
            }
        }
    }

    // ─── UI ───────────────────────────────────────────────────────────────
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFFFFE4B5),
                        Color(0xFFFFB6C1)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Welcome, little explorer! 🚀",
                fontSize = 23.sp,
                color = Color(0xFF6A1B9A),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Text(
                text = if (isSignUp) "Sign Up" else "Log In",
                fontSize = 28.sp,
                color = Color.Magenta
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Enter Your Email 📩") },
                singleLine = true,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor   = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedTextColor        = Color.Black,
                    unfocusedTextColor      = Color.Black,
                    focusedLabelColor       = Color.Black,
                    unfocusedLabelColor     = Color.Gray,
                    cursorColor             = Color.Black
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Enter Your Password 🔑") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor   = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedTextColor        = Color.Black,
                    unfocusedTextColor      = Color.Black,
                    focusedLabelColor       = Color.Black,
                    unfocusedLabelColor     = Color.Gray,
                    cursorColor             = Color.Black
                )
            )

            if (errorMsg.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(errorMsg, color = Color.Red)
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = { doEmailAuth() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSignUp) Color(0xFFFFC107) else Color(0xFFFF5722)
                ),
                shape = RoundedCornerShape(50.dp),
                modifier = Modifier.fillMaxWidth(0.7f)
            ) {
                Text(if (isSignUp) "Sign Up" else "Log In")
            }

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(onClick = { isSignUp = !isSignUp }) {
                Text(
                    if (isSignUp)
                        "Already have an account? Log In"
                    else
                        "Don't have an account? Sign Up"
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            Divider()
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = { googleLauncher.launch(googleClient.signInIntent) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.White
                )
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_google_logo),
                    contentDescription = "Google logo",
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text("Continue with Google", color = Color.Black)
            }
        }
    }
}
