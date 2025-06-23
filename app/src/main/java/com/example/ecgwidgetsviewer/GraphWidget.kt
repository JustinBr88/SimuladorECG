package com.example.ecgwidgetsviewer

import android.content.Context
import com.example.ecgwidgetsviewer.GraphMode
import com.example.ecgwidgetsviewer.SimuEcgMode
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.Size
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat

class GraphWidget(context: Context?, attrs: AttributeSet?) :
    View(context, attrs) {
    private var startStop = false
    private var currentCounter = 0
    private var simuECG: SimuECG? = null
    private val obtain: Obtain
    private val paintLine: Paint
    private val paintLineAfter: Paint
    private val paintRectPrev: Paint
    private val paintCircle: Paint
    private val canvasColor: Int
    private val markerRadius = 12
    private val path: Path
    private var bitmap: Bitmap? = null
    private var canvas: Canvas? = null
    private var shiftH = 0
    private var size: Size? = null

    // Handler para repintar automáticamente
    private val redrawHandler = Handler(Looper.getMainLooper())
    private val redrawRunnable = object : Runnable {
        override fun run() {
            invalidate()
            redrawHandler.postDelayed(this, 40) // 25 FPS
        }
    }

    init {
        canvasColor = ContextCompat.getColor(context!!,  R.color.canvas_2)
        val canvasPrevColor =
            ContextCompat.getColor(context,  R.color.canvas_1)
        obtain = Obtain(this, 24)
        paintLine = Paint()
        paintLine.color = Color.BLACK
        paintLine.style = Paint.Style.STROKE
        paintLine.strokeWidth = 2f
        paintLineAfter = Paint()
        paintLineAfter.color = Color.GRAY
        paintLineAfter.style = Paint.Style.STROKE
        paintLineAfter.strokeWidth = 2f
        paintRectPrev = Paint()
        paintRectPrev.color = canvasPrevColor
        paintRectPrev.style = Paint.Style.FILL
        paintCircle = Paint()
        paintCircle.color = Color.RED // Set color as per your requirement
        paintCircle.style = Paint.Style.FILL
        path = Path()
    }

    private fun graphModeToSimuEcgMode(graphMode: GraphMode): SimuEcgMode = when (graphMode) {
        GraphMode.NORMAL -> SimuEcgMode.NORMAL
        GraphMode.ALERT -> SimuEcgMode.ALERT
        GraphMode.CRITICAL -> SimuEcgMode.CRITICAL
        else -> SimuEcgMode.NORMAL // O el modo que elijas como default
    }

    fun setMode(seriesLength: Int, mode: GraphMode?) {
        simuECG = SimuECG(seriesLength, 5, graphModeToSimuEcgMode(mode!!))
        simuECG?.start()
        redrawHandler.post(redrawRunnable) // Empieza a repintar automáticamente
    }

    fun setMode(mode: GraphMode?) {
        simuECG?.mode = graphModeToSimuEcgMode(mode!!)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        canvas = Canvas(bitmap!!)
        size = Size(w, h)
        shiftH = h / 6
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(canvasColor)
        if (simuECG != null) {
            simuECG!!.prepareDrawing(size!!, shiftH.toDouble())
            drawProcedure(size, canvas)
        }
        canvas.drawBitmap(bitmap!!, 0f, 0f, null)
        canvas.drawPath(path, paintLine)
    }

    fun drawProcedure(size: Size?, canvas: Canvas) {
        // Si tienes un modo overlay en GraphMode, ajústalo aquí si es necesario
        if (simuECG?.mode == SimuEcgMode.NORMAL) {
            drawFlowingGraph(canvas)
        } else {
            drawFlowingGraph(canvas) // O ajusta según tu lógica
        }
    }

    fun drawFlowingGraph(canvas: Canvas) {
        canvas.drawPath(simuECG!!.pathBefore!!, paintLine)
        canvas.drawPath(simuECG!!.pathAfter!!, paintLine)
        if (!simuECG!!.isFull()) {
            canvas.drawCircle(
                simuECG!!.point!!.x.toFloat(),
                simuECG!!.point!!.y.toFloat(), markerRadius.toFloat(), paintCircle
            )
        }
    }

    fun drawOverlayGraph(canvas: Canvas, size: Size?) {
        canvas.drawRect(
            simuECG!!.point!!.x.toFloat(),
            0f,
            size!!.width.toFloat(),
            size.height.toFloat(),
            paintRectPrev
        )
        canvas.drawPath(simuECG!!.pathBefore!!, paintLine)
        canvas.drawPath(simuECG!!.pathAfter!!, paintLineAfter)
        canvas.drawCircle(
            simuECG!!.point!!.x.toFloat(),
            simuECG!!.point!!.y.toFloat(), markerRadius.toFloat(), paintCircle
        )
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        // Handle tap event
        if (event.action == MotionEvent.ACTION_UP) {
            if (!startStop) start() else stop()
        }
        return true
    }

    fun stop() {
        if (startStop) {
            startStop = false
            obtain.stop()
            simuECG?.stop()
            redrawHandler.removeCallbacks(redrawRunnable)
        }
    }

    fun start() {
        if (!startStop) {
            startStop = true
            obtain.start()
            simuECG?.start()
            redrawHandler.post(redrawRunnable)
        }
    }

    fun clearCanvas() {
        canvas!!.drawColor(Color.WHITE)
        invalidate()
    }

    fun update(counter: Int) {
        currentCounter = counter
        obtain.setState(simuECG!!.drawingFrequency())
        postInvalidate()
    }
}