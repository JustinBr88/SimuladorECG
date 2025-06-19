package com.example.ecgwidgetsviewer

class CircularBuffer<T>(private val capacity: Int) {
    private val buffer: ArrayList<T?> = ArrayList(List(capacity) { null })
    private var head = 0
    private var size = 0

    fun write(value: T) {
        buffer[head] = value
        head = (head + 1) % capacity
        if (size < capacity) size++
    }

    fun toList(): List<T> {
        val out = ArrayList<T>(size)
        val start = if (size == capacity) head else 0
        for (i in 0 until size) {
            val idx = (start + i) % capacity
            buffer[idx]?.let { out.add(it) }
        }
        return out
    }

    fun clear() {
        for (i in buffer.indices) buffer[i] = null
        head = 0
        size = 0
    }
}