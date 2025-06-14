package com.example.ecgwidgetsviewer

import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.Socket

class ConectSimu(private val onDataReceived: (Int) -> Unit) {

    fun startClient(ip: String = "192.168.0.11", port: Int = 12345) {
        Thread {
            try {
                val socket = Socket(ip, port)
                val reader = BufferedReader(InputStreamReader(socket.getInputStream()))

                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    val bpm = line!!.trim().toIntOrNull()
                    if (bpm != null) {
                        onDataReceived(bpm)
                    }
                }

                reader.close()
                socket.close()

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }
}
