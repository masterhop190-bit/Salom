package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.NovaMainViewModel
import com.example.ui.components.GlassCard
import com.example.ui.components.GlowingArcVisualizer
import com.example.ui.theme.AlertRed
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.GlowCyan
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
import com.example.ui.theme.WarningAmber

@Composable
fun HomeScreen(
    viewModel: NovaMainViewModel,
    modifier: Modifier = Modifier
) {
    val isListening by viewModel.isListening.collectAsState()
    val isSpeaking by viewModel.isSpeaking.collectAsState()
    val isProcessing by viewModel.isProcessing.collectAsState()
    val amplitude by viewModel.rmsAmplitude.collectAsState()
    val statusText by viewModel.currentStatusText.collectAsState()
    val liveMode by viewModel.liveModeActive.collectAsState()
    val recognizedSpeech by viewModel.recognizedSpeech.collectAsState()
    val isAccessibilityActive by viewModel.isAccessibilityActive.collectAsState()

    var manualInputText by remember { mutableStateOf("") }
    var showCallDialog by remember { mutableStateOf(false) }
    var callNumberInput by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SpaceDarkBg)
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // JARVIS Top HUD Status Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (isListening) CyberCyan else if (isAccessibilityActive) NeonEmerald else WarningAmber)
                    )
                    Text(
                        text = "JARVIS // NOVA PROTOCOL",
                        color = CyberCyan,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Text(
                    text = "STATUS: ${if (isListening) "LISTENING..." else if (isSpeaking) "VOCALIZING..." else "SYS_ONLINE (Say 'Hi Nova')"} ",
                    color = TextMuted,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            // Live Autonomous Mode Toggle
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (liveMode) NeonPurple.copy(alpha = 0.25f) else SpaceCardBg)
                    .border(
                        1.dp,
                        if (liveMode) NeonPurple else SpaceCardBorder,
                        RoundedCornerShape(20.dp)
                    )
                    .clickable { viewModel.toggleLiveMode() }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .testTag("live_mode_toggle"),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.GraphicEq,
                        contentDescription = "Live Mode",
                        tint = if (liveMode) NeonPurple else TextMuted,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = if (liveMode) "LIVE DUPLEX" else "HANDS-FREE",
                        color = if (liveMode) NeonPurple else TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Central Holographic Blue Cube & Reactor Core
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp),
            contentAlignment = Alignment.Center
        ) {
            GlowingArcVisualizer(
                modifier = Modifier.size(260.dp),
                amplitude = amplitude,
                isListening = isListening,
                isSpeaking = isSpeaking,
                isProcessing = isProcessing
            )

            // Voice Action Pill Over the Core
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                if (isListening) CyberCyan else ElectricBlue,
                                if (isListening) ElectricBlue else NeonPurple
                            )
                        )
                    )
                    .clickable { viewModel.toggleVoiceListening() }
                    .padding(horizontal = 20.dp, vertical = 10.dp)
                    .testTag("main_mic_button")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = if (isListening) Icons.Filled.Stop else Icons.Filled.Mic,
                        contentDescription = "Mic Trigger",
                        tint = SpaceDarkBg,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = if (isListening) "STOP LISTENING" else "START (SAY 'HI NOVA')",
                        color = SpaceDarkBg,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                }
            }
        }

        // Real-time Status / Subtitle Display
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = 12.dp,
            borderColor = if (isListening) CyberCyan else SpaceCardBorder
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = statusText,
                    color = when {
                        isListening -> CyberCyan
                        isSpeaking -> NeonPurple
                        isProcessing -> NeonEmerald
                        else -> TextSecondary
                    },
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                if (recognizedSpeech.isNotBlank()) {
                    Text(
                        text = "“$recognizedSpeech”",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }

        // Quick Tactical Grid: Call, Telegram, Screen Vision, Docx
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "JARVIS PROTOCOL ACTIONS",
                color = CyberCyan,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                fontFamily = FontFamily.Monospace
            )
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // 1. Direct Phone Call Action
            QuickActionCard(
                icon = Icons.Filled.PhoneInTalk,
                accentColor = NeonEmerald,
                title = "Telefon Qilish (Phone Call)",
                subtitle = "Istalgan raqamga yoki kontaktga to'g'ridan-to'g'ri qo'ng'iroq qilish",
                tag = "DIRECT CALL",
                testTag = "action_phone_call",
                onClick = { showCallDialog = true }
            )

            // 2. Telegram Send Action
            QuickActionCard(
                icon = Icons.AutoMirrored.Filled.Send,
                accentColor = CyberCyan,
                title = "Telegram: Onamga 'Bordim'",
                subtitle = "Telegram ochish, kontaktni topish va matn kiritib yuborish",
                tag = "GUI AUTO",
                testTag = "action_telegram",
                onClick = { viewModel.triggerTelegramQuickAction() }
            )

            // 3. Screen Vision
            QuickActionCard(
                icon = Icons.Filled.Visibility,
                accentColor = WarningAmber,
                title = "Ekran Tahlili (Screen Vision)",
                subtitle = "Ekranda nima borligini skanerlash va ovozli tushuntirib berish",
                tag = "VISION AI",
                testTag = "action_screen_vision",
                onClick = { viewModel.triggerScreenVision() }
            )

            // 4. DOCX Generator
            QuickActionCard(
                icon = Icons.Filled.Description,
                accentColor = ElectricBlue,
                title = "DOCX Hujjat: Avtomobil rasmi bilan",
                subtitle = "Internetdan rasm qidirib Word (.docx) fayl generatsiya qilish",
                tag = "FILE GEN",
                testTag = "action_docx_car",
                onClick = { viewModel.triggerDocxCarGeneration() }
            )
        }

        // Manual Command Line / Prompt
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = 8.dp
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = manualInputText,
                    onValueChange = { manualInputText = it },
                    placeholder = { Text("Buyruq yozing (Masalan: Call +99890...)", color = TextMuted, fontSize = 12.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberCyan,
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedContainerColor = SpaceCardBg,
                        unfocusedContainerColor = SpaceCardBg
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("home_text_input")
                )

                IconButton(
                    onClick = {
                        if (manualInputText.isNotBlank()) {
                            val text = manualInputText.trim()
                            manualInputText = ""
                            viewModel.handleUserPrompt(text, isVoice = false)
                        }
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(CyberCyan)
                        .testTag("home_send_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = SpaceDarkBg,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // JARVIS Cyber Diagnostics
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = 12.dp
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TelemetryStat("CORE", "JARVIS-3.5", CyberCyan)
                TelemetryStat("AUDIO_TTS", "ACTIVE", NeonPurple)
                TelemetryStat("ACCESSIBILITY", if (isAccessibilityActive) "ONLINE" else "OFFLINE", if (isAccessibilityActive) NeonEmerald else AlertRed)
                TelemetryStat("TOKENS", "${viewModel.preferencesManager.totalTokensUsed}", ElectricBlue)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }

    // Call Dialog
    if (showCallDialog) {
        AlertDialog(
            onDismissRequest = { showCallDialog = false },
            containerColor = SpaceCardBg,
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Filled.Phone, contentDescription = null, tint = NeonEmerald)
                    Text("Telefon Qilish (Direct Dial)", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Qo'ng'iroq qilmoqchi bo'lgan telefon raqamini kiriting:",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                    OutlinedTextField(
                        value = callNumberInput,
                        onValueChange = { callNumberInput = it },
                        placeholder = { Text("+998901234567", color = TextMuted) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonEmerald,
                            unfocusedBorderColor = SpaceCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedContainerColor = SpaceDarkBg,
                            unfocusedContainerColor = SpaceDarkBg
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().testTag("call_number_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (callNumberInput.isNotBlank()) {
                            val num = callNumberInput.trim()
                            showCallDialog = false
                            callNumberInput = ""
                            viewModel.makePhoneCall(num)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonEmerald, contentColor = SpaceDarkBg),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("confirm_call_button")
                ) {
                    Text("Qo'ng'iroq Qilish", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCallDialog = false }) {
                    Text("Bekor qilish", color = TextMuted)
                }
            }
        )
    }
}

@Composable
private fun QuickActionCard(
    icon: ImageVector,
    accentColor: Color,
    title: String,
    subtitle: String,
    tag: String,
    testTag: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .testTag(testTag),
        color = SpaceCardBg,
        border = androidx.compose.foundation.BorderStroke(1.dp, SpaceCardBorder)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(accentColor.copy(alpha = 0.15f))
                    .border(1.dp, accentColor.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = title,
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = subtitle,
                    color = TextSecondary,
                    fontSize = 11.sp,
                    lineHeight = 14.sp
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(SpaceSurface)
                    .padding(horizontal = 6.dp, vertical = 3.dp)
            ) {
                Text(
                    text = tag,
                    color = TextMuted,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun TelemetryStat(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
        Text(text = value, color = color, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
    }
}
