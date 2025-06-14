package com.example.ecgwidgetsviewer

import android.graphics.Point
import android.graphics.Path
import android.util.Size

class StoreWrapper(
    private val capacity: Int,
    private val scaling: Int,
    private var graphMode: GraphMode
) {
    private var data = IntArray(capacity)
    private var currentIndex = 0
    private var isFilled = false

    var pathBefore: Path? = null
    var pathAfter: Path? = null
    var point: Point? = null

    fun write(value: Int) {
        data[currentIndex] = value
        currentIndex = (currentIndex + 1) % capacity
        if (currentIndex == 0) isFilled = true
    }

    fun getCurrentData(): List<Int> {
        val output = mutableListOf<Int>()
        val total = if (isFilled) capacity else currentIndex
        var idx = if (isFilled) currentIndex else 0
        for (i in 0 until total) {
            output.add(data[idx])
            idx = (idx + 1) % capacity
        }
        return output
    }

    fun clear() {
        data.fill(0)
        currentIndex = 0
        isFilled = false
    }

    fun prepareDrawing(size: Size, shiftH: Double) {
        val width = size.width
        val height = size.height
        val deltaX = width.toFloat() / capacity
        val midY = height / 2.0

        pathBefore = Path()
        pathAfter = Path()

        var index = if (isFilled) currentIndex else 0
        val totalPoints = if (isFilled) capacity else currentIndex

        for (i in 0 until totalPoints) {
            val x = i * deltaX
            val y = (midY + (data[index] / scaling.toDouble())).toFloat()

            if (i == 0) {
                pathBefore!!.moveTo(x, y)
            } else {
                pathBefore!!.lineTo(x, y)
            }

            index = (index + 1) % capacity
        }

        val lastX = totalPoints * deltaX
        val lastY = (midY + (data[(currentIndex - 1 + capacity) % capacity] / scaling.toDouble())).toFloat()
        point = Point(lastX.toInt(), lastY.toInt())
    }

    fun updateBuffer(counter: Int) {
        // Placeholder por si luego quieres usarlo
    }

    fun isFull(): Boolean = isFilled

    fun mode(): GraphMode = graphMode

    fun setMode(mode: GraphMode) {
        this.graphMode = mode
    }

    fun drawingFrequency(): Int = 60
}
