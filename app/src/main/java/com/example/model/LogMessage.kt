package com.example.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.NcAlarmRed
import com.example.ui.theme.NcPrimaryCyan
import com.example.ui.theme.NcRedEmergency
import com.example.ui.theme.NcSecondaryAmber
import com.example.ui.theme.NcTertiaryGreen
import com.example.ui.theme.NcTextMuted
import com.example.ui.theme.NcTextSecondary

enum class LogMessageType(val color: Color, val tag: String) {
    COMMAND_SENT(NcPrimaryCyan, "TX >"),
    RESPONSE_OK(NcTertiaryGreen, "RX <"),
    RESPONSE_ERROR(NcRedEmergency, "ERR!"),
    STATUS_REPORT(NcTextMuted, "POLL"),
    ALARM(NcAlarmRed, "ALARM"),
    SYSTEM(NcSecondaryAmber, "SYS")
}

data class LogMessage(
    val id: Long = System.nanoTime(),
    val timestamp: String,
    val message: String,
    val type: LogMessageType
)
