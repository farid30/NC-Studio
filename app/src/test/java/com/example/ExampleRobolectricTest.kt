package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.connection.GrblProtocol
import com.example.model.MachineState
import com.example.parser.GCodeParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("NCStudio ESP32", appName)
    }

    @Test
    fun `parse grbl status report`() {
        val reportStr = "<Idle|MPos:10.500,20.000,-2.500|WPos:5.500,10.000,-1.000|FS:1200,15000>"
        val report = GrblProtocol.parseStatusReport(reportStr)
        assertNotNull(report)
        assertEquals(MachineState.IDLE, report?.state)
        assertEquals(10.500f, report?.mPos?.x ?: 0f, 0.001f)
        assertEquals(20.000f, report?.mPos?.y ?: 0f, 0.001f)
        assertEquals(-2.500f, report?.mPos?.z ?: 0f, 0.001f)
        assertEquals(1200f, report?.feedRate ?: 0f, 0.1f)
        assertEquals(15000f, report?.spindleRpm ?: 0f, 0.1f)
    }

    @Test
    fun `parse gcode program segments and bounds`() {
        val gcode = """
            G21 ; mm
            G90
            G0 X0 Y0 Z5
            G1 Z-1 F300
            G1 X40 Y0 F1000
            G1 X40 Y30
            G0 Z5
        """.trimIndent()

        val program = GCodeParser.parse("test_cut", "Test Cut", "Simple contour", gcode)
        assertEquals(5, program.segments.size)
        assertTrue(program.bounds.width >= 40f)
        assertTrue(program.bounds.height >= 30f)
    }
}
