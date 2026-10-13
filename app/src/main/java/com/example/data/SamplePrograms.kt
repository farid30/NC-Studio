package com.example.data

import com.example.model.GCodeProgram
import com.example.parser.GCodeParser

object SamplePrograms {

    private val ncLogoGcode = """
        (NC Studio Logo & Mill Test)
        G21 ; Millimeters
        G90 ; Absolute coordinates
        G17 ; XY Plane
        M3 S12000 ; Spindle ON at 12000 RPM
        G0 Z5.000 F3000 ; Safe Z height
        G0 X0.000 Y0.000 ; Move to Origin
        
        ; Outer Border Frame 50x30mm
        G0 X5.000 Y5.000
        G1 Z-0.500 F300 ; Plunge cut
        G1 X45.000 Y5.000 F800 ; Bottom edge
        G1 X45.000 Y25.000 ; Right edge
        G1 X5.000 Y25.000 ; Top edge
        G1 X5.000 Y5.000 ; Left edge
        G0 Z5.000 ; Retract
        
        ; Letter 'N'
        G0 X10.000 Y9.000
        G1 Z-0.500 F300
        G1 X10.000 Y21.000 F800
        G1 X17.000 Y9.000
        G1 X17.000 Y21.000
        G0 Z5.000
        
        ; Letter 'C'
        G0 X25.000 Y21.000
        G1 Z-0.500 F300
        G1 X20.000 Y21.000 F800
        G1 X20.000 Y9.000
        G1 X25.000 Y9.000
        G0 Z5.000
        
        ; Center Spindle Crosshair
        G0 X32.000 Y15.000
        G1 Z-0.500 F300
        G1 X40.000 Y15.000 F800
        G0 Z5.000
        G0 X36.000 Y11.000
        G1 Z-0.500 F300
        G1 X36.000 Y19.000 F800
        G0 Z5.000
        
        ; Circle around Crosshair
        G0 X36.000 Y20.000
        G1 Z-0.500 F300
        G1 X38.828 Y17.828 F800
        G1 X40.000 Y15.000
        G1 X38.828 Y12.172
        G1 X36.000 Y10.000
        G1 X33.172 Y12.172
        G1 X32.000 Y15.000
        G1 X33.172 Y17.828
        G1 X36.000 Y20.000
        G0 Z10.000 ; Safe Retract
        
        ; Return to Origin
        G0 X0.000 Y0.000
        M5 ; Spindle OFF
        M30 ; Program End
    """.trimIndent()

    private val surfaceFacingGcode = """
        (Surface Facing & Spoilboard Leveling 60x60mm)
        G21 ; Metric
        G90 ; Absolute
        M3 S15000 ; Spindle ON
        G0 Z10.000 F3000
        G0 X0.000 Y0.000
        
        ; Facing passes with 8mm stepover
        G0 X0.000 Y0.000
        G1 Z-0.200 F400
        G1 X60.000 Y0.000 F1500
        G1 X60.000 Y8.000
        G1 X0.000 Y8.000
        G1 X0.000 Y16.000
        G1 X60.000 Y16.000
        G1 X60.000 Y24.000
        G1 X0.000 Y24.000
        G1 X0.000 Y32.000
        G1 X60.000 Y32.000
        G1 X60.000 Y40.000
        G1 X0.000 Y40.000
        G1 X0.000 Y48.000
        G1 X60.000 Y48.000
        G1 X60.000 Y56.000
        G1 X0.000 Y56.000
        G1 X0.000 Y60.000
        G1 X60.000 Y60.000
        
        G0 Z15.000 F3000
        G0 X0.000 Y0.000
        M5
        M30
    """.trimIndent()

    private val circularPocketGcode = """
        (Circular Pocket Ø40mm Spiral Roughing)
        G21 ; mm
        G90 ; Absolute
        M3 S18000
        G0 Z5.000
        G0 X25.000 Y25.000 ; Pocket Center
        
        ; Spiral Outward passes
        G1 Z-1.000 F250
        ; R=3mm
        G1 X28.000 Y25.000 F1000
        G1 X25.000 Y28.000
        G1 X22.000 Y25.000
        G1 X25.000 Y22.000
        G1 X28.000 Y25.000
        ; R=6mm
        G1 X31.000 Y25.000
        G1 X25.000 Y31.000
        G1 X19.000 Y25.000
        G1 X25.000 Y19.000
        G1 X31.000 Y25.000
        ; R=10mm
        G1 X35.000 Y25.000
        G1 X32.071 Y32.071
        G1 X25.000 Y35.000
        G1 X17.929 Y32.071
        G1 X15.000 Y25.000
        G1 X17.929 Y17.929
        G1 X25.000 Y15.000
        G1 X32.071 Y17.929
        G1 X35.000 Y25.000
        ; R=15mm
        G1 X40.000 Y25.000
        G1 X35.607 Y35.607
        G1 X25.000 Y40.000
        G1 X14.393 Y35.607
        G1 X10.000 Y25.000
        G1 X14.393 Y14.393
        G1 X25.000 Y10.000
        G1 X35.607 Y14.393
        G1 X40.000 Y25.000
        ; R=20mm (Final boundary)
        G1 X45.000 Y25.000
        G1 X39.142 Y39.142
        G1 X25.000 Y45.000
        G1 X10.858 Y39.142
        G1 X5.000 Y25.000
        G1 X10.858 Y10.858
        G1 X25.000 Y5.000
        G1 X39.142 Y10.858
        G1 X45.000 Y25.000
        
        G0 Z10.000
        G0 X0.000 Y0.000
        M5
        M30
    """.trimIndent()

    private val steppedPyramidGcode = """
        (Stepped Pyramid 3D Profile 40x40mm)
        G21 ; mm
        G90 ; Absolute
        M3 S14000
        G0 Z5.000
        
        ; Level 1 (Z-1.0, 40x40mm)
        G0 X5.000 Y5.000
        G1 Z-1.000 F300
        G1 X45.000 Y5.000 F1200
        G1 X45.000 Y45.000
        G1 X5.000 Y45.000
        G1 X5.000 Y5.000
        G0 Z5.000
        
        ; Level 2 (Z-2.0, 32x32mm)
        G0 X9.000 Y9.000
        G1 Z-2.000 F300
        G1 X41.000 Y9.000 F1100
        G1 X41.000 Y41.000
        G1 X9.000 Y41.000
        G1 X9.000 Y9.000
        G0 Z5.000
        
        ; Level 3 (Z-3.0, 24x24mm)
        G0 X13.000 Y13.000
        G1 Z-3.000 F300
        G1 X37.000 Y13.000 F1000
        G1 X37.000 Y37.000
        G1 X13.000 Y37.000
        G1 X13.000 Y13.000
        G0 Z5.000
        
        ; Level 4 (Z-4.0, 16x16mm)
        G0 X17.000 Y17.000
        G1 Z-4.000 F300
        G1 X33.000 Y17.000 F900
        G1 X33.000 Y33.000
        G1 X17.000 Y33.000
        G1 X17.000 Y17.000
        G0 Z10.000
        
        G0 X0.000 Y0.000
        M5
        M30
    """.trimIndent()

    val all: List<GCodeProgram> by lazy {
        listOf(
            GCodeParser.parse(
                id = "prog_nc_logo",
                name = "NC Studio Logo & Reticle",
                description = "Vector engraving of NC Studio logo, text, and crosshair reticle (50x30mm)",
                rawGcode = ncLogoGcode
            ),
            GCodeParser.parse(
                id = "prog_facing",
                name = "Surface Leveling (60x60mm)",
                description = "Raster zig-zag surfacing passes for spoilboard leveling",
                rawGcode = surfaceFacingGcode
            ),
            GCodeParser.parse(
                id = "prog_pocket",
                name = "Circular Pocket (Ø40mm)",
                description = "Spiral roughing pocket cut with smooth stepover",
                rawGcode = circularPocketGcode
            ),
            GCodeParser.parse(
                id = "prog_pyramid",
                name = "Stepped Pyramid 3D Profile",
                description = "Concentric 4-tier milling profile with depth levels down to Z -4mm",
                rawGcode = steppedPyramidGcode
            )
        )
    }
}
