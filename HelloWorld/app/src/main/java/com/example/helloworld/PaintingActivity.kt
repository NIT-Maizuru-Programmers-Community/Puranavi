package com.example.helloworld

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Paint
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
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
import java.io.File

class PaintingActivity : AppCompatActivity() {

    private var currentColor: Int = Color.BLACK
    private lateinit var drawingView: DrawingView
    private lateinit var completeButton: Button

    // Firebase Realtime Database と Firebase Storage の参照を保持
    private lateinit var database: DatabaseReference
    private lateinit var storage: FirebaseStorage
    private lateinit var storageRef: StorageReference

    private var paint: Paint = Paint().apply {
        color = Color.BLACK
        style = Paint.Style.FILL // 塗りつぶしスタイル
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_painting)

        // Firebase データベースとストレージの初期化
        database = FirebaseDatabase.getInstance().getReference("perfect")
        storage = FirebaseStorage.getInstance()
        storageRef = storage.reference

        drawingView = findViewById(R.id.drawingView)
        completeButton = findViewById(R.id.completeButton)  // 完成ボタンを取得
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
            imageRef.downloadUrl.addOnSuccessListener { uri ->
                saveImageUrlToDatabase(uri.toString())
            }
        }.addOnFailureListener { exception ->
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
