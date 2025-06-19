package com.example.ecgwidgetsviewer

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.LinearLayout
import android.view.LayoutInflater

class MainActivity : AppCompatActivity() {
    private lateinit var graphWidget: GraphWidget
    private lateinit var containerLayout: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        containerLayout = findViewById(R.id.containerLayout)

        val inflater = LayoutInflater.from(this)
        val ecgView = inflater.inflate(R.layout.card_view_layout, containerLayout, false)
        graphWidget = ecgView.findViewById(R.id.painting_view)
        containerLayout.addView(ecgView)

        // Configura el modo y longitud de la señal (por ejemplo, 240 muestras, modo flowing)
        graphWidget.setMode(240, GraphMode.flowing)
    }

    override fun onDestroy() {
        super.onDestroy()
        graphWidget.stop() // Detén el widget y el simulador al salir
    }
}