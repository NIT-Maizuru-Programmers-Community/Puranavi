package com.example.helloworld

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

import android.widget.Toast
import java.io.ByteArrayOutputStream

class DrawingView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private var paint: Paint = Paint().apply {
        color = Color.BLACK
        style = Paint.Style.FILL // 塗りつぶしスタイル
    }

    private var tiles: MutableList<Tile> = mutableListOf() // タイルを保持するリスト
    private var tileSize: Float = 50f // タイルのサイズ
    private var bitmap: Bitmap? = null
    private val bitmapPaint = Paint(Paint.DITHER_FLAG)

    // インターフェースを定義
    interface OnAllTilesFilledListener {
        fun onAllTilesFilled() // タイルがすべて塗り終わった時の処理
    }

    private var listener: OnAllTilesFilledListener? = null

    // リスナーをセットするメソッド
    fun setOnAllTilesFilledListener(listener: OnAllTilesFilledListener) {
        this.listener = listener
    }

    // 画像を読み込むメソッド
    fun loadBitmap(bitmap: Bitmap) {
        this.bitmap = bitmap
        createTiles() // タイルを生成
        invalidate()  // 再描画
    }

    fun changeColor(newColor: Int) {
        paint.color = newColor
        invalidate()  // 色変更時に再描画
    }

    // タイルを生成するメソッド
    private fun createTiles() {
        tiles.clear() // リストをクリア

        // タイルの生成
        bitmap?.let { bmp ->
            val width = bmp.width
            val height = bmp.height

            for (y in 0 until height step tileSize.toInt()) {
                for (x in 0 until width step tileSize.toInt()) {
                    val rect = RectF(
                        x.toFloat(), y.toFloat(),
                        (x + tileSize).coerceAtMost(width.toFloat()),
                        (y + tileSize).coerceAtMost(height.toFloat())
                    )
                    tiles.add(Tile(rect))
                }
            }
        }
    }

    // タイルの塗りつぶし状態を確認し、リスナーを呼び出す
    private fun checkCompletion() {
        if (tiles.all { it.isFilled }) {
            listener?.onAllTilesFilled() // リスナーを呼び出す

            // 塗り絵完了のメッセージを表示
            Toast.makeText(context, "塗り絵が完成しました！", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        if (event.action == MotionEvent.ACTION_DOWN || event.action == MotionEvent.ACTION_MOVE) {
            // タッチした位置にあるタイルを見つける
            tiles.forEach { tile ->
                if (tile.rect.contains(x, y) && !tile.isFilled) { // タイルが塗られていない場合のみ色を変更
                    tile.color = paint.color // 選択した色でタイルを塗りつぶす
                    tile.isFilled = true // タイルが塗られたことを記録
                    invalidate() // 再描画
                    checkCompletion() // 完成状態を確認
                }
            }
        }
        return true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // 画像を描画
        bitmap?.let {
            canvas.drawBitmap(it, 0f, 0f, bitmapPaint)
        }

        // 塗られたタイルを描画
        tiles.forEach { tile ->
            if (tile.isFilled) {
                canvas.drawRect(tile.rect, Paint().apply {
                    color = tile.color // 塗られたタイルの色を使用
                    style = Paint.Style.FILL // 塗りつぶし
                })
            }
            // タイルの枠線を描画
            canvas.drawRect(tile.rect, Paint().apply {
                color = Color.BLACK
                style = Paint.Style.STROKE
                strokeWidth = 2f
            })
        }
    }

    // Bitmap を取得するメソッド
    fun getBitmap(): Bitmap {
        // Bitmap を生成
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        draw(canvas) // 自分自身を描画
        return bitmap
    }
}
