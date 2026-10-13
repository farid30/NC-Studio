package com.example.model

enum class ConnectionType(val title: String, val defaultPort: Int, val description: String) {
    SIMULATOR("Virtual ESP32 Simulator", 0, "Built-in emulator running GRBL 1.1h for test & visualizer"),
    WIFI_TELNET("WiFi Telnet (Raw Socket)", 23, "Default GRBL-ESP32 / FluidNC raw streaming port"),
    WEBSOCKET("WebSocket (FluidNC WebUI)", 80, "FluidNC / ESP32-CAM websocket streaming"),
    HTTP_REST("HTTP REST API", 80, "Command endpoint (/command?cmd= or /gcode)"),
    BLUETOOTH("Bluetooth Serial (SPP)", 0, "Classic Bluetooth SPP (FluidNC / ESP32-BT)")
}

enum class ConnectionStatus {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    ERROR
}

data class Esp32ConnectionConfig(
    val type: ConnectionType = ConnectionType.SIMULATOR,
    val hostIp: String = "192.168.4.1",
    val port: Int = 23,
    val baudRate: Int = 115200,
    val bluetoothDeviceAddress: String = "",
    val bluetoothDeviceName: String = "ESP32_CNC"
)
