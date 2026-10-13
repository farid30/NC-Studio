package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import com.example.ui.theme.NcBorder
import com.example.ui.theme.NcDarkBackground
import com.example.ui.theme.NcDarkSurfaceElevated
import com.example.ui.theme.NcDarkSurfaceVariant
import com.example.ui.theme.NcPrimaryCyan
import com.example.ui.theme.NcSecondaryAmber
import com.example.ui.theme.NcTertiaryGreen
import com.example.ui.theme.NcTextMuted
import com.example.ui.theme.NcTextPrimary
import com.example.ui.theme.NcTextSecondary
import com.example.util.ApkExporter
import java.util.Locale

@Composable
fun ApkDownloadDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var statusMessage by remember { mutableStateOf<String?>(null) }
    val apkSizeMb = remember { ApkExporter.getApkSizeMb(context) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = NcDarkSurfaceVariant,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(NcTertiaryGreen.copy(alpha = 0.2f))
                        .border(1.dp, NcTertiaryGreen, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Android,
                        contentDescription = null,
                        tint = NcTertiaryGreen,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Column {
                    Text(
                        text = "Download NC Studio APK",
                        fontWeight = FontWeight.Bold,
                        color = NcTextPrimary,
                        fontSize = 17.sp
                    )
                    Text(
                        text = "Standalone Android Package",
                        fontSize = 11.sp,
                        color = NcTextSecondary
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // File Specs Card
                Surface(
                    color = NcDarkBackground,
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NcBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        SpecRow("Package:", "com.aistudio.ncstudioesp.wxtp")
                        SpecRow("File Name:", "NCStudio-ESP32.apk")
                        SpecRow("Version:", "1.0 (Release Build)")
                        SpecRow("File Size:", String.format(Locale.US, "%.1f MB", apkSizeMb))
                        SpecRow("Target OS:", "Android 7.0 - 16 (API 24-36)")
                    }
                }

                if (statusMessage != null) {
                    Surface(
                        color = NcTertiaryGreen.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, NcTertiaryGreen)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = NcTertiaryGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = statusMessage!!,
                                fontSize = 11.sp,
                                color = NcTextPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                // Primary Action Buttons
                Button(
                    onClick = {
                        val (success, msg) = ApkExporter.saveApkToDownloads(context)
                        statusMessage = msg
                        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("save_apk_to_downloads_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NcPrimaryCyan,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "SAVE APK TO DOWNLOADS",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }

                OutlinedButton(
                    onClick = {
                        val (_, msg) = ApkExporter.shareApk(context)
                        statusMessage = msg
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("share_apk_file_button"),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = NcSecondaryAmber
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "SHARE APK VIA BLUETOOTH / DRIVE",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }

                // AI Studio Cloud Export Guide
                Surface(
                    color = NcDarkSurfaceElevated,
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NcBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = NcPrimaryCyan,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "EXPORT FROM AI STUDIO MENU",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = NcPrimaryCyan
                            )
                        }
                        Text(
                            text = "You can also download the signed production APK directly in your browser: click the Settings/Menu icon at the top right of the Google AI Studio screen and select 'Export APK' or 'Download Project ZIP'.",
                            fontSize = 11.sp,
                            color = NcTextSecondary,
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = NcDarkSurfaceElevated,
                    contentColor = NcTextPrimary
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Close")
            }
        }
    )
}

@Composable
private fun SpecRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = NcTextMuted, fontSize = 11.sp)
        Text(
            text = value,
            color = NcTextPrimary,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.SemiBold
        )
    }
}
