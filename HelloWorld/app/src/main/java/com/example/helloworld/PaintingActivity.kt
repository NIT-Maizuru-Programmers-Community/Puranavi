package com.example.helloworld

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Paint
import android.net.Uri
import android.os.Bundle
import android.os.StrictMode
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.larswerkman.holocolorpicker.ColorPicker
import com.larswerkman.holocolorpicker.OpacityBar
import com.larswerkman.holocolorpicker.SaturationBar
import com.larswerkman.holocolorpicker.SVBar
import java.io.ByteArrayOutputStream
import android.widget.Toast
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.Socket
import java.net.SocketTimeoutException

class PaintingActivity : AppCompatActivity() {
    private lateinit var textView: TextView
    private lateinit var savingsProgressBar: ProgressBar // プログレスバーのプロパティ
    private val serverIP = "172.20.10.13" // ArduinoのIPアドレス
    private val serverPort = 80           // Arduinoのポート番号

    private var accumulatedData = 0 //受け取った数字を累積する変数

    private var currentColor: Int = Color.BLACK
    private lateinit var drawingView: DrawingView
    private lateinit var completeButton: Button

    // Firebase Realtime Database と Firebase Storage の参照を保持
    private lateinit var database: DatabaseReference
    private lateinit var storage: FirebaseStorage
    private lateinit var storageRef: StorageReference

    private var targetAmount: Int = 0 //targetAmountをクラスのプロパティとして宣言

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_painting)

        // Firebase データベースとストレージの初期化
        database = FirebaseDatabase.getInstance().getReference("perfect")
        storage = FirebaseStorage.getInstance()
        storageRef = storage.reference
        textView = findViewById(R.id.money)
        val imageView: ImageView = findViewById(R.id.dotart) // 画像表示用

        //プログレスバーの初期化
        savingsProgressBar = findViewById(R.id.savingsProgressBar)

        drawingView = findViewById(R.id.drawingView)
        completeButton = findViewById(R.id.completeButton)  // 完成ボタンを取得
        val colorPicker = findViewById<ColorPicker>(R.id.color_picker)
        val svBar = findViewById<SVBar>(R.id.svbar)
        val opacityBar = findViewById<OpacityBar>(R.id.opacitybar)
        val saturationBar = findViewById<SaturationBar>(R.id.saturationbar)
        val confirmButton = findViewById<Button>(R.id.confirm_button)
        val imageUri = intent.getStringExtra("imageUri")//前の画面で作った画像を取得

        if (imageUri != null) {
            val bitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(Uri.parse(imageUri)))
            imageView.setImageBitmap(bitmap)
        } else {
            Toast.makeText(this, "画像の読み込みに失敗しました", Toast.LENGTH_SHORT).show()
        }

        targetAmount = intent.getIntExtra("targetAmount",0) //前の画面のtargetAmountを取得
        accumulatedData = 0 //目標金額の初期設定

        // ネットワーク操作を許可する（デモ用の実装）
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        connectToArduino()

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

        // DrawingView のリスナー設定（塗り絵完成時にボタンを表示）
        drawingView.setOnAllTilesFilledListener(object : DrawingView.OnAllTilesFilledListener {
            override fun onAllTilesFilled() {
                // 完成ボタンを表示
                completeButton.visibility = View.VISIBLE
            }
        })

        // 完成ボタンのクリックリスナー設定
        completeButton.setOnClickListener {
            uploadImage()  // 画像を Firebase にアップロード
            completeButton.visibility = View.GONE // ボタンを非表示にする
        }
    }

    // 画像を Firebase Storage にアップロードするメソッド
    private fun uploadImage() {
        // DrawingView の Bitmap を取得
        val bitmap = drawingView.getBitmap() // DrawingView に getBitmap メソッドを追加する必要があります。

        // Bitmap を ByteArray に変換
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val data = byteArrayOutputStream.toByteArray()

        // Firebase Storage に画像をアップロード
        val imageRef = storageRef.child("perfect/${System.currentTimeMillis()}.png") // ファイル名はタイムスタンプを使って一意に
        val uploadTask = imageRef.putBytes(data)

        uploadTask.addOnSuccessListener {
            // アップロード成功時に画像の URL を取得
            imageRef.downloadUrl.addOnSuccessListener { uri:Uri ->
                saveImageUrlToDatabase(uri.toString())
            }
        }.addOnFailureListener { exception:Exception ->
            // エラーハンドリング
            exception.printStackTrace()
        }
    }

    // 画像の URL を Realtime Database に保存するメソッド
    private fun saveImageUrlToDatabase(imageUrl: String) {
        val drawingData = hashMapOf(
            "userId" to "user123",  // ユーザーIDの例
            "completed" to true,
            "imageUrl" to imageUrl // 画像の URL を保存
        )

        // データを "perfect" フォルダにアップロード
        database.push().setValue(drawingData).addOnSuccessListener {
            // アップロード成功時の処理
            // 例: Toast メッセージを表示
        }.addOnFailureListener { exception ->
            // エラーハンドリング
            exception.printStackTrace()
        }
    }

    private fun connectToArduino() {
        Thread {
            try {
                val socket = Socket(serverIP, serverPort)
                socket.soTimeout = 5000
                val reader = BufferedReader(InputStreamReader(socket.getInputStream()))

                runOnUiThread {
                    Toast.makeText(this, "Arduinoに接続しました", Toast.LENGTH_SHORT).show()
                }

                while (true) {
                    try {
                        val data = reader.readLine()?.toIntOrNull() // データを整数に変換
                        if (data != null) {
                            accumulatedData += data // 累積
                            runOnUiThread {
                                // 累積データが目標金額を超えないように調整
                                if (accumulatedData > targetAmount) {
                                    accumulatedData = targetAmount
                                }

                                // プログレスバーの進捗率を計算して設定
                                val progressPercentage = (accumulatedData.toFloat() / targetAmount * 100).toInt().coerceIn(0, 100)
                                val progress = accumulatedData.coerceIn(0, targetAmount)
                                savingsProgressBar.progress = progress

                                // テキストビューに貯金額と進捗率を表示
                                textView.text = "貯金額: $accumulatedData 円 / 目標金額： $targetAmount 円 ($progressPercentage%)"
                            }
                        } else {
                            break
                        }
                    } catch (e: SocketTimeoutException) {
                        break
                    }
                }
                socket.close()
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this, "接続エラー: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }.start()
    }

}
