package com.example.ecgwidgetsviewer

import android.graphics.Path
import android.graphics.Point
import android.util.Size
import kotlinx.coroutines.*
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import kotlin.math.*
import kotlin.random.Random

enum class SimuEcgMode { NORMAL, ALERT, CRITICAL }

class SimuECG(
    private val seriesLength: Int,
    private val seriesNumber: Int,
    var mode: SimuEcgMode = SimuEcgMode.NORMAL,
    // Callback para actualizar el estado recibido desde la IA (opcional)
    private val estadoCallback: ((String) -> Unit)? = null
) {
    private val freq = 24 // Hz
    private val periodMs = 1000 // ms
    private val drawSeriesLength = (seriesLength / (periodMs.toDouble() / freq)).toInt() + 1
    private val buffer = CircularBuffer<Double>(seriesLength * seriesNumber)
    private var job: Job? = null
    private var step = 0.0

    // Para graficar
    var path: Path? = null
    var pathBefore: Path? = null
    var pathAfter: Path? = null
    var point: Point? = null

    private fun getDynamicHeartRate(): Double {
        return when (mode) {
            SimuEcgMode.NORMAL -> Random.nextInt(60, 101).toDouble()
            SimuEcgMode.ALERT -> if (Random.nextBoolean()) Random.nextInt(40, 60).toDouble() else Random.nextInt(101, 121).toDouble()
            SimuEcgMode.CRITICAL -> if (Random.nextBoolean()) Random.nextInt(10, 26).toDouble() else Random.nextInt(150, 181).toDouble()
        }
    }

    private fun generateEcgSample(t: Double): Double {
        val heartRate = getDynamicHeartRate()
        // ENVÍA el BPM generado a la API cada vez que se genera
        enviarBpmAlServidor(heartRate)
        val period = 60.0 / heartRate
        val phase = (t % period) / period * 2 * Math.PI

        val p = 0.1 * sin(phase - 0.2 * Math.PI)
        val q = -0.15 * exp(-((phase - 0.35 * Math.PI).pow(2)) / 0.07)
        val r = 1.0 * exp(-((phase - 0.5 * Math.PI).pow(2)) / 0.05)
        val s = -0.25 * exp(-((phase - 0.6 * Math.PI).pow(2)) / 0.05)
        val tWave = 0.3 * sin(phase - 0.8 * Math.PI) * exp(-((phase - 0.8 * Math.PI).pow(2)) / 0.2)
        val noise = 0.05 * (Random.nextDouble() - 0.5)
        return 1500.0 + 300 * (p + q + r + s + tWave + noise)
    }

    fun start() {
        job = CoroutineScope(Dispatchers.IO).launch {
            var t = 0.0
            val dt = 1.0 / freq
            while (isActive) {
                val value = generateEcgSample(t)
                buffer.write(value)
                t += dt
                delay((1000 / freq).toLong())
            }
        }
    }

    fun stop() {
        job?.cancel()
    }

    // --- Métodos para graficar, adaptados a tu buffer ---

    fun drawingFrequency(): Int = drawSeriesLength

    // Métodos min/max para el buffer
    val min: Double
        get() = buffer.toList().filterNotNull().minOrNull() ?: 0.0
    val max: Double
        get() = buffer.toList().filterNotNull().maxOrNull() ?: 0.0

    fun prepareData(size: Size, shiftH: Double): List<Double> {
        val width = size.width.toDouble()
        val height = size.height.toDouble()
        val allData = buffer.toList().filterNotNull()
        val n = seriesLength // solo las N muestras más recientes

        val dataTemp = if (allData.size > n) {
            allData.takeLast(n)
        } else {
            allData
        }

        var minV = dataTemp.minOrNull() ?: 0.0
        var maxV = dataTemp.maxOrNull() ?: 0.0

        if (minV == maxV) {
            minV /= 2
            maxV += minV / 2
        }
        val dv = maxV - minV
        step = if (dataTemp.size > 1) width / (dataTemp.size - 1) else width
        val coeff = if (dv != 0.0) (height - 2 * shiftH) / dv else 1.0
        return dataTemp.map { (maxV - it) * coeff + shiftH }
    }

    fun preparePath(data: List<Double>): Path {
        val path = Path()
        if (data.isEmpty()) return path
        path.moveTo(0f, data[0].toFloat())
        for (i in 1 until data.size) {
            path.lineTo((i * step).toFloat(), data[i].toFloat())
        }
        return path
    }

    fun preparePathBefore(data: List<Double>): Path = preparePath(data)
    fun preparePathAfter(data: List<Double>): Path = Path()
    fun preparePoint(data: List<Double>): Point {
        val idx = data.lastIndex.coerceAtLeast(0)
        return Point((idx * step).toInt(), data.getOrNull(idx)?.toInt() ?: 0)
    }

    fun prepareDrawing(size: Size, shiftH: Double) {
        val data = prepareData(size, shiftH)
        path = preparePath(data)
        pathBefore = preparePathBefore(data)
        pathAfter = preparePathAfter(data)
        point = preparePoint(data)
    }

    fun isFull(): Boolean = buffer.toList().size >= bufferCapacity()

    private fun bufferCapacity(): Int = seriesLength * seriesNumber

    fun buffer(): CircularBuffer<Double> = buffer

    // ----------- ENVÍO DE BPM A LA API -----------
    private fun enviarBpmAlServidor(bpm: Double) {
        val client = OkHttpClient()
        val json = JSONObject()
        json.put("heart_rate", bpm)
        val body = RequestBody.create(MediaType.get("application/json; charset=utf-8"), json.toString())
        val request = Request.Builder()
            .url("http://TU_IP_LOCAL:5000/predict") // Cambia TU_IP_LOCAL por la IP real donde corre Flask
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // Puedes manejar errores aquí si lo deseas
            }
            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (response.isSuccessful) {
                        val jsonRespuesta = JSONObject(response.body()!!.string())
                        val estado = jsonRespuesta.getString("condition")
                        // Llama al callback si está definido, para actualizar la UI
                        estadoCallback?.invoke(estado)
                    }
                }
            }
        })
    }
}