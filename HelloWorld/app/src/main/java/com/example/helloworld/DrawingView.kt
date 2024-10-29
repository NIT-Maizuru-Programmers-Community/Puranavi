package com.example.helloworld

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class DrawingView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private var paint = Paint()
    private var path = android.graphics.Path()
    private var bitmap: Bitmap? = null
    private var canvas: Canvas? = null
    private var tilesFilledCount = 0
    private var totalTilesCount = 10 // タイルの総数を適切に設定
    private var onAllTilesFilledListener: OnAllTilesFilledListener? = null

    init {
        paint.isAntiAlias = true
        paint.color = Color.BLACK
        paint.style = Paint.Style.STROKE
        paint.strokeJoin = Paint.Join.ROUND
        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeWidth = 10f
    }

    override fun onSizeChanged(width: Int, height: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(width, height, oldw, oldh)
        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        canvas = Canvas(bitmap!!)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawBitmap(bitmap!!, 0f, 0f, null)
        canvas.drawPath(path, paint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val touchX = event.x
        val touchY = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                path.moveTo(touchX, touchY)
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                path.lineTo(touchX, touchY)
            }
            MotionEvent.ACTION_UP -> {
                canvas?.drawPath(path, paint)
                path.reset()
                invalidate()

                // タイルが埋まったかの確認ロジックを追加
                tilesFilledCount++
                checkAllTilesFilled()
            }
        }
        return true
    }

    // タイルがすべて埋まったかをチェックするメソッド
    private fun checkAllTilesFilled() {
        if (tilesFilledCount >= totalTilesCount) {
            onAllTilesFilledListener?.onAllTilesFilled()
        }
    }

    fun loadBitmap(newBitmap: Bitmap) {
        bitmap = newBitmap.copy(Bitmap.Config.ARGB_8888, true)
        canvas = Canvas(bitmap!!)
        invalidate()
    }

    fun changeColor(color: Int) {
        paint.color = color
    }

    fun getBitmap(): Bitmap {
        return bitmap!!
    }

    fun setOnAllTilesFilledListener(listener: OnAllTilesFilledListener) {
        onAllTilesFilledListener = listener
    }

    interface OnAllTilesFilledListener {
        fun onAllTilesFilled()
    }
}
