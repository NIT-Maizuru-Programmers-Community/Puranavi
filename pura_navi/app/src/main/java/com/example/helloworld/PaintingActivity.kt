package com.example.helloworld

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.larswerkman.holocolorpicker.ColorPicker
import com.larswerkman.holocolorpicker.OpacityBar
import com.larswerkman.holocolorpicker.SaturationBar
import com.larswerkman.holocolorpicker.SVBar
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket
import android.content.Intent
import com.bumptech.glide.load.engine.DiskCacheStrategy

class PaintingActivity : AppCompatActivity() {
    private lateinit var textView: TextView
    private lateinit var savingsProgressBar: ProgressBar // プログレスバーのプロパティ
    private val serverIP = "172.20.10.13" // ArduinoのIPアドレス
    private val serverPort = 80           // Arduinoのポート番号
    private lateinit var socket: Socket
    private lateinit var output: PrintWriter
    private lateinit var input: BufferedReader

    private var accumulatedData = 0 //受け取った数字を累積する変数
    private var currentColor: Int = Color.BLACK
    private lateinit var drawingView: DrawingView
    private lateinit var completeButton: Button
    private lateinit var deathbutton: Button

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
        deathbutton = findViewById(R.id.btndeath)
        completeButton = findViewById(R.id.completebutton)  // 完成ボタンを取得

        completeButton.visibility = View.GONE // 完成ボタンを初期状態で非表示にする

        val colorPicker = findViewById<ColorPicker>(R.id.color_picker)
        val svBar = findViewById<SVBar>(R.id.svbar)
        val opacityBar = findViewById<OpacityBar>(R.id.opacitybar)
        val saturationBar = findViewById<SaturationBar>(R.id.saturationbar)
        val confirmButton = findViewById<Button>(R.id.confirm_button)
        val imageUri = intent.getStringExtra("imageUri") //前の画面で作った画像を取得

        drawingView.bringToFront() //DrawingViewを最前列に!!

        if (imageUri != null) {
            Glide.with(this)
                .asBitmap()
                .load(Uri.parse(imageUri))
                .skipMemoryCache(true) // メモリキャッシュをスキップ
                .diskCacheStrategy(DiskCacheStrategy.NONE) // ディスクキャッシュもスキップ
                .into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                        drawingView.loadBitmap(resource,targetAmount) // 最新画像をDrawingViewに読み込む
                    }
                    override fun onLoadCleared(placeholder: Drawable?) {
                        // 必要に応じてリソースを解放する処理
                    }
                })

        } else {
            Toast.makeText(this, "画像の読み込みに失敗しました", Toast.LENGTH_SHORT).show()
        }


        targetAmount = intent.getIntExtra("targetAmount", 0) //前の画面のtargetAmountを取得
        accumulatedData = 0 //目標金額の初期設定

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

        // 色確定ボタン
        confirmButton.setOnClickListener {
            drawingView.changeColor(currentColor)
        }

        // DrawingView のリスナー設定（塗り絵完成時にボタンを表示）
        drawingView.setOnAllTilesFilledListener(object : DrawingView.OnAllTilesFilledListener {
            override fun onAllTilesFilled() {
                runOnUiThread {
                    completeButton.visibility = View.VISIBLE // すべてのタイルが塗り終わった時にボタンを表示
                }
            }
        })

        // 完成ボタンのクリックリスナー設定
        completeButton.setOnClickListener {
            if (!drawingView.isBitmapLoaded) {
                Log.d("PaintingActivity", "タッチイベントを受け取ったが、ビットマップが読み込まれていません")
                return@setOnClickListener // 修正：リスナーの戻り値を正しく扱う
            }

            uploadImage()
            completeButton.visibility = View.GONE // ボタンを非表示にする

            val bitmap = drawingView.getBitmap() // Bitmapを取得
            // BitmapをByteArrayに変換
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            val byteArray = stream.toByteArray()

            // Intentで次のアクティビティに渡す
            val intent = Intent(this, Result::class.java)
            intent.putExtra("bitmap", byteArray)
            startActivity(intent)
        }

        deathbutton.setOnClickListener {
            drawingView.toggleTileBorders() // 画像の表示/非表示を切り替える
        }
    }

    private fun connectToArduino() {
        Thread {
            try {
                // サーバーに接続
                socket = Socket(serverIP, serverPort)
                output = PrintWriter(socket.getOutputStream(), true)
                input = BufferedReader(InputStreamReader(socket.getInputStream()))

                while (true) {
                    // Arduinoからデータを受信
                    val data = input.readLine()?.toIntOrNull()
                    data?.let {
                        accumulatedData += it
                        runOnUiThread {
                            if (accumulatedData > targetAmount) {
                                accumulatedData = targetAmount
                            }
                            val progressPercentage = (accumulatedData.toFloat() / targetAmount * 100).toInt().coerceIn(0, 100)
                            val progress = accumulatedData.coerceIn(0, targetAmount)
                            savingsProgressBar.progress = progress
                            textView.text = "貯金額: $accumulatedData 円 / 目標金額： $targetAmount 円 ($progressPercentage%)"
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this, "接続エラー: ${e.message}", Toast.LENGTH_LONG).show()
                }
            } finally {
                // 接続をクリーンアップ
                try {
                    output.close()
                    input.close()
                    socket.close()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }.start()
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
            imageRef.downloadUrl.addOnSuccessListener { uri: Uri ->
                saveImageUrlToDatabase(uri.toString())
            }
        }.addOnFailureListener { exception: Exception ->
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
}
