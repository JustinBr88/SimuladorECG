package com.example.ecgwidgetsviewer

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.LinearLayout
import android.view.LayoutInflater
import com.example.ecgwidgetsviewer.GraphWidget
import com.example.ecgwidgetsviewer.GraphMode
import com.example.ecgwidgetsviewer.StoreWrapper
import com.example.ecgwidgetsviewer.Circular
import com.example.ecgwidgetsviewer.ConectSimu


class MainActivity : AppCompatActivity() {

    private lateinit var graphWidget: GraphWidget
    private lateinit var containerLayout: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        containerLayout = findViewById(R.id.containerLayout)

        // Agregar automáticamente un solo widget ECG
        agregarECGWidgetUnico()
        graphWidget = findViewById(R.id.graphWidget)

        // Inicializa el gráfico con tamaño de buffer y modo gráfico (flow o overlay)
        graphWidget.setMod(seriesLength = 200, mode = GraphMode.flow)

        // Inicia el gráfico
        graphWidget.start()

        // Comienza a simular datos ECG automáticamente
        startDataSimulation()
    }
    private fun agregarECGWidgetUnico() {
        val inflater = LayoutInflater.from(this)
        val ecgView = inflater.inflate(R.layout.cart_view_layout, containerLayout, false)

        // Opcional: iniciar simulación desde aquí si tienes una clase como ConectSimu
        val graphWidget = ecgView.findViewById<GraphWidget>(R.id.painting_view)
        val storeWrapper = StoreWrapper()
        val circular = Circular() // Asegúrate de que esté correctamente importado
        ConectSimu(graphWidget, storeWrapper, circular).start()

        containerLayout.addView(ecgView)
    }
    private fun startDataSimulation() {
        Thread {
            while (true) {
                val simulatedValue = (-200..200).random() // Puedes ajustar el rango
                graphWidget.post {
                    graphWidget.addDataPoint(simulatedValue)
                }
                Thread.sleep(50) // 20 Hz aprox (ajustable)
            }
        }.start()
    }
}
