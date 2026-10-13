package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.NcBorder
import com.example.ui.theme.NcDarkBackground
import com.example.ui.theme.NcDarkSurfaceVariant
import com.example.ui.theme.NcPrimaryCyan
import com.example.ui.theme.NcTextMuted
import com.example.ui.theme.NcTextPrimary

@Composable
fun ZeroingDialog(
    axis: String,
    onDismiss: () -> Unit,
    onConfirm: (Float) -> Unit
) {
    var textValue by remember { mutableStateOf("0.000") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = NcDarkSurfaceVariant,
        title = {
            Text(
                text = "Set Axis $axis Coordinate",
                fontWeight = FontWeight.Bold,
                color = NcPrimaryCyan
            )
        },
        text = {
            Column {
                Text(
                    text = "Specify new workpiece offset for axis $axis (mm):",
                    fontSize = 13.sp,
                    color = NcTextPrimary
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = textValue,
                    onValueChange = { textValue = it },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth().testTag("set_axis_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NcPrimaryCyan,
                        unfocusedBorderColor = NcBorder,
                        focusedTextColor = NcTextPrimary,
                        unfocusedTextColor = NcTextPrimary,
                        cursorColor = NcPrimaryCyan
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("0.000", "5.000", "10.000", "25.000").forEach { preset ->
                        Button(
                            onClick = { textValue = preset },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NcDarkBackground,
                                contentColor = NcPrimaryCyan
                            ),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Text(preset, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val floatVal = textValue.toFloatOrNull() ?: 0f
                    onConfirm(floatVal)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = NcPrimaryCyan,
                    contentColor = androidx.compose.ui.graphics.Color.Black
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("confirm_set_axis_button")
            ) {
                Text("Apply", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_set_axis_button")
            ) {
                Text("Cancel", color = NcTextMuted)
            }
        }
    )
}
