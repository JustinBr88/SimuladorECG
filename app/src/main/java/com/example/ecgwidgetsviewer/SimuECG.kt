package com.example.ecgwidgetsviewer

import kotlinx.coroutines.*
import kotlin.random.Random

class SimuECG(private val onBpmGenerated: (Float) -> Unit) {
    private var job: Job? = null

    fun start() {
        job = CoroutineScope(Dispatchers.IO).launch {
            while (isActive) {
                val bpm = Random.nextDouble(60.0, 100.0).toFloat()
                onBpmGenerated(bpm)
                delay(1000) // 1 segundo entre señales
            }
        }
    }

    fun stop() {
        job?.cancel()
    }
}