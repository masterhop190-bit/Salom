package com.example.ui.screens

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.preferences.SecurePreferencesManager
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
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarningAmber

@Composable
fun SettingsScreen(
    viewModel: NovaMainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val prefs = viewModel.preferencesManager

    var apiKeyText by remember { mutableStateOf(prefs.getApiKey()) }
    var keyVisible by remember { mutableStateOf(false) }
    var selectedModel by remember { mutableStateOf(prefs.selectedModel) }
    var selectedLang by remember { mutableStateOf(prefs.primaryLanguage) }
    var wakeWordEnabled by remember { mutableStateOf(prefs.wakeWordEnabled) }
    var overlayEnabled by remember { mutableStateOf(prefs.overlayBubbleEnabled) }
    var offlinePrivacy by remember { mutableStateOf(prefs.offlinePrivacyMode) }
    var ttsSpeed by remember { mutableFloatStateOf(prefs.ttsSpeed) }
    var ttsPitch by remember { mutableFloatStateOf(prefs.ttsPitch) }

    val isAccessibilityActive by viewModel.isAccessibilityActive.collectAsState()

    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { /* handled */ }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SpaceDarkBg)
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Title
        Text(
            text = "SYSTEM SETTINGS & CONFIG",
            color = CyberCyan,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )

        // API Key Section
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = 14.dp
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Filled.Key, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(18.dp))
                    Text(text = "Google Gemini API Key", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedTextField(
                    value = apiKeyText,
                    onValueChange = { apiKeyText = it },
                    singleLine = true,
                    visualTransformation = if (keyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { keyVisible = !keyVisible }) {
                            Icon(
                                imageVector = if (keyVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                contentDescription = "Toggle visibility",
                                tint = TextMuted
                            )
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberCyan,
                        unfocusedBorderColor = SpaceCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedContainerColor = SpaceDarkBg,
                        unfocusedContainerColor = SpaceDarkBg
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().testTag("settings_api_key_input")
                )

                Button(
                    onClick = {
                        prefs.setApiKey(apiKeyText.trim())
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = SpaceDarkBg),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.align(Alignment.End).testTag("save_api_key_button")
                ) {
                    Text("Kalitni Saqlash", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Model Selector
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = 14.dp
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Filled.Memory, contentDescription = null, tint = NeonPurple, modifier = Modifier.size(18.dp))
                    Text(text = "Gemini Model Tanlovi", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }

                val models = listOf(
                    SecurePreferencesManager.MODEL_FLASH to "Gemini 3.5 Flash (Tez & Tejamkor)",
                    SecurePreferencesManager.MODEL_PRO to "Gemini 3.1 Pro (Keng Qamrovli & Mantiqiy)",
                    SecurePreferencesManager.MODEL_FLASH_LITE to "Gemini 3.1 Flash Lite (Ultra Tez)"
                )

                models.forEach { (modelKey, label) ->
                    val isSelected = selectedModel == modelKey
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) NeonPurple.copy(alpha = 0.2f) else SpaceDarkBg)
                            .border(1.dp, if (isSelected) NeonPurple else SpaceCardBorder, RoundedCornerShape(8.dp))
                            .clickable {
                                selectedModel = modelKey
                                prefs.selectedModel = modelKey
                            }
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = label,
                            color = if (isSelected) NeonPurple else TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                        if (isSelected) {
                            Icon(Icons.Filled.Check, contentDescription = null, tint = NeonPurple, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }

        // Language Selector
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = 14.dp
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Filled.Language, contentDescription = null, tint = NeonEmerald, modifier = Modifier.size(18.dp))
                    Text(text = "Muloqot Tili (Language)", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val languages = listOf(
                        SecurePreferencesManager.LANG_UZBEK to "O'zbekcha",
                        SecurePreferencesManager.LANG_RUSSIAN to "Русский",
                        SecurePreferencesManager.LANG_ENGLISH to "English"
                    )

                    languages.forEach { (code, label) ->
                        val isSelected = selectedLang == code
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) NeonEmerald.copy(alpha = 0.2f) else SpaceDarkBg)
                                .border(1.dp, if (isSelected) NeonEmerald else SpaceCardBorder, RoundedCornerShape(8.dp))
                                .clickable {
                                    selectedLang = code
                                    prefs.primaryLanguage = code
                                    viewModel.voiceEngine.setLanguage(code)
                                }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                color = if (isSelected) NeonEmerald else TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }

        // Voice Engine Customization (Speed & Pitch)
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = 14.dp
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Filled.VolumeUp, contentDescription = null, tint = ElectricBlue, modifier = Modifier.size(18.dp))
                    Text(text = "Ovoz va Talaffuz (TTS Settings)", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }

                Text(text = "Nutq tezligi: ${String.format("%.1f", ttsSpeed)}x", color = TextSecondary, fontSize = 11.sp)
                Slider(
                    value = ttsSpeed,
                    onValueChange = {
                        ttsSpeed = it
                        prefs.ttsSpeed = it
                    },
                    valueRange = 0.5f..2.0f,
                    colors = SliderDefaults.colors(thumbColor = ElectricBlue, activeTrackColor = ElectricBlue)
                )

                Text(text = "Nutq balandligi (Pitch): ${String.format("%.1f", ttsPitch)}x", color = TextSecondary, fontSize = 11.sp)
                Slider(
                    value = ttsPitch,
                    onValueChange = {
                        ttsPitch = it
                        prefs.ttsPitch = it
                    },
                    valueRange = 0.5f..2.0f,
                    colors = SliderDefaults.colors(thumbColor = ElectricBlue, activeTrackColor = ElectricBlue)
                )

                Button(
                    onClick = {
                        viewModel.voiceEngine.speak(
                            text = if (selectedLang == "uz") "Salom! Men Nova AI yordamchisiman." else if (selectedLang == "ru") "Здравствуйте! Я голосовой ассистент Нова." else "Hello! I am Nova AI Assistant.",
                            speed = ttsSpeed,
                            pitch = ttsPitch
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SpaceSurface, contentColor = TextPrimary),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Ovozni Sinash", fontSize = 11.sp)
                }
            }
        }

        // Permissions Wizard & Background Services
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = 14.dp
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Filled.Security, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(18.dp))
                    Text(text = "Tizim Ruxsatlari & Xizmatlar", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }

                // 1. Accessibility Service
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Accessibility GUI Xizmati", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Text(text = if (isAccessibilityActive) "Faol (Yoqilgan)" else "O'chiq (Ruxsat zarur)", color = if (isAccessibilityActive) NeonEmerald else WarningAmber, fontSize = 10.sp)
                    }
                    Button(
                        onClick = {
                            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
                            context.startActivity(intent)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SpaceSurface, contentColor = TextPrimary),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text("Sozlamalar", fontSize = 10.sp)
                    }
                }

                // 2. Wake Word Background Service
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "\"Ok Nova\" Wake Word", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Text(text = "Fondagi doimiy ovozli chaqiruv", color = TextMuted, fontSize = 10.sp)
                    }
                    Switch(
                        checked = wakeWordEnabled,
                        onCheckedChange = {
                            wakeWordEnabled = it
                            viewModel.toggleWakeWordService(context, it)
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = CyberCyan, checkedTrackColor = CyberCyan.copy(alpha = 0.5f))
                    )
                }

                // 3. Floating Overlay Bubble
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Suzuvchi Nova Bubble (Overlay)", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Text(text = "Ekran ustidagi tezkor nishon", color = TextMuted, fontSize = 10.sp)
                    }
                    Switch(
                        checked = overlayEnabled,
                        onCheckedChange = {
                            overlayEnabled = it
                            viewModel.toggleOverlayService(context, it)
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = CyberCyan, checkedTrackColor = CyberCyan.copy(alpha = 0.5f))
                    )
                }
            }
        }

        // Token Economy Stats
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = 14.dp
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Filled.Speed, contentDescription = null, tint = NeonEmerald, modifier = Modifier.size(18.dp))
                    Text(text = "Token Tejamkorligi & Statistika", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }

                Text(
                    text = "Jami sarflangan tokenlar: ${prefs.totalTokensUsed}",
                    color = CyberCyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = "• On-demand Screen Vision: Faqat so'ralganda JPEG formatida siqib yuboriladi.\n• Local Wake-Word & UI node parsing: API sarflamasdan 100% qurilmaning o'zida ishlaydi.",
                    color = TextMuted,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}
