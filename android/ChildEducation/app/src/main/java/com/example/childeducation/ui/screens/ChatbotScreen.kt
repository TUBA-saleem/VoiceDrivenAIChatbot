package com.example.childeducation.ui.screens

import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.childeducation.R
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.Locale
import java.util.concurrent.TimeUnit // Import TimeUnit

@Composable
fun ChatbotScreen(
    navController: NavController,
    initialVoiceInput: String? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    var userInput by remember { mutableStateOf("") }
    val messages = remember { mutableStateListOf<Pair<Boolean, String>>() }
    val feedback = remember { mutableStateMapOf<Int, Boolean?>() }


    // MODIFIED: Increase timeout for OkHttpClient
    val okHttpClient = remember {
        OkHttpClient.Builder()
            .readTimeout(2, TimeUnit.MINUTES) // Set read timeout to 2 minutes
            .connectTimeout(1, TimeUnit.MINUTES) // Optionally, set connect timeout
            .writeTimeout(1, TimeUnit.MINUTES) // Optionally, set write timeout
            .build()
    }
    val gson = remember { Gson() }
    val JSON_MEDIA_TYPE = remember { "application/json; charset=utf-8".toMediaType() }

    // TTS State
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    var ttsInitialized by remember { mutableStateOf(false) }

    // Initialize TTS
    LaunchedEffect(Unit) {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = tts?.setLanguage(Locale.US) // Or Locale.getDefault() or any other Locale
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e("TTS", "The Language specified is not supported! Result: $result")
                    Toast.makeText(context, "TTS language not supported.", Toast.LENGTH_SHORT).show()
                } else {
                    ttsInitialized = true
                    Log.i("TTS", "TextToSpeech Initialization successful. Language set to: ${Locale.US.displayLanguage}")
                }
            } else {
                Log.e("TTS", "TextToSpeech Initialization failed! Status: $status")
                Toast.makeText(context, "TTS initialization failed. Status: $status", Toast.LENGTH_SHORT).show()
            }
        }

    }

    val stopSpeaking: () -> Unit = {
        if (ttsInitialized && tts != null) {
            tts?.stop()
            Log.d("TTS", "Stopped speaking")
        }
    }
    // Dispose TTS
    DisposableEffect(Unit) {
        onDispose {
            Log.d("TTS", "Shutting down TTS engine.")
            tts?.stop()
            tts?.shutdown()
            ttsInitialized = false
        }
    }

    // Function to speak messages - MODIFIED for explicit Unit return type and enhanced logging
    val speakMessage: (String) -> Unit = { messageToSpeak: String ->
        if (ttsInitialized && tts != null) {
            if (messageToSpeak.isNotBlank() && messageToSpeak != "Typing..." && !messageToSpeak.startsWith("Error:") && !messageToSpeak.startsWith("\uD83D\uDCCE") && !messageToSpeak.startsWith("\u2705")) {
                val result = tts?.speak(messageToSpeak, TextToSpeech.QUEUE_FLUSH, null, null)
                if (result == TextToSpeech.ERROR) {
                    Log.e("TTS", "Error speaking message: '$messageToSpeak'")
                    Toast.makeText(context, "Error speaking message.", Toast.LENGTH_SHORT).show()
                } else {
                    Log.d("TTS", "Speaking message: '$messageToSpeak', Result: $result")
                }
            } else {
                Log.d("TTS", "Skipping TTS for status/error/empty message: '$messageToSpeak'")
            }
        } else {
            Toast.makeText(context, "Text-to-Speech is not ready yet. Please wait.", Toast.LENGTH_SHORT).show()
            Log.w("TTS", "Speak called but TTS not initialized or is null. ttsInitialized: $ttsInitialized, tts: $tts")
        }


    }



    // LaunchedEffect to process initial voice input
    LaunchedEffect(initialVoiceInput) {
        if (initialVoiceInput != null && initialVoiceInput.isNotBlank()) {
            sendQuestionToChatbot(
                question = initialVoiceInput,
                messagesState = messages,
                feedbackState = feedback,
                scrollState = scrollState,
                context = context,
                okHttpClient = okHttpClient,
                gson = gson,
                jsonMediaType = JSON_MEDIA_TYPE
            )
        }
    }

    val voiceLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val spokenText = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            spokenText?.let {
                scope.launch {
                    sendQuestionToChatbot(
                        question = it,
                        messagesState = messages,
                        feedbackState = feedback,
                        scrollState = scrollState,
                        context = context,
                        okHttpClient = okHttpClient,
                        gson = gson,
                        jsonMediaType = JSON_MEDIA_TYPE
                    )
                }
                Toast.makeText(context, "Voice recognized: $it", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Voice input canceled or failed.", Toast.LENGTH_SHORT).show()
        }
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            val fileName = getFileNameFromUri(context, it)
            val isImage = listOf("jpg", "jpeg", "png", "gif", "webp").any { ext -> fileName.endsWith(ext, ignoreCase = true) }

            if (isImage) {
                messages.add(true to "image://$it")
            } else {
                messages.add(true to "\uD83D\uDCCE You uploaded: $fileName")
            }
            messages.add(false to "\u2705 File received! (Handled by backend)")

            scope.launch {
                delay(100)
                scrollState.animateScrollTo(scrollState.maxValue)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            Text(
                "AI Buddy",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp)
                .verticalScroll(scrollState)
        ) {
            messages.forEachIndexed { index, (isUser, message) ->
                ChatBubble(
                    message = message,
                    isUser = isUser,
                    onLike = { feedback[index] = true },
                    onDislike = { feedback[index] = false },
                    isLiked = feedback[index],
                    onSpeak = { speakMessage(message) } ,
                    onStop = { stopSpeaking() }
                )
                Spacer(modifier = Modifier.height(10.dp))
            }
        }

        ChatInputField(
            userInput = userInput,
            onUserInputChange = { userInput = it },
            onSendMessage = {
                if (userInput.isNotBlank()) {
                    val currentQuestion = userInput
                    userInput = ""
                    scope.launch {
                        sendQuestionToChatbot(
                            question = currentQuestion,
                            messagesState = messages,
                            feedbackState = feedback,
                            scrollState = scrollState,
                            context = context,
                            okHttpClient = okHttpClient,
                            gson = gson,
                            jsonMediaType = JSON_MEDIA_TYPE
                        )
                    }
                } else {
                    Toast.makeText(context, "Type something first!", Toast.LENGTH_SHORT).show()
                }
            },
            onVoiceInput = {
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now...")
                }
                voiceLauncher.launch(intent)
                Toast.makeText(context, "\uD83C\uDF99 Voice input started!", Toast.LENGTH_SHORT).show()
            },
            onFileUploadClick = {
                filePickerLauncher.launch(arrayOf("*/*"))
            }
        )
    }
}

@Composable
fun ChatBubble(
    message: String,
    isUser: Boolean,
    onLike: () -> Unit,
    onDislike: () -> Unit,
    isLiked: Boolean?,
    onSpeak: (String) -> Unit,
    onStop: () -> Unit
) {
    val isImage = message.startsWith("image://")
    val imageUri = if (isImage) Uri.parse(message.removePrefix("image://")) else null

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        if (!isUser) {
            Image(
                painter = painterResource(id = R.drawable.chatbot_avatar),
                contentDescription = "Chatbot Avatar",
                modifier = Modifier
                    .size(40.dp)
                    .padding(end = 8.dp, top = 4.dp)
            )
        }

        Column {
            Box(
                modifier = Modifier
                    .background(
                        if (isUser) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(12.dp)
                    .widthIn(min = 100.dp, max = 300.dp)
            ) {
                if (isImage && imageUri != null) {
                    Image(
                        painter = rememberAsyncImagePainter(imageUri),
                        contentDescription = "Uploaded Image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                } else {
                    Text(
                        message,
                        color = if (isUser) MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.colorScheme.onSecondaryContainer,
                        fontSize = 16.sp
                    )
                }
            }

            if (!isUser && !isImage) {
                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onLike, modifier = Modifier.size(36.dp)) {
                        Icon(
                            Icons.Filled.ThumbUp,
                            contentDescription = "Like",
                            tint = if (isLiked == true) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outline
                        )
                    }
                    IconButton(onClick = onDislike, modifier = Modifier.size(36.dp)) {
                        Icon(
                            Icons.Filled.ThumbDown,
                            contentDescription = "Dislike",
                            tint = if (isLiked == false) MaterialTheme.colorScheme.error
                            else MaterialTheme.colorScheme.outline
                        )
                    }
                    // This is the Speak button
                    var isSpeaking by remember { mutableStateOf(false) }

                    IconButton(
                        onClick = {
                            if (isSpeaking) {
                                Log.d("ChatBubble", "Stopping speech")
                                onStop()            // You need to implement this to stop TTS
                                isSpeaking = false
                            } else {
                                Log.d("ChatBubble", "Starting speech for message: \"$message\"")
                                onSpeak(message)     // Starts TTS speaking
                                isSpeaking = true
                            }
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (isSpeaking) Icons.Filled.Stop else Icons.Filled.VolumeUp,
                            contentDescription = if (isSpeaking) "Stop speaking" else "Speak message",
                            tint = MaterialTheme.colorScheme.outline
                        )
                    }

                }
            }
        }
        if (isUser) {
            Spacer(modifier = Modifier.width(40.dp))
        }
    }
}

@Composable
fun ChatInputField(
    userInput: String,
    onUserInputChange: (String) -> Unit,
    onSendMessage: () -> Unit,
    onVoiceInput: () -> Unit,
    onFileUploadClick: () -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = userInput,
            onValueChange = onUserInputChange,
            label = { Text("Ask Something Else") },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(50.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                unfocusedIndicatorColor = MaterialTheme.colorScheme.outline,
                cursorColor = MaterialTheme.colorScheme.onSurface,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    keyboardController?.hide()
                    onSendMessage()
                }
            ),
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Done
            ),
            trailingIcon = {
                Row {
                    IconButton(onClick = onFileUploadClick) {
                        Icon(Icons.Default.AttachFile, contentDescription = "Attach File")
                    }
                    IconButton(onClick = onSendMessage) {
                        Icon(Icons.Filled.Send, contentDescription = "Send Message")
                    }
                }
            }
        )

        Spacer(modifier = Modifier.width(10.dp))

        Button(
            onClick = onVoiceInput,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = CircleShape,
            modifier = Modifier.size(80.dp)
        ) {
            Icon(Icons.Filled.Mic, contentDescription = "Mic", tint = MaterialTheme.colorScheme.onPrimary)
        }
    }

}

fun getFileNameFromUri(context: android.content.Context, uri: android.net.Uri): String {
    val cursor = context.contentResolver.query(uri, null, null, null, null)
    var name = "Unknown File"
    cursor?.use {
        val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (it.moveToFirst() && nameIndex != -1) {
            name = it.getString(nameIndex)
        }
    }
    return name
}

private suspend fun sendQuestionToChatbot(
    question: String,
    messagesState: MutableList<Pair<Boolean, String>>,
    feedbackState: SnapshotStateMap<Int, Boolean?>,
    scrollState: androidx.compose.foundation.ScrollState,
    context: android.content.Context,
    okHttpClient: OkHttpClient,
    gson: Gson,
    jsonMediaType: MediaType
) {
    messagesState.add(true to question)
    val typingMessageIndex = messagesState.size
    messagesState.add(false to "Typing...")
    feedbackState[typingMessageIndex] = null

    delay(100)
    scrollState.animateScrollTo(scrollState.maxValue)

    try {
        val jsonPayload = gson.toJson(mapOf("question" to question))
        val body = jsonPayload.toRequestBody(jsonMediaType)
        val request = Request.Builder()
            .url("http://127.0.0.1:5000/ask")
            .post(body)
            .build()

        val botResponse = withContext(Dispatchers.IO) {
            okHttpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val errorBody = response.body?.string() ?: "Unknown error"
                    throw IOException("HTTP error: ${response.code}, Body: $errorBody")
                }
                val responseBody = response.body?.string()
                val parsedResponse = gson.fromJson(responseBody, Map::class.java)
                parsedResponse["answer"] as? String ?: "Error: No 'answer' field in response"
            }
        }
        messagesState[typingMessageIndex] = false to botResponse
    } catch (e: Exception) {
        messagesState[typingMessageIndex] = false to "Error: Could not get a response. Please try again. (${e.localizedMessage})"
        Toast.makeText(context, "Connection Error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
    }
    delay(100)
    scrollState.animateScrollTo(scrollState.maxValue)
}
