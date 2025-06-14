package com.example.ecgwidgetsviewer

import java.util.Random

class  Utils {

    fun extractRangeData(rowData: List<Int?>, start: Int, number: Int): List<Int?> {
        if (rowData.isEmpty() || start < 0 || number <= 0 || start >= rowData.size) {
            return emptyList()
        }
        return if (rowData.size <= start + number) {
            rowData.subList(start, rowData.size)
        } else {
            rowData.subList(start, start + number)
        }
    }

    fun getMinForFullBuffer(buffer: StoreWrapper): Int {
        val rowData = buffer.getCurrentData()
        return rowData.minOrNull() ?: 0
    }

    fun getMaxForFullBuffer(buffer: StoreWrapper): Int {
        val rowData = buffer.getCurrentData()
        return rowData.maxOrNull() ?: 0
    }

    fun dataSeriesOverlay(buffer: StoreWrapper): List<Int> {
        return buffer.getCurrentData()
    }

    fun randomInRange(min: Int, max: Int): Int {
        require(min < max) { "max must be greater than min" }
        return Random().nextInt(max - min + 1) + min
    }
}
