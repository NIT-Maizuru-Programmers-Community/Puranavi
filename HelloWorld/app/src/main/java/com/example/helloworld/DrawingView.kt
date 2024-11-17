package com.example.helloworld

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.util.Log
import android.widget.Toast

class DrawingView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private var paint: Paint = Paint().apply {
        color = Color.BLACK
        style = Paint.Style.FILL
    }

    private var tilePaint: Paint = Paint().apply { // タイル塗りつぶし用
        style = Paint.Style.FILL
    }

    private var tileBorderPaint: Paint = Paint().apply { // タイル枠線用
        color = Color.BLACK
        style = Paint.Style.STROKE
        strokeWidth = 2f
    }

    private var tiles: MutableList<Tile> = mutableListOf() // タイルを保持するリスト
    var targetAmount:Int = 0
    private var bitmap: Bitmap? = null
    private val bitmapPaint = Paint(Paint.DITHER_FLAG)

    var isBitmapLoaded: Boolean = false // ビットマップが読み込まれたかのフラグ
    private var isImageVisible: Boolean = true // 画像を表示するかどうかのフラグ
    private var showTileBorders: Boolean = true //枠線を表示するか否か

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
    fun loadBitmap(bitmap: Bitmap,targetAmount:Int) {
        this.bitmap = bitmap
        this.targetAmount = targetAmount
        isBitmapLoaded = true // ビットマップを読み込んだらフラグを更新
        Log.d("DrawingView", "ビットマップが読み込まれました: 幅=${bitmap.width}, 高さ=${bitmap.height}")
        createTiles() // タイルを生成
        invalidate()  // 再描画
    }

    fun changeColor(newColor: Int) {
        paint.color = newColor
        invalidate()  // 色変更時に再描画
    }

    // タイルを生成するメソッド
    private fun createTiles() {
        tiles.clear()
        bitmap?.let { bmp ->
            Log.d("DrawingView", "Bitmapのサイズ: 幅=${bmp.width}, 高さ=${bmp.height}")
            val width = bmp.width
            val height = bmp.height

            // 一辺のタイルの数からタイルのサイズを計算
            var tileCountPerSide = when{
                targetAmount in 1..3000 -> Constants.TILE_SIZE_C
                targetAmount in 3001..6000 -> Constants.TILE_SIZE_UC
                targetAmount in 6001..15000 -> Constants.TILE_SIZE_R
                targetAmount in 15001..30000 -> Constants.TILE_SIZE_SR
                targetAmount in 30001..50000 -> Constants.TILE_SIZE_UR
                else-> 0
            }
            val tileSize = minOf(width, height) / tileCountPerSide.toFloat()

            val columns = (width / tileSize).toInt()
            val rows = (height / tileSize).toInt()

            for (y in 0 until rows) {
                for (x in 0 until columns) {
                    val left = x * tileSize
                    val top = y * tileSize
                    val right = left + tileSize
                    val bottom = top + tileSize

                    val rectF = RectF(left, top, right, bottom)
                    tiles.add(Tile(rectF))
                }
            }
            Log.d("DrawingView", "生成されたタイルの数: ${tiles.size}")
        } ?: Log.d("DrawingView", "Bitmapがnullです")
    }

    // タイルの塗りつぶし状態を確認し、リスナーを呼び出す
    private fun checkCompletion() {
        if (tiles.all { it.isFilled }) {
            listener?.onAllTilesFilled() // リスナーを呼び出す
            // 塗り絵完了のメッセージを表示
            Toast.makeText(context, "塗り絵が完成しました！", Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isBitmapLoaded) {
            Log.d("DrawingView", "タッチイベントを受け取ったが、ビットマップが読み込まれていません")
            return false // ビットマップが読み込まれていない場合は処理を中断
        }

        Log.d("DrawingView", "タッチイベントを受け取った: (${event.x}, ${event.y})")
        val x = event.x
        val y = event.y

        if (event.action == MotionEvent.ACTION_DOWN || event.action == MotionEvent.ACTION_MOVE) {
            // タッチした位置にあるタイルを見つける
            tiles.forEach { tile ->
                if (tile.rect.contains(x, y) && !tile.isFilled) {
                    tile.color = paint.color // 選択した色でタイルを塗りつぶす
                    tile.isFilled = true // タイルが塗られたことを記録
                    Log.d("DrawingView", "タイルの位置: (${tile.rect.left}, ${tile.rect.top}) 塗りつぶし色: ${tile.color}")
                    invalidate() // 再描画
                    checkCompletion() // 完成状態を確認
                }
            }
        }
        return true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // 2. 画像を描画（表示フラグに基づいて）
        if (isImageVisible) {
            bitmap?.let {
                canvas.drawBitmap(it, 0f, 0f, bitmapPaint)
            } ?: Log.d("DrawingView", "Bitmapがnullのため描画しません")
        }

        // 塗られたタイルを描画
        tiles.forEach { tile ->
            // 塗られたタイルを描画
            if (tile.isFilled) {
                tilePaint.color = tile.color
                canvas.drawRect(tile.rect, tilePaint)
            }
            // タイルの枠線を描画
            if(showTileBorders){
                canvas.drawRect(tile.rect, tileBorderPaint)
            }
        }
    }

    // 枠線を表示・非表示するか否かの処理
    fun toggleTileBorders() {
        showTileBorders = !showTileBorders
        invalidate() // 再描画
    }

    fun getBitmap(): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        draw(canvas) // 自身を描画
        return bitmap
    }

    fun areAllTilesFilled():Boolean{
        return tiles.all{it.isFilled}
    }
}
