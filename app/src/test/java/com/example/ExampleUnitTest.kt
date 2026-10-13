package com.example

import com.example.connection.GrblProtocol
import org.junit.Assert.assertEquals
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testJogCommandFormatting() {
        val jogCmd = GrblProtocol.buildJogCommand("X", 10.0f, 1500f)
        assertEquals("\$J=G91 G21 X10.000 F1500", jogCmd)
    }

    @Test
    fun testZeroAxisCommandFormatting() {
        val zeroCmd = GrblProtocol.buildZeroAxisCommand("Z", 0f)
        assertEquals("G10 L20 P1 Z0.000", zeroCmd)
    }
}
