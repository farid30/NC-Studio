package com.example.connection

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import com.example.model.ConnectionStatus
import com.example.model.ConnectionType
import com.example.model.Esp32ConnectionConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.net.InetSocketAddress
import java.net.Socket
import java.util.UUID
import java.util.concurrent.TimeUnit

class Esp32ControllerManager(
    private val context: Context,
    private val scope: CoroutineScope
) {
    private val _connectionStatus = MutableStateFlow(ConnectionStatus.DISCONNECTED)
    val connectionStatus: StateFlow<ConnectionStatus> = _connectionStatus.asStateFlow()

    private val _incomingMessages = MutableSharedFlow<String>(extraBufferCapacity = 128)
    val incomingMessages: SharedFlow<String> = _incomingMessages.asSharedFlow()

    private var currentConfig = Esp32ConnectionConfig()
    private var simulator: Esp32Simulator? = null

    // Sockets & streams
    private var tcpSocket: Socket? = null
    private var tcpWriter: OutputStreamWriter? = null
    private var btSocket: BluetoothSocket? = null
    private var btWriter: OutputStream? = null
    private var okWebSocket: WebSocket? = null
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    private var readerJob: Job? = null
    private var pollerJob: Job? = null

    companion object {
        private val SPP_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    }

    fun connect(config: Esp32ConnectionConfig) {
        disconnect()
        currentConfig = config
        _connectionStatus.value = ConnectionStatus.CONNECTING

        scope.launch(Dispatchers.IO) {
            when (config.type) {
                ConnectionType.SIMULATOR -> {
                    connectSimulator()
                }
                ConnectionType.WIFI_TELNET -> {
                    connectTcpSocket(config.hostIp, config.port)
                }
                ConnectionType.WEBSOCKET -> {
                    connectWebSocket(config.hostIp, config.port)
                }
                ConnectionType.HTTP_REST -> {
                    // For HTTP, mark connected if ping succeeds
                    connectHttp(config.hostIp, config.port)
                }
                ConnectionType.BLUETOOTH -> {
                    connectBluetooth(config.bluetoothDeviceAddress)
                }
            }
        }
    }

    private suspend fun connectSimulator() {
        val sim = Esp32Simulator(scope)
        simulator = sim
        sim.start()
        _connectionStatus.value = ConnectionStatus.CONNECTED

        scope.launch {
            sim.incomingMessages.collect { msg ->
                _incomingMessages.emit(msg)
            }
        }
    }

    private suspend fun connectTcpSocket(host: String, port: Int) {
        try {
            val socket = Socket()
            socket.connect(InetSocketAddress(host, port), 4000)
            tcpSocket = socket
            val writer = OutputStreamWriter(socket.getOutputStream(), Charsets.UTF_8)
            tcpWriter = writer
            _connectionStatus.value = ConnectionStatus.CONNECTED
            _incomingMessages.emit("[SYS:Connected to ESP32 at $host:$port (Telnet)]")

            startPolling()
            startTcpReader(socket)
        } catch (e: Exception) {
            _connectionStatus.value = ConnectionStatus.ERROR
            _incomingMessages.emit("[SYS:Connection Error: ${e.message}]")
        }
    }

    private fun startTcpReader(socket: Socket) {
        readerJob?.cancel()
        readerJob = scope.launch(Dispatchers.IO) {
            try {
                val reader = BufferedReader(InputStreamReader(socket.getInputStream(), Charsets.UTF_8))
                while (isActive && !socket.isClosed) {
                    val line = reader.readLine() ?: break
                    _incomingMessages.emit(line)
                }
            } catch (e: Exception) {
                if (isActive) {
                    _incomingMessages.emit("[SYS:Telnet stream disconnected: ${e.message}]")
                    _connectionStatus.value = ConnectionStatus.DISCONNECTED
                }
            }
        }
    }

    private fun connectWebSocket(host: String, port: Int) {
        val url = if (port == 80) "ws://$host/" else "ws://$host:$port/"
        val request = Request.Builder().url(url).build()

        okWebSocket = okHttpClient.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                _connectionStatus.value = ConnectionStatus.CONNECTED
                scope.launch {
                    _incomingMessages.emit("[SYS:WebSocket Connected to $url]")
                    startPolling()
                }
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                scope.launch {
                    text.lines().filter { it.isNotBlank() }.forEach { line ->
                        _incomingMessages.emit(line)
                    }
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                _connectionStatus.value = ConnectionStatus.ERROR
                scope.launch {
                    _incomingMessages.emit("[SYS:WebSocket Error: ${t.message}]")
                }
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                _connectionStatus.value = ConnectionStatus.DISCONNECTED
            }
        })
    }

    private suspend fun connectHttp(host: String, port: Int) {
        try {
            val url = "http://$host:$port/command?cmd=?"
            val request = Request.Builder().url(url).get().build()
            val response = okHttpClient.newCall(request).execute()
            if (response.isSuccessful) {
                _connectionStatus.value = ConnectionStatus.CONNECTED
                _incomingMessages.emit("[SYS:Connected to ESP32 HTTP at $host:$port]")
                val body = response.body?.string() ?: ""
                body.lines().forEach { _incomingMessages.emit(it) }
                startPolling()
            } else {
                _connectionStatus.value = ConnectionStatus.ERROR
                _incomingMessages.emit("[SYS:HTTP Connect failed: Code ${response.code}]")
            }
        } catch (e: Exception) {
            _connectionStatus.value = ConnectionStatus.ERROR
            _incomingMessages.emit("[SYS:HTTP Connection Error: ${e.message}]")
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun connectBluetooth(address: String) {
        try {
            val adapter = BluetoothAdapter.getDefaultAdapter()
            if (adapter == null || !adapter.isEnabled) {
                _connectionStatus.value = ConnectionStatus.ERROR
                _incomingMessages.emit("[SYS:Bluetooth is disabled or not supported]")
                return
            }

            val device: BluetoothDevice = adapter.getRemoteDevice(address)
            val socket = device.createRfcommSocketToServiceRecord(SPP_UUID)
            adapter.cancelDiscovery()
            socket.connect()
            btSocket = socket
            btWriter = socket.outputStream

            _connectionStatus.value = ConnectionStatus.CONNECTED
            _incomingMessages.emit("[SYS:Connected to Bluetooth ESP32: ${device.name ?: address}]")

            startPolling()
            startBtReader(socket)
        } catch (e: Exception) {
            _connectionStatus.value = ConnectionStatus.ERROR
            _incomingMessages.emit("[SYS:Bluetooth Connection Failed: ${e.message}]")
        }
    }

    private fun startBtReader(socket: BluetoothSocket) {
        readerJob?.cancel()
        readerJob = scope.launch(Dispatchers.IO) {
            try {
                val reader = BufferedReader(InputStreamReader(socket.inputStream, Charsets.UTF_8))
                while (isActive && socket.isConnected) {
                    val line = reader.readLine() ?: break
                    _incomingMessages.emit(line)
                }
            } catch (e: Exception) {
                if (isActive) {
                    _incomingMessages.emit("[SYS:Bluetooth stream closed: ${e.message}]")
                    _connectionStatus.value = ConnectionStatus.DISCONNECTED
                }
            }
        }
    }

    private fun startPolling() {
        pollerJob?.cancel()
        pollerJob = scope.launch(Dispatchers.IO) {
            while (isActive && _connectionStatus.value == ConnectionStatus.CONNECTED) {
                delay(250)
                sendRaw(GrblProtocol.CMD_STATUS_QUERY, isRealtime = true)
            }
        }
    }

    fun sendCommand(cmd: String) {
        scope.launch(Dispatchers.IO) {
            val trimmed = cmd.trim()
            if (trimmed.isNotBlank()) {
                sendRaw("$trimmed\n", isRealtime = false)
            }
        }
    }

    fun sendRealtime(char: String) {
        scope.launch(Dispatchers.IO) {
            sendRaw(char, isRealtime = true)
        }
    }

    private suspend fun sendRaw(raw: String, isRealtime: Boolean) = withContext(Dispatchers.IO) {
        try {
            when (currentConfig.type) {
                ConnectionType.SIMULATOR -> {
                    simulator?.send(raw.trim())
                }
                ConnectionType.WIFI_TELNET -> {
                    tcpWriter?.let {
                        it.write(raw)
                        it.flush()
                    }
                }
                ConnectionType.WEBSOCKET -> {
                    okWebSocket?.send(raw)
                }
                ConnectionType.HTTP_REST -> {
                    val encoded = java.net.URLEncoder.encode(raw.trim(), "UTF-8")
                    val url = "http://${currentConfig.hostIp}:${currentConfig.port}/command?cmd=$encoded"
                    val req = Request.Builder().url(url).get().build()
                    val res = okHttpClient.newCall(req).execute()
                    val reply = res.body?.string() ?: ""
                    reply.lines().forEach { _incomingMessages.emit(it) }
                }
                ConnectionType.BLUETOOTH -> {
                    btWriter?.let {
                        it.write(raw.toByteArray(Charsets.UTF_8))
                        it.flush()
                    }
                }
            }
        } catch (e: Exception) {
            _incomingMessages.emit("[SYS:Send Error: ${e.message}]")
        }
    }

    fun disconnect() {
        pollerJob?.cancel()
        pollerJob = null
        readerJob?.cancel()
        readerJob = null

        simulator?.stop()
        simulator = null

        try {
            tcpWriter?.close()
            tcpSocket?.close()
        } catch (_: Exception) {}
        tcpWriter = null
        tcpSocket = null

        try {
            okWebSocket?.close(1000, "User disconnected")
        } catch (_: Exception) {}
        okWebSocket = null

        try {
            btWriter?.close()
            btSocket?.close()
        } catch (_: Exception) {}
        btWriter = null
        btSocket = null

        _connectionStatus.value = ConnectionStatus.DISCONNECTED
    }
}
