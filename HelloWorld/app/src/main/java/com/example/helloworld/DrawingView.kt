package com.example.helloworld

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat

class DrawingView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private var paint: Paint = Paint().apply {
        color = Color.BLACK
        style = Paint.Style.STROKE
        strokeWidth = 10f
    }
    private var path = android.graphics.Path()

    // 画像用の変数を追加
    private var bitmap: Bitmap? = null
    private val bitmapPaint = Paint(Paint.DITHER_FLAG)

    // 画像を読み込むメソッド
    fun loadBitmap(bitmap: Bitmap) {
        this.bitmap = bitmap
        invalidate()  // 再描画
    }

    fun changeColor(newColor: Int) {
        paint.color = newColor
        invalidate()  // 色変更時に再描画
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y
        when (event.action) {
            MotionEvent.ACTION_DOWN -> path.moveTo(x, y)
            MotionEvent.ACTION_MOVE -> path.lineTo(x, y)
        }
        invalidate()
        return true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // 画像があれば、Canvasに描画
        bitmap?.let {
            canvas.drawBitmap(it, 0f, 0f, bitmapPaint)
        }

        // パスを描画
        canvas.drawPath(path, paint)
    }
}
