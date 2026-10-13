package com.example.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.NcAlarmRed
import com.example.ui.theme.NcPrimaryCyan
import com.example.ui.theme.NcRedEmergency
import com.example.ui.theme.NcSecondaryAmber
import com.example.ui.theme.NcTertiaryGreen
import com.example.ui.theme.NcTextMuted

enum class MachineState(val label: String, val badgeColor: Color) {
    IDLE("IDLE", NcTertiaryGreen),
    RUN("RUNNING", NcPrimaryCyan),
    HOLD("FEED HOLD", NcSecondaryAmber),
    JOG("JOGGING", NcPrimaryCyan),
    ALARM("ALARM LOCK", NcAlarmRed),
    HOME("HOMING", NcSecondaryAmber),
    CHECK("CHECK MODE", NcSecondaryAmber),
    SLEEP("SLEEP", NcTextMuted),
    DISCONNECTED("OFFLINE", NcTextMuted);

    companion object {
        fun fromGrblString(raw: String): MachineState {
            val upper = raw.trim().uppercase()
            return when {
                upper.startsWith("IDLE") -> IDLE
                upper.startsWith("RUN") -> RUN
                upper.startsWith("HOLD") -> HOLD
                upper.startsWith("JOG") -> JOG
                upper.startsWith("ALARM") -> ALARM
                upper.startsWith("HOME") -> HOME
                upper.startsWith("CHECK") -> CHECK
                upper.startsWith("SLEEP") -> SLEEP
                else -> IDLE
            }
        }
    }
}
