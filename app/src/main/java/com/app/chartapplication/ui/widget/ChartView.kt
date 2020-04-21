package com.app.chartapplication.ui.widget

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import java.text.DecimalFormat
import kotlin.math.*



/**
 * Created by Anatoly Ovchinnikov on 2020-04-16.
 */
class ChartView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private val paint = Paint()
    private var axisStep: Float = 0f

    private lateinit var valuesX: FloatArray
    private lateinit var valuesY: FloatArray

    private lateinit var xCoord: FloatArray
    private lateinit var yCoord: FloatArray

    private var x0 = 0f
    private var y0 = 0f

    private var zoomRatio = 1.0f
    private var currentZoomRatio = 1.0f

    private var mLastTouchX: Float = 0f
    private var mLastTouchY: Float = 0f

    private val mPath = Path()

    init {
        this.setOnTouchListener { _, event ->
            if(!mPath.isEmpty) {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        mLastTouchX = event.x
                        mLastTouchY = event.y
                    }

                    MotionEvent.ACTION_MOVE -> {
                        val(mPosX, mPosY) = (event.x - mLastTouchX) to (event.y - mLastTouchY)
                        val matrix = Matrix()
                        matrix.postTranslate(round(mPosX), round(mPosY))

                        mPath.transform(matrix)

                        matrix.mapPoints(xCoord)
                        matrix.mapPoints(yCoord)

                        xCoord[ 0 ] = 0f
                        xCoord[ 2 ] = width.toFloat()
                        yCoord[ 1 ] = 0f
                        yCoord[ 3 ] = height.toFloat()

                        paint.reset()

                        this.invalidate()
                        mLastTouchX = event.x
                        mLastTouchY = event.y
                    }

                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {

                    }
                }
                true
            } else {
                false
            }
        }
    }

    private lateinit var renderFinished: () -> Unit

    fun setReadyListener(renderFinished: () -> Unit) {
        this.renderFinished = renderFinished
    }

    fun setValues(valuesX: FloatArray, valuesY: FloatArray) {
        this.valuesX = valuesX
        this.valuesY = valuesY
        mPath.reset()
        invalidate()
    }

    fun zoomIn() {
        zoomRatio = zoomInValue
        currentZoomRatio *= zoomRatio
        processZoom()
    }

    fun zoomOut() {
        zoomRatio = zoomOutValue
        currentZoomRatio *= zoomRatio
        processZoom()
    }

    var interpolate = false
        set(value) {
            field = value
            mPath.reset()
            this.invalidate()
        }

    private fun processZoom() {
        val matrix = Matrix()
        matrix.setScale(zoomRatio, zoomRatio)
        mPath.transform(matrix)

        matrix.mapPoints(xCoord)
        matrix.mapPoints(yCoord)

        matrix.reset()
        matrix.postTranslate(x0 - yCoord [ 0 ], y0 - xCoord [ 1 ])

        mPath.transform(matrix)

        matrix.mapPoints(xCoord)
        matrix.mapPoints(yCoord)

        xCoord[ 0 ] = 0f
        xCoord[ 2 ] = width.toFloat()
        yCoord[ 1 ] = 0f
        yCoord[ 3 ] = height.toFloat()

        paint.reset()
        this.invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        if(this::valuesX.isInitialized && this::valuesY.isInitialized && valuesX.isNotEmpty() && valuesY.isNotEmpty() && valuesX.size == valuesY.size) {
            val sizeX = width / cellSize
            val sizeY = height / cellSize

            drawAxis(canvas)
            drawGrid(canvas)

            setAxisStep(canvas, sizeX, sizeY)
            drawChart(canvas)
        }
        super.onDraw(canvas)
    }

    private fun drawAxis(canvas: Canvas) {
        if(!this::xCoord.isInitialized && !this::yCoord.isInitialized) {
            xCoord = floatArrayOf(0f, (height / 2).toFloat(), width.toFloat(), (height / 2).toFloat())
            yCoord = floatArrayOf((width / 2).toFloat(), 0f, (width / 2).toFloat(), height.toFloat())
        }

        x0 = yCoord [ 0 ]
        y0 = xCoord [ 1 ]

        paint.color = Color.RED
        paint.strokeWidth = 10f
        // draw x
        canvas.drawLine(xCoord[ 0 ], xCoord[ 1 ], xCoord[ 2 ], xCoord[ 3 ], paint)
        // draw y
        canvas.drawLine(yCoord[ 0 ], yCoord[ 1 ], yCoord[ 2 ], yCoord[ 3 ], paint)
    }

    private fun drawGrid(canvas: Canvas) {
        paint.color = Color.BLACK
        paint.strokeWidth = 2f
        // draw y
        for(i in 0 until height) {
            canvas.drawLine(0f, (y0 + cellSize + cellSize * i), width.toFloat(), (y0 + cellSize + cellSize * i), paint)
            canvas.drawLine(0f, (y0- ( cellSize + (cellSize * i) )), width.toFloat(), (y0 - ( cellSize + (cellSize * i) )), paint)
        }

        // draw x
        for(i in 0 until width) {
            canvas.drawLine((x0 + cellSize + (cellSize * i)), 0f, (x0 + cellSize + (cellSize * i)), height.toFloat(), paint)
            canvas.drawLine((x0 - (cellSize + (cellSize * i)) ), 0f, (x0 - (cellSize + (cellSize * i)) ), height.toFloat(), paint)
        }
    }

    private fun setAxisStep(canvas: Canvas, sizeX: Int, sizeY: Int) {
        val maxAbsX = max(abs(valuesX.max()!!), abs(valuesX.min()!!))

        val maxAbsY = max(abs(valuesY.max()!!), abs(valuesY.min()!!))

        val maxAbsValue = max(maxAbsX, maxAbsY)

        val minAxisCellSize = min(sizeX, sizeY)

        axisStep = ceil(maxAbsValue / (minAxisCellSize / 2)) / currentZoomRatio

        paint.color = Color.RED
        paint.textSize = 40f
        paint.textAlign = Paint.Align.RIGHT

        canvas.drawText("0", (0f.toX() - 10), (0f.toY() + 40).toFloat(), paint)

        // ==== Draw X axis ====
        // Calculate all X axis values
        var counter = 0
        var coordX = x0
        val list = mutableListOf<Float>()

        while (0 < coordX) {
            val value = -(axisStep + axisStep * counter)
            coordX = value.toX()
            list.add(coordX)
            list.add(0f.toY())
            counter++
        }

        counter = 0
        coordX = x0
        while (coordX <= width) {
            val value = axisStep + axisStep * counter
            coordX = value.toX()
            list.add(coordX)
            list.add(0f.toY())

            counter++
        }

        val xLabelCoord = list.toFloatArray()

        // ==== ...and draw its ====
        for(i in 0 until xLabelCoord.size step 2) {
            canvas.drawText(DecimalFormat("#.##").format(xLabelCoord[ i ].fromX()), xLabelCoord[ i ] + 10, xLabelCoord[ i + 1 ] + 40, paint)
        }
        // ==== end ====


        // ==== Draw Y axis ====
        // Calculate all Y axis values

        counter = 0
        var coordY = y0
        list.clear()

        while (height > coordY) {
            val value = -(axisStep + axisStep * counter)
            coordY = value.toY()
            list.add(0f.toX())
            list.add(coordY)
            counter++
        }

        counter = 0
        coordY = y0
        while (coordY > 0) {
            val value = axisStep + axisStep * counter
            coordY = value.toY()
            list.add(0f.toX())
            list.add(coordY)

            counter++
        }

        val yLabelCoord = list.toFloatArray()

        // ==== ...and draw its ====
        for(i in 0 until yLabelCoord.size step 2) {
            canvas.drawText(DecimalFormat("#.##").format(yLabelCoord[ i + 1 ].fromY()), yLabelCoord[ i ] - 30f, yLabelCoord[ i + 1 ] + 10, paint)
        }
        // ==== end ====
    }

    private fun drawChart(canvas: Canvas) {
        paint.color = Color.BLUE
        paint.strokeWidth = 10f
        paint.style = Paint.Style.STROKE

        if(mPath.isEmpty) {
            val screenValuesX = FloatArray(valuesX.size)
            val screenValuesY = FloatArray(valuesY.size)
            for(i in valuesX.indices) {
                screenValuesX[ i ] = valuesX[ i ].toX()
                screenValuesY[ i ] = valuesY[ i ].toY()
            }
            if(interpolate) {
                renderInterpolateChart(screenValuesX, screenValuesY)
            } else {
                renderLinearChart(screenValuesX, screenValuesY)
            }
        }

        canvas.drawPath(mPath, paint)
        paint.style = Paint.Style.FILL
        renderFinished()
    }

    private fun renderLinearChart(screenValuesX: FloatArray, screenValuesY: FloatArray) {
        mPath.moveTo(screenValuesX[ 0 ], screenValuesY[ 0 ])
//        mPath.addCircle(screenValuesX[ 0 ], screenValuesY[ 0 ], 8f, Path.Direction.CW)
        for(i in 1 until screenValuesX.size) {
            mPath.lineTo(screenValuesX[ i ], screenValuesY[ i ])
//            mPath.addCircle(screenValuesX[ i ], screenValuesY[ i ], 8f, Path.Direction.CW)
        }
    }

    private fun renderInterpolateChart(screenValuesX: FloatArray, screenValuesY: FloatArray) {
        for(i in 1 until screenValuesX.size) {
            val pts = floatArrayOf(screenValuesX[ i - 1 ], screenValuesY[ i - 1 ], screenValuesX[ i ], screenValuesY[ i ])
            val conX1 = (pts[ 0 ] + pts[ 2 ]) / 2
            val conY1 = pts[ 1 ]
            val conX2 = (pts[ 0 ] + pts[ 2 ]) / 2
            val conY2 = pts[ 3 ]
            mPath.moveTo(pts[ 0 ], pts[ 1 ])
            paint.isAntiAlias = true
            mPath.cubicTo(conX1, conY1, conX2, conY2, pts[ 2 ], pts[ 3 ])
        }
    }

    fun getBitmap(): Bitmap {
        //this.measure(100, 100);
        //this.layout(0, 0, 100, 100);
        this.isDrawingCacheEnabled = true
        this.buildDrawingCache()
        val bmp = Bitmap.createBitmap(this.drawingCache)
        this.isDrawingCacheEnabled = false

        return bmp
    }

    private fun Float.toX() = x0 + this * cellSize / axisStep

    private fun Float.toY() = y0 - this * cellSize / axisStep

    private fun Float.fromX() = (this - x0) * axisStep / cellSize

    private fun Float.fromY()= (y0 - this) * axisStep / cellSize

    companion object {
        private const val cellSize = 90
        private const val zoomInValue = 2.0f
        private const val zoomOutValue = 0.5f
    }
}