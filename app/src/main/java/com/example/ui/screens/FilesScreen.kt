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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.GeneratedDocument
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun FilesScreen(
    viewModel: NovaMainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val documents by viewModel.documents.collectAsState()
    var filePrompt by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SpaceDarkBg)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Title Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "GENERATED FILES & DOCS",
                    color = CyberCyan,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Auto-generated Word, PDF, Code, & Web files",
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Quick File Generator Bar
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
                    value = filePrompt,
                    onValueChange = { filePrompt = it },
                    placeholder = { Text("Masalan: Make a .docx with 1 car image...", color = TextMuted, fontSize = 12.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberCyan,
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedContainerColor = SpaceDarkBg,
                        unfocusedContainerColor = SpaceDarkBg
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f).testTag("files_prompt_input")
                )

                IconButton(
                    onClick = {
                        if (filePrompt.isNotBlank()) {
                            val text = filePrompt.trim()
                            filePrompt = ""
                            viewModel.handleUserPrompt(text, isVoice = false)
                        }
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(CyberCyan)
                        .testTag("files_generate_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Send,
                        contentDescription = "Generate",
                        tint = SpaceDarkBg,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Documents List
        if (documents.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(ElectricBlue.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.FolderOpen,
                            contentDescription = null,
                            tint = ElectricBlue,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Text(
                        text = "Hozircha generatsiya qilingan fayllar yo'q",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )

                    Text(
                        text = "Nova-ga \".docx fayl yarat\" yoki \".pdf hisobot tayyorla\" deb buyruq bering",
                        color = TextMuted,
                        fontSize = 11.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(documents, key = { it.id }) { doc ->
                    DocumentItemCard(
                        doc = doc,
                        onOpen = { viewModel.openGeneratedFile(context, doc) },
                        onShare = { viewModel.shareGeneratedFile(context, doc) }
                    )
                }
            }
        }
    }
}

@Composable
private fun DocumentItemCard(
    doc: GeneratedDocument,
    onOpen: () -> Unit,
    onShare: () -> Unit
) {
    val timeFormat = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())
    val dateString = timeFormat.format(Date(doc.createdAt))

    val (icon, badgeColor) = when (doc.fileType.lowercase()) {
        "docx" -> Icons.Filled.Description to ElectricBlue
        "pdf" -> Icons.Filled.PictureAsPdf to AlertRed
        "py", "js", "html" -> Icons.Filled.Code to NeonEmerald
        else -> Icons.Filled.Description to WarningAmber
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp)),
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
                    .size(46.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(badgeColor.copy(alpha = 0.15f))
                    .border(1.dp, badgeColor.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = badgeColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = doc.title,
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = "${doc.fileType.uppercase()} • ${(doc.fileSize / 1024).coerceAtLeast(1)} KB • $dateString",
                    color = TextMuted,
                    fontSize = 10.sp
                )

                if (doc.query.isNotBlank()) {
                    Text(
                        text = "Query: \"${doc.query}\"",
                        color = CyberCyan,
                        fontSize = 11.sp,
                        maxLines = 1
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(
                    onClick = onShare,
                    modifier = Modifier.size(36.dp).testTag("share_doc_${doc.id}")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Share,
                        contentDescription = "Share",
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(
                    onClick = onOpen,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(badgeColor)
                        .testTag("open_doc_${doc.id}")
                ) {
                    Icon(
                        imageVector = Icons.Filled.OpenInNew,
                        contentDescription = "Open",
                        tint = SpaceDarkBg,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
