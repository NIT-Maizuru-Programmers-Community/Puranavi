package com.example.helloworld

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.larswerkman.holocolorpicker.ColorPicker
import com.larswerkman.holocolorpicker.OpacityBar
import com.larswerkman.holocolorpicker.SaturationBar
import com.larswerkman.holocolorpicker.SVBar

class PaintingActivity : AppCompatActivity() {

    private var currentColor: Int = Color.BLACK
    private lateinit var drawingView: DrawingView

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_painting)

        drawingView = findViewById(R.id.drawingView)
        val colorPicker = findViewById<ColorPicker>(R.id.color_picker)
        val svBar = findViewById<SVBar>(R.id.svbar)
        val opacityBar = findViewById<OpacityBar>(R.id.opacitybar)
        val saturationBar = findViewById<SaturationBar>(R.id.saturationbar)
        val confirmButton = findViewById<Button>(R.id.confirm_button)

        // カラーピッカーのバーを設定
        colorPicker.addSVBar(svBar)
        colorPicker.addOpacityBar(opacityBar)
        colorPicker.addSaturationBar(saturationBar)

        // 色変更時のリスナー設定
        colorPicker.onColorChangedListener = ColorPicker.OnColorChangedListener {
            currentColor = it
            drawingView.changeColor(currentColor)  // 色を変更する
        }

        // URIを受け取る
        val uriString = intent.getStringExtra(MainActivity.EXTRA_DOT_IMAGE)
        uriString?.let { uri ->
            val inputStream = contentResolver.openInputStream(Uri.parse(uri))
            val selectedBitmap = BitmapFactory.decodeStream(inputStream)
            drawingView.loadBitmap(selectedBitmap) // DrawingViewにBitmapを読み込む
        }

        // 色確定ボタン
        confirmButton.setOnClickListener {
            drawingView.changeColor(currentColor)
        }
    }
}
