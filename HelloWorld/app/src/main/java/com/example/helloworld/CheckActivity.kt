package com.example.helloworld

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import java.io.FileNotFoundException

class CheckActivity : ComponentActivity() {

    private val REQUEST_WRITE_STORAGE = 112

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_check)

        // ストレージの書き込み権限を確認
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_WRITE_STORAGE)
        }

        // OpenCVの初期化
        if (OpenCVLoader.initDebug()) {
            val openCVVersion = org.opencv.core.Core.VERSION
            Toast.makeText(this, "OpenCVVersion:$openCVVersion", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "OpenCVの読み込みに失敗しました", Toast.LENGTH_SHORT).show()
        }

        // 画像を表示するImageViewを取得
        val imageView: ImageView = findViewById(R.id.imageView)

        // インテントから画像URIを取得
        val imageUri = intent.getStringExtra("imageUri")
        if (imageUri != null) {
            try {
                // URIからビットマップを読み込む
                val bitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(Uri.parse(imageUri)))

                // OpenCV処理の適用
                val processedBitmap = processImage(bitmap)
                imageView.setImageBitmap(processedBitmap)

            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            }
        } else {
            Toast.makeText(this, "画像の読み込みに失敗しました", Toast.LENGTH_SHORT).show()
        }

        // btnOKボタンを取得
        val btnOK: Button = findViewById(R.id.btnOK)
        btnOK.setOnClickListener {
            // Paintingアクティビティに遷移
            val intent = Intent(this, PaintingActivity::class.java)
            startActivity(intent)
        }

        val btnNO:Button = findViewById(R.id.btnNO)
        btnNO.setOnClickListener{
            finish()
        }
    }

    private fun processImage(bitmap: Bitmap): Bitmap {
        val originalMat = Mat()
        Utils.bitmapToMat(bitmap, originalMat)

        // 固定サイズにリサイズ
        val fixedSize = Size(1024.0, 1024.0)
        val resizedMat = Mat()
        Imgproc.resize(originalMat, resizedMat, fixedSize)

        // グレースケールに変換
        val grayMat = Mat()
        Imgproc.cvtColor(resizedMat, grayMat, Imgproc.COLOR_BGR2GRAY)

        // ピクセルアート化とCannyエッジ検出の処理
        val pixelSize = 64
        val gridSize = 32
        val smallImage = Mat()
        Imgproc.resize(grayMat, smallImage, Size(pixelSize.toDouble(), pixelSize.toDouble()), 0.0, 0.0, Imgproc.INTER_NEAREST)
        val pixelArtMat = Mat()
        Imgproc.resize(smallImage, pixelArtMat, fixedSize, 0.0, 0.0, Imgproc.INTER_NEAREST)

        val edgesMat = Mat()
        Imgproc.Canny(pixelArtMat, edgesMat, 50.0, 70.0)

        val edgesColorMat = Mat(edgesMat.size(), CvType.CV_8UC3)
        Imgproc.cvtColor(edgesMat, edgesColorMat, Imgproc.COLOR_GRAY2BGR)
        for (row in 0 until edgesColorMat.rows()) {
            for (col in 0 until edgesColorMat.cols()) {
                val pixel = edgesColorMat.get(row, col)
                if (pixel[0] == 255.0) {
                    edgesColorMat.put(row, col, byteArrayOf(0, 0, 255.toByte()))
                }
            }
        }

        val invertedMat = Mat()
        Core.bitwise_not(edgesMat, invertedMat)
        val binaryMat = Mat()
        Imgproc.threshold(invertedMat, binaryMat, 200.0, 255.0, Imgproc.THRESH_BINARY)
        Imgproc.resize(binaryMat, binaryMat, edgesColorMat.size())

        val binaryColorMat = Mat()
        Imgproc.cvtColor(binaryMat, binaryColorMat, Imgproc.COLOR_GRAY2BGR)
        val resultMat = Mat()
        Core.add(edgesColorMat, binaryColorMat, resultMat)

        val kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, Size(10.0, 10.0))
        Imgproc.dilate(edgesColorMat, edgesColorMat, kernel)

        val gridThickness = 1
        for (i in 0 until resultMat.rows()) {
            for (j in 0 until resultMat.cols()) {
                if (i % gridSize <= gridThickness || j % gridSize <= gridThickness) {
                    resultMat.put(i, j, byteArrayOf(0, 0, 0))
                }
            }
        }

        val resultBitmap = Bitmap.createBitmap(resultMat.cols(), resultMat.rows(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(resultMat, resultBitmap)

        return resultBitmap
    }
}
