package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.provider.Settings
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.data.local.entity.AutomationShortcut
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
fun AutomationScreen(
    viewModel: NovaMainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isAccessibilityActive by viewModel.isAccessibilityActive.collectAsState()
    val isOverlayActive by viewModel.isOverlayActive.collectAsState()
    val shortcuts by viewModel.shortcuts.collectAsState()
    val screenHierarchy by viewModel.screenHierarchyPreview.collectAsState()

    var showHierarchy by remember { mutableStateOf(false) }

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
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "AUTONOMOUS GUI CENTER",
                    color = CyberCyan,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Accessibility-driven in-app actions and automation",
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }
        }

        // Accessibility Service Status Banner
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            borderColor = if (isAccessibilityActive) NeonEmerald.copy(alpha = 0.5f) else WarningAmber.copy(alpha = 0.5f),
            contentPadding = 14.dp
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = if (isAccessibilityActive) Icons.Filled.CheckCircle else Icons.Filled.Warning,
                            contentDescription = null,
                            tint = if (isAccessibilityActive) NeonEmerald else WarningAmber,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = if (isAccessibilityActive) "Accessibility Service Faol" else "Accessibility Xizmati O'chiq",
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (!isAccessibilityActive) {
                        Button(
                            onClick = {
                                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                context.startActivity(intent)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = SpaceDarkBg),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("open_accessibility_settings")
                        ) {
                            Text("Yoqish", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Text(
                    text = "Nova boshqa ilovalarni (masalan Telegram, WhatsApp, Sozlamalar) ochib, matn yozish va tugmalarni bosishi uchun Accessibility ruxsati zarur.",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
            }
        }

        // Floating Overlay Bubble Banner
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            borderColor = if (isOverlayActive) CyberCyan.copy(alpha = 0.5f) else SpaceCardBorder,
            contentPadding = 14.dp
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Floating Screen Overlay Bubble",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Har qanday ilova ustida turuvchi suzuvchi Nova nishoni",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }

                Button(
                    onClick = {
                        if (!Settings.canDrawOverlays(context)) {
                            val intent = Intent(
                                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                Uri.parse("package:${context.packageName}")
                            ).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
                            context.startActivity(intent)
                        } else {
                            viewModel.toggleOverlayService(context, !isOverlayActive)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isOverlayActive) NeonPurple else SpaceSurface,
                        contentColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("toggle_overlay_button")
                ) {
                    Text(if (isOverlayActive) "Faol" else "Yoqish", fontSize = 11.sp)
                }
            }
        }

        // Automation Workflows Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "SAQLANGAN AVTOMATIK SKRIPTLAR",
                color = TextMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }

        // Shortcuts List
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            shortcuts.forEach { shortcut ->
                AutomationShortcutItem(
                    shortcut = shortcut,
                    onRun = {
                        viewModel.handleUserPrompt(shortcut.triggerPhrase, isVoice = true)
                    }
                )
            }
        }

        // Live Screen Node Inspector (Advanced Dev Tool)
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = 14.dp
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Filled.Layers, contentDescription = null, tint = ElectricBlue, modifier = Modifier.size(18.dp))
                        Text(
                            text = "Screen Hierarchy Inspector",
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        IconButton(
                            onClick = {
                                viewModel.refreshScreenHierarchy()
                                showHierarchy = true
                            },
                            modifier = Modifier.size(32.dp).testTag("refresh_hierarchy_button")
                        ) {
                            Icon(Icons.Filled.Refresh, contentDescription = "Refresh", tint = CyberCyan)
                        }
                    }
                }

                Text(
                    text = "Aktiv ekrandagi barcha matnlar, tugmalar va bosiluvchi elementlar daraxtini tekshirish.",
                    color = TextMuted,
                    fontSize = 11.sp
                )

                if (showHierarchy && screenHierarchy.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(SpaceDarkBg)
                            .border(1.dp, SpaceCardBorder, RoundedCornerShape(8.dp))
                            .padding(8.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = screenHierarchy,
                            color = CyberCyan,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 14.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun AutomationShortcutItem(
    shortcut: AutomationShortcut,
    onRun: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = SpaceCardBg,
        border = androidx.compose.foundation.BorderStroke(1.dp, SpaceCardBorder)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(CyberCyan.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.TouchApp,
                    contentDescription = null,
                    tint = CyberCyan,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = shortcut.title,
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "\"${shortcut.triggerPhrase}\"",
                    color = CyberCyan,
                    fontSize = 11.sp
                )
                Text(
                    text = "Target: ${shortcut.targetAppPackage}",
                    color = TextMuted,
                    fontSize = 10.sp
                )
            }

            IconButton(
                onClick = onRun,
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(NeonEmerald)
                    .testTag("run_shortcut_${shortcut.id}")
            ) {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = "Run",
                    tint = SpaceDarkBg,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
