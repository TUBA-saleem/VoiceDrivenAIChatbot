package com.example.childeducation.ui.screens

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateValue
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.childeducation.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedWriter
import java.io.OutputStreamWriter
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

@Composable
fun ChatbotListeningScreen(onCancel: () -> Unit) {
    val context = LocalContext.current

    var recognizedText by remember { mutableStateOf("") }
    var isListening by remember { mutableStateOf(false) }

    // Manage TextToSpeech instance as nullable state
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    var ttsInitialized by remember { mutableStateOf(false) }

    // Initialize TTS asynchronously
    LaunchedEffect(Unit) {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = tts?.setLanguage(Locale.US)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Toast.makeText(context, "TTS language not supported.", Toast.LENGTH_SHORT).show()
                } else {
                    ttsInitialized = true
                }
            } else {
                Toast.makeText(context, "TTS initialization failed. Status: $status", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Dispose TTS when composable leaves composition
    DisposableEffect(Unit) {
        onDispose {
            tts?.stop()
            tts?.shutdown()
            ttsInitialized = false
        }
    }

    // Function to safely speak message
    val speakMessage: (String) -> Unit = { messageToSpeak ->
        if (ttsInitialized && tts != null) {
            if (messageToSpeak.isNotBlank()) {
                val result = tts?.speak(messageToSpeak, TextToSpeech.QUEUE_FLUSH, null, null)
                if (result == TextToSpeech.ERROR) {
                    Toast.makeText(context, "Error speaking message.", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(context, "Text-to-Speech is not ready yet.", Toast.LENGTH_SHORT).show()
        }
    }

    // Speech recognition launcher
    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val resultList = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            recognizedText = resultList?.get(0).orEmpty()
            if (recognizedText.isNotEmpty()) {
                Toast.makeText(context, "Heard: $recognizedText", Toast.LENGTH_SHORT).show()
                CoroutineScope(Dispatchers.IO).launch {
                    val response = fetchBotResponse(recognizedText)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Bot: $response", Toast.LENGTH_SHORT).show()
                        speakMessage(response)
                    }
                }
            }
        }
        isListening = false
    }



    // Animations
    val micPulse = rememberInfiniteTransition().animateFloat(
        initialValue = 1f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(900),
            repeatMode = RepeatMode.Reverse
        )
    )

    val bounceAvatar = rememberInfiniteTransition().animateFloat(
        initialValue = 0f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        )
    )

    val alphaAnim by animateFloatAsState(
        targetValue = if (recognizedText.isNotEmpty()) 1f else 0.5f,
        animationSpec = tween(700)
    )

    val dotsAnim = rememberInfiniteTransition().animateValue(
        initialValue = 1,
        targetValue = 3,
        typeConverter = Int.VectorConverter,
        animationSpec = infiniteRepeatable(
            animation = tween(500),
            repeatMode = RepeatMode.Restart
        )
    )

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
            .padding(16.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxSize()
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            Image(
                painter = painterResource(id = R.drawable.chatbot_avatar),
                contentDescription = "Chatbot",
                modifier = Modifier
                    .size(180.dp)
                    .offset(y = bounceAvatar.value.dp)
            )

            Text(
                text = if (isListening) "Listening" + ".".repeat(dotsAnim.value) else "Press to speak",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = recognizedText.ifEmpty { "Waiting for your voice..." },
                fontSize = 18.sp,
                modifier = Modifier.alpha(alphaAnim)
            )

            Spacer(modifier = Modifier.height(40.dp))

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size((120 * micPulse.value).dp)
                    .background(MaterialTheme.colorScheme.primary, shape = CircleShape)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onPress = {
                                // If TTS is speaking, stop it first
                                if (ttsInitialized && tts?.isSpeaking == true) {
                                    tts?.stop()
                                }

                                isListening = true
                                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                                    putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now...")
                                }
                                speechLauncher.launch(intent)
                                tryAwaitRelease()
                            }
                        )
                    }

            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Mic",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(48.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    tts?.stop()
                    onCancel()
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                shape = CircleShape,
                modifier = Modifier.size(80.dp)
            ) {
                Icon(Icons.Filled.Close, contentDescription = "Stop Listening", tint = MaterialTheme.colorScheme.onError)
            }
        }
    }
}

suspend fun fetchBotResponse(userInput: String): String {
    return try {
        val url = URL("http://127.0.0.1:5000/ask")
        val connection = url.openConnection() as HttpURLConnection

        connection.requestMethod = "POST"
        connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
        connection.setRequestProperty("Accept", "application/json")
        connection.doOutput = true
        connection.doInput = true
        connection.connectTimeout = 5000
        connection.readTimeout = 5000

        val jsonRequest = JSONObject().apply {
            put("question", userInput)
        }

        Log.d("FETCH_REQUEST", "Sending: ${jsonRequest.toString()}")

        BufferedWriter(OutputStreamWriter(connection.outputStream, "UTF-8")).use { writer ->
            writer.write(jsonRequest.toString())
        }

        val responseCode = connection.responseCode
        if (responseCode == HttpURLConnection.HTTP_OK) {
            val responseText = connection.inputStream.bufferedReader().use { it.readText() }
            val jsonResponse = JSONObject(responseText)
            jsonResponse.optString("answer", "Sorry, no reply from server.")
        } else {
            "Server error: $responseCode"
        }
    } catch (e: Exception) {
        e.printStackTrace()
        "Failed to connect to server."
    }
}

