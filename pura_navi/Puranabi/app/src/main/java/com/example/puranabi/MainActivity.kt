package com.example.puranabi

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import org.opencv.core.Core
import org.opencv.core.CvType
import java.io.IOException

class MainActivity : ComponentActivity() {
    private val REQUEST_WRITE_STORAGE = 112

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // ストレージの書き込み権限を確認
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_WRITE_STORAGE)
        }

        // OpenCVの初期化
        if (OpenCVLoader.initDebug()) {
            val openCVVersion = org.opencv.core.Core.VERSION
            val textView: TextView = findViewById(R.id.text1)
            textView.text = "OpenCV Version: $openCVVersion"
        } else {
            val textView: TextView = findViewById(R.id.text1)
            textView.text = "Failed to load OpenCV"
        }

        // 画像を表示するImageViewを取得
        val imageView: ImageView = findViewById(R.id.imageView)

        try {
            // 画像をリソースからBitmapとして読み込む
            val bitmap = BitmapFactory.decodeResource(resources, R.drawable.regiscan)

            // BitmapをMatに変換
            val originalMat = Mat()
            Utils.bitmapToMat(bitmap, originalMat)

            // 画像を固定サイズにリサイズ
            val fixedSize = Size(2048.0, 2048.0)
            val resizedMat = Mat()
            Imgproc.resize(originalMat, resizedMat, fixedSize)

            // BGRからグレースケールに変換
            val grayMat = Mat()
            Imgproc.cvtColor(resizedMat, grayMat, Imgproc.COLOR_BGR2GRAY)

            // pixelSizeを指定（2のn乗）
            val pixelSize = 64 // 例: pixelSizeを64に設定

            // gridSizeを指定(2のn乗)
            val gridSize = 32

            // ドット絵風に画像を縮小（ピクセルアート化）
            val smallImage = Mat()
            Imgproc.resize(grayMat, smallImage, Size(pixelSize.toDouble(), pixelSize.toDouble()), 0.0, 0.0, Imgproc.INTER_NEAREST)

            // 再び固定サイズに拡大
            val pixelArtMat = Mat()
            Imgproc.resize(smallImage, pixelArtMat, fixedSize, 0.0, 0.0, Imgproc.INTER_NEAREST)

            // Cannyエッジ検出 (しきい値を少し上げる)
            val edgesMat = Mat()
            Imgproc.Canny(pixelArtMat, edgesMat, 50.0, 70.0)

            // エッジを赤色にするために3チャンネルの画像に変換
            val edgesColorMat = Mat(edgesMat.size(), CvType.CV_8UC3)  // CV_8UC3: 8-bit unsigned, 3 channels (BGR)
            Imgproc.cvtColor(edgesMat, edgesColorMat, Imgproc.COLOR_GRAY2BGR)

            // エッジの部分を赤色に設定（BGRフォーマットで赤は(0, 0, 255)）
            for (row in 0 until edgesColorMat.rows()) {
                for (col in 0 until edgesColorMat.cols()) {
                    val pixel = edgesColorMat.get(row, col)
                    if (pixel[0] == 255.0) {  // 白い部分を赤に変更
                        edgesColorMat.put(row, col, byteArrayOf(0, 0, 255.toByte()))
                    }
                }
            }

            // 境目の部分を黒に設定するために、エッジ画像を反転
            val invertedMat = Mat()
            Core.bitwise_not(edgesMat, invertedMat)

            // 二値化して境目を黒、背景を白にする
            val binaryMat = Mat()
            Imgproc.threshold(invertedMat, binaryMat, 200.0, 255.0, Imgproc.THRESH_BINARY)

            // binaryMatのサイズをedgesColorMatに合わせる
            Imgproc.resize(binaryMat, binaryMat, edgesColorMat.size())

            // binaryMatを3チャンネルに変換（グレースケールからBGRへ）
            val binaryColorMat = Mat()
            Imgproc.cvtColor(binaryMat, binaryColorMat, Imgproc.COLOR_GRAY2BGR)

            // 二値化された画像の境目（黒部分）をエッジ画像と合成
            val resultMat = Mat()
            Core.add(edgesColorMat, binaryColorMat, resultMat)

            // エッジの線を太くするために膨張処理を行う (カーネルサイズを小さく調整)
            val kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, Size(10.0, 10.0))  // 小さなカーネル
            Imgproc.dilate(edgesColorMat, edgesColorMat, kernel)

            // ドットの境目に黒のグリッドを引く (太さを調整)
            val gridThickness = 1
            for (i in 0 until resultMat.rows()) {
                for (j in 0 until resultMat.cols()) {
                    // 行の境界線を引く
                    if (i % gridSize <= gridThickness) {
                        resultMat.put(i, j, byteArrayOf(0, 0, 0))  // 黒
                    }
                    // 列の境界線を引く
                    if (j % gridSize <= gridThickness) {
                        resultMat.put(i, j, byteArrayOf(0, 0, 0))  // 黒
                    }
                }
            }

            // 結果をBitmapに変換
            val resultBitmap = Bitmap.createBitmap(resultMat.cols(), resultMat.rows(), Bitmap.Config.ARGB_8888)
            Utils.matToBitmap(resultMat, resultBitmap)

            // ImageViewに表示
            imageView.setImageBitmap(resultBitmap)

        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}