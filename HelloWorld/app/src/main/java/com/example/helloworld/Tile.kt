package com.example.helloworld

import android.graphics.RectF
import android.graphics.Color

data class Tile(
    val rect: RectF, // タイルの位置とサイズ
    var color: Int = Color.WHITE, // 塗りつぶしの色
    var isFilled: Boolean = false // 塗られたかどうか
)
