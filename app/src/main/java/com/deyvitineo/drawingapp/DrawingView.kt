package com.deyvitineo.drawingapp

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View


class DrawingView(context: Context, attrs: AttributeSet) : View(context, attrs) {


    private var mDrawPath: CustomPath? = null
    private var mCanvasBitmap: Bitmap? = null
    private var mDrawPaint: Paint? = null
    private var mCanvasPaint: Paint? = null
    private var mBrushSize: Float = 20.toFloat()
    private var mColor = Color.BLACK
    private var mCanvas: Canvas? = null
    private val mPaths = ArrayList<CustomPath>()
    private val mRedoPaths = ArrayList<CustomPath>()

    init {
        setupDrawing()
    }

    private fun setupDrawing() {
        mDrawPaint = Paint()
        mDrawPath = CustomPath(mColor, mBrushSize)
        mDrawPaint!!.color = mColor
        mDrawPaint!!.style = Paint.Style.STROKE
        mDrawPaint!!.strokeJoin = Paint.Join.ROUND
        mDrawPaint!!.strokeCap = Paint.Cap.ROUND
        mCanvasPaint = Paint(Paint.DITHER_FLAG)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mCanvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        mCanvas = Canvas(mCanvasBitmap!!)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.drawBitmap(mCanvasBitmap!!, 0f, 0f, mCanvasPaint)

        for (path in mPaths) {
            mDrawPaint!!.strokeWidth = path.brushThickness
            mDrawPaint!!.color = path.color
            canvas?.drawPath(path, mDrawPaint!!)
        }

        if (!mDrawPath!!.isEmpty) {
            mDrawPaint!!.strokeWidth = mDrawPath!!.brushThickness
            mDrawPaint!!.color = mDrawPath!!.color
            canvas?.drawPath(mDrawPath!!, mDrawPaint!!)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val touchX = event?.x
        val touchY = event?.y

        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                mDrawPath!!.color = mColor
                mDrawPath!!.brushThickness = mBrushSize

                mDrawPath!!.reset()
                if (touchX != null && touchY != null) {
                    mDrawPath!!.moveTo(touchX, touchY)
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (touchX != null && touchY != null) {
                    mDrawPath!!.lineTo(touchX, touchY)
                }
            }
            MotionEvent.ACTION_UP -> {
                if (mRedoPaths.size > 0) { //if something is drawn after undoing, the redo array is being cleared
                    mRedoPaths.clear()
                }
                mPaths.add(mDrawPath!!)
                mDrawPath = CustomPath(mColor, mBrushSize)
            }
            else -> return false
        }
        invalidate()
        return true
    }

    /**
     * Sets new Brush size
     * @param newSize -> brush's new size
     */
    fun setBrushSize(newSize: Float) {
        mBrushSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, newSize, resources.displayMetrics)
        mDrawPaint!!.strokeWidth = mBrushSize
    }

    /**
     * Sets new brush color
     * @param newColor -> String for the new color (it is converted from hexadecimal to Color)
     */
    fun setColor(newColor: String) {
        mColor = Color.parseColor(newColor)
        mDrawPaint!!.color = mColor
    }

    /**
     * It removes the last drawing available from the mPaths array. If a path can be undone, it will return true, false otherwise. It also adds
     * the removed path to the mRedoPaths array which holds all the paths that can be redone
     */
    fun undoPath(): Boolean {
        return if (mPaths.size > 0) {
            mRedoPaths.add(mPaths.removeAt(mPaths.size - 1))
            invalidate() //calls on draw for paths to be drawn again
            true
        } else {
            false
        }
    }

    /**
     * It redoes a path if one is available. The mRedoPaths array is cleared whenever the user draws something
     */
    fun redoPath(): Boolean {
        return if (mRedoPaths.size > 0) {
            mPaths.add(mRedoPaths.removeAt(mRedoPaths.size - 1))
            invalidate()
            true
        } else {
            false
        }
    }


    /**
     * Custom path class used to store the color and brush thickness of every path
     * @param color -> holds the color value
     * @param brushThickness -> holds the thickness of the brush
     */
    internal inner class CustomPath(var color: Int, var brushThickness: Float) : Path() {

    }
}