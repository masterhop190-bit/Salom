package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.ConversationMessage
import com.example.ui.NovaMainViewModel
import com.example.ui.components.GlassCard
import com.example.ui.theme.AlertRed
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.SpaceCardBg
import com.example.ui.theme.SpaceCardBorder
import com.example.ui.theme.SpaceDarkBg
import com.example.ui.theme.SpaceSurface
import com.example.ui.theme.SpaceSurfaceVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ChatScreen(
    viewModel: NovaMainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val messages by viewModel.messages.collectAsState()
    val isListening by viewModel.isListening.collectAsState()
    val isSpeaking by viewModel.isSpeaking.collectAsState()
    val isProcessing by viewModel.isProcessing.collectAsState()
    val recognizedSpeech by viewModel.recognizedSpeech.collectAsState()

    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SpaceDarkBg)
    ) {
        // Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = SpaceCardBg,
            border = androidx.compose.foundation.BorderStroke(1.dp, SpaceCardBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(CyberCyan.copy(alpha = 0.2f))
                            .border(1.dp, CyberCyan, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.SmartToy,
                            contentDescription = null,
                            tint = CyberCyan,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "NOVA CONVERSATION",
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Model: ${viewModel.preferencesManager.selectedModel.substringAfterLast("-").uppercase()}",
                            color = TextMuted,
                            fontSize = 10.sp
                        )
                    }
                }

                IconButton(
                    onClick = { viewModel.clearChat() },
                    modifier = Modifier.testTag("clear_chat_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Clear History",
                        tint = TextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Message Thread
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (messages.isEmpty()) {
                item {
                    EmptyChatGreeting(onPromptClick = { prompt ->
                        viewModel.handleUserPrompt(prompt, isVoice = false)
                    })
                }
            }

            items(messages, key = { it.id }) { msg ->
                MessageBubble(
                    message = msg,
                    onSpeak = { text ->
                        viewModel.voiceEngine.speak(
                            text,
                            speed = viewModel.preferencesManager.ttsSpeed,
                            pitch = viewModel.preferencesManager.ttsPitch
                        )
                    }
                )
            }

            if (isProcessing) {
                item {
                    ProcessingIndicator()
                }
            }
        }

        // Bottom Input Area
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = SpaceCardBg,
            border = androidx.compose.foundation.BorderStroke(1.dp, SpaceCardBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (isListening && recognizedSpeech.isNotBlank()) {
                    Text(
                        text = "Eshitilmoqda: \"$recognizedSpeech\"",
                        color = CyberCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Voice Mic button
                    IconButton(
                        onClick = { viewModel.toggleVoiceListening() },
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isListening) CyberCyan else SpaceSurface)
                            .border(1.dp, if (isListening) CyberCyan else SpaceCardBorder, RoundedCornerShape(12.dp))
                            .testTag("chat_mic_button")
                    ) {
                        Icon(
                            imageVector = if (isListening) Icons.Filled.Mic else Icons.Filled.MicOff,
                            contentDescription = "Voice Input",
                            tint = if (isListening) SpaceDarkBg else TextPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = { Text("Savol yoki buyruq yozing...", color = TextMuted, fontSize = 13.sp) },
                        maxLines = 4,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberCyan,
                            unfocusedBorderColor = SpaceCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedContainerColor = SpaceDarkBg,
                            unfocusedContainerColor = SpaceDarkBg
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chat_text_input")
                    )

                    IconButton(
                        onClick = {
                            if (inputText.isNotBlank()) {
                                val text = inputText.trim()
                                inputText = ""
                                viewModel.handleUserPrompt(text, isVoice = false)
                            }
                        },
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(CyberCyan)
                            .testTag("chat_send_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Send,
                            contentDescription = "Send",
                            tint = SpaceDarkBg,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MessageBubble(
    message: ConversationMessage,
    onSpeak: (String) -> Unit
) {
    val isUser = message.role == "user"
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val timeString = timeFormat.format(Date(message.timestamp))

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(if (isUser) 0.82f else 0.92f),
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp
            ),
            color = if (isUser) SpaceSurfaceVariant else SpaceCardBg,
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (isUser) CyberCyan.copy(alpha = 0.4f) else SpaceCardBorder
            )
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Header (Role & Timestamp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isUser) "YOU" else "NOVA",
                        color = if (isUser) CyberCyan else NeonPurple,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (!isUser) {
                            Icon(
                                imageVector = Icons.Filled.VolumeUp,
                                contentDescription = "Speak",
                                tint = TextMuted,
                                modifier = Modifier
                                    .size(16.dp)
                                    .clickable { onSpeak(message.content) }
                            )
                        }
                        Text(
                            text = timeString,
                            color = TextMuted,
                            fontSize = 10.sp
                        )
                    }
                }

                // Message Text Content
                Text(
                    text = message.content,
                    color = TextPrimary,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )

                // If Action JSON is present, display mini action tag
                if (!message.actionJson.isNullOrBlank()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(SpaceDarkBg)
                            .border(1.dp, SpaceCardBorder, RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.TouchApp,
                                contentDescription = null,
                                tint = NeonEmerald,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Action Executed • Tokens: ${message.tokenCount}",
                                color = NeonEmerald,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyChatGreeting(onPromptClick: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(CyberCyan.copy(alpha = 0.15f))
                .border(1.dp, CyberCyan, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.AutoAwesome,
                contentDescription = null,
                tint = CyberCyan,
                modifier = Modifier.size(30.dp)
            )
        }

        Text(
            text = "Nova AI Jarvis Assistant",
            color = TextPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Quyidagi namuna buyruqlardan birini tanlang yoki ovoz orqali buyruq bering:",
            color = TextSecondary,
            fontSize = 12.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        val samplePrompts = listOf(
            "Call +998901234567",
            "Telegram’da Onamga 'Bordim' deb xabar yubor",
            "Ekranda nima bor? (Screen vision)",
            "Make me a .docx file containing 1 car image",
            "Python tilida Telegram bot skriptini yozib ber"
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            samplePrompts.forEach { prompt ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onPromptClick(prompt) },
                    color = SpaceCardBg,
                    border = androidx.compose.foundation.BorderStroke(1.dp, SpaceCardBorder)
                ) {
                    Text(
                        text = "• $prompt",
                        color = CyberCyan,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ProcessingIndicator() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.padding(8.dp)
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(16.dp),
            color = CyberCyan,
            strokeWidth = 2.dp
        )
        Text(
            text = "Nova o'ylamoqda...",
            color = TextSecondary,
            fontSize = 12.sp
        )
    }
}
