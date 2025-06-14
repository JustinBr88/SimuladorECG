package com.example.ecgwidgetsviewer

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.LinearLayout
import android.view.LayoutInflater
import android.widget.TextView

class MainActivity : AppCompatActivity() {

    private lateinit var graphWidget: GraphWidget
    private lateinit var containerLayout: LinearLayout
    private lateinit var bpmTextView: TextView
    private var simulator: SimuECG? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        containerLayout = findViewById(R.id.containerLayout)

        // Infla la tarjeta de ECG y agrégala al contenedor
        val inflater = LayoutInflater.from(this)
        val ecgView = inflater.inflate(R.layout.card_view_layout, containerLayout, false)
        graphWidget = ecgView.findViewById(R.id.painting_view)
        containerLayout.addView(ecgView)

        // TextView para mostrar el BPM simulado
        bpmTextView = TextView(this)
        containerLayout.addView(bpmTextView)

        // Inicializa el gráfico
        graphWidget.setMod(seriesLength = 200, mode = GraphMode.flowing)
        graphWidget.start()

        // Inicia el simulador local de BPM
        simulator = SimuECG { bpm ->
            runOnUiThread {
                bpmTextView.text = "BPM simulado: ${bpm.toInt()}"
                graphWidget.addDataPoint(bpm.toInt())
            }
        }
        simulator?.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        simulator?.stop()
    }
}