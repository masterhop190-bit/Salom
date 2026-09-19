package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.preferences.SecurePreferencesManager
import com.example.ui.components.GlassCard
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.SpaceCardBg
import com.example.ui.theme.SpaceCardBorder
import com.example.ui.theme.SpaceDarkBg
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun OnboardingDialog(
    initialApiKey: String,
    initialLanguage: String,
    onComplete: (apiKey: String, language: String) -> Unit
) {
    var apiKey by remember { mutableStateOf(initialApiKey) }
    var passwordVisible by remember { mutableStateOf(false) }
    var selectedLanguage by remember { mutableStateOf(initialLanguage) }

    Dialog(
        onDismissRequest = { /* Force completion */ },
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false, usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .padding(vertical = 24.dp),
            shape = RoundedCornerShape(24.dp),
            color = SpaceDarkBg,
            border = androidx.compose.foundation.BorderStroke(1.5.dp, CyberCyan)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Badge
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(CyberCyan.copy(alpha = 0.15f))
                        .border(1.5.dp, CyberCyan, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.AutoAwesome,
                        contentDescription = "Nova",
                        tint = CyberCyan,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Text(
                    text = "NOVA AI ASSISTANT",
                    color = CyberCyan,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )

                Text(
                    text = "Jarvis-like Personal AI Companion • Autonomous GUI Automation & Vision",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                // API Key section
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = SpaceCardBorder,
                    contentPadding = 14.dp
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Filled.Key, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(18.dp))
                            Text(
                                text = "Google Gemini API Key",
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Text(
                            text = "Stored securely on device (Encrypted SharedPreferences). If left blank, default system key will be utilized.",
                            color = TextMuted,
                            fontSize = 11.sp
                        )

                        OutlinedTextField(
                            value = apiKey,
                            onValueChange = { apiKey = it },
                            placeholder = { Text("AIzaSy...", color = TextMuted, fontSize = 13.sp) },
                            singleLine = true,
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                        contentDescription = "Toggle key visibility",
                                        tint = TextMuted
                                    )
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyberCyan,
                                unfocusedBorderColor = SpaceCardBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedContainerColor = SpaceCardBg,
                                unfocusedContainerColor = SpaceCardBg
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("onboarding_api_key_input")
                        )
                    }
                }

                // Primary Language selection
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = SpaceCardBorder,
                    contentPadding = 14.dp
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Filled.Language, contentDescription = null, tint = NeonEmerald, modifier = Modifier.size(18.dp))
                            Text(
                                text = "Asosiy Muloqot Tili (Primary Language)",
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val languages = listOf(
                                SecurePreferencesManager.LANG_UZBEK to "O'zbekcha (UZ)",
                                SecurePreferencesManager.LANG_RUSSIAN to "Русский (RU)",
                                SecurePreferencesManager.LANG_ENGLISH to "English (EN)"
                            )

                            languages.forEach { (code, label) ->
                                val isSelected = selectedLanguage == code
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isSelected) NeonEmerald.copy(alpha = 0.2f) else SpaceCardBg)
                                        .border(
                                            1.dp,
                                            if (isSelected) NeonEmerald else SpaceCardBorder,
                                            RoundedCornerShape(10.dp)
                                        )
                                        .clickable { selectedLanguage = code }
                                        .padding(vertical = 10.dp, horizontal = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        color = if (isSelected) NeonEmerald else TextSecondary,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }

                // Capabilities overview
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    CapabilityItem(Icons.Filled.Mic, "Ok Nova Wake Word & Live hands-free voice mode")
                    CapabilityItem(Icons.Filled.TouchApp, "Autonomous Telegram / in-app GUI actions")
                    CapabilityItem(Icons.Filled.AutoAwesome, "Automated media search & .docx/.pdf generation")
                }

                // Action button
                Button(
                    onClick = {
                        onComplete(apiKey.trim(), selectedLanguage)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("initialize_nova_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyberCyan,
                        contentColor = SpaceDarkBg
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = "INITIALIZE NOVA",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun CapabilityItem(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(horizontal = 4.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.CheckCircle,
            contentDescription = null,
            tint = NeonEmerald,
            modifier = Modifier.size(14.dp)
        )
        Text(text = text, color = TextSecondary, fontSize = 11.sp)
    }
}
