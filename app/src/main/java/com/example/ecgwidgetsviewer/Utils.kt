package com.example.ecgwidgetsviewer

import java.util.Random

class Utils {

    fun extractRangeData(rowData: List<Double?>, start: Int, number: Int): List<Double?> {
        if (rowData.isEmpty() || start < 0 || number <= 0 || start >= rowData.size) {
            return emptyList()
        }
        return if (rowData.size <= start + number) {
            rowData.subList(start, rowData.size)
        } else {
            rowData.subList(start, start + number)
        }
    }

    fun getMinForFullBuffer(buffer: SimuECG): Double {
        val rowData = buffer.buffer().toList().filterNotNull()
        return rowData.minOrNull() ?: 0.0
    }

    fun getMaxForFullBuffer(buffer: SimuECG): Double {
        val rowData = buffer.buffer().toList().filterNotNull()
        return rowData.maxOrNull() ?: 0.0
    }

    fun dataSeriesOverlay(buffer: SimuECG): List<Double> {
        return buffer.buffer().toList().filterNotNull()
    }

    fun randomInRange(min: Int, max: Int): Int {
        require(min < max) { "max must be greater than min" }
        return Random().nextInt(max - min + 1) + min
    }

    // Equivalente a dataSeriesNormal de StoreWrapper para SimuECG
    fun dataSeriesNormal(buffer: SimuECG): List<Double?> {
        return buffer.buffer().toList()
    }
}