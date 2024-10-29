package com.example.helloworld

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.view.MotionEvent
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso
import java.io.File
import java.io.FileOutputStream
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import android.graphics.Color
import android.widget.EditText
import android.widget.TextView
import java.io.File
import java.io.FileOutputStream
import com.squareup.picasso.Picasso


class MainActivity : ComponentActivity() {
    private lateinit var imageView: ImageView
    private lateinit var selectImageBtn: Button
    private lateinit var postIdInput: EditText
    private lateinit var targetAmountTextView: TextView
    private lateinit var databaseReference: DatabaseReference


    private lateinit var tiles: List<Tile>
    private var originalBitmap: Bitmap? = null
    private var selectedColor: Int = Color.RED // デフォルトの選択色
    private lateinit var paint: Paint // Paintオブジェクトを追加

    companion object {
        const val REQUEST_IMAGE_PICK = 1
        const val EXTRA_DOT_IMAGE = "DOT_IMAGE"
    }

    private lateinit var database: DatabaseReference
    private var targetSavings: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Paintオブジェクトの初期化
        paint = Paint().apply {
            style = Paint.Style.FILL // 塗りつぶしスタイルを設定
        }

        // Firebaseから目標金額を取得
        database = FirebaseDatabase.getInstance().getReference("savingsGoal")

        val dotButton = findViewById<Button>(R.id.dotButton)

        val paintButton = findViewById<Button>(R.id.paintButton)

        // UI要素の初期化
        imageView = findViewById(R.id.imageView)  // 画像を表示するImageView
        selectImageBtn = findViewById(R.id.selectImageBtn)  // 画像を選択するボタン
        postIdInput = findViewById(R.id.postIdInput)  // postIdを入力するEditText
        targetAmountTextView = findViewById(R.id.targetAmountTextView)  // 目標金額を表示するTextView

        // Firebase Realtime Databaseの参照を取得
        databaseReference = FirebaseDatabase.getInstance().getReference("posts")

        // 画像と目標金額を取得するためのボタンのクリックリスナー
        selectImageBtn.setOnClickListener {
            val postId = postIdInput.text.toString().trim()  // ユーザーが入力したPost IDを取得

            if (postId.isNotEmpty()) {
                // postIdを元にFirebase Realtime Databaseからデータを取得
                databaseReference.child(postId).get().addOnSuccessListener { snapshot ->
                    if (snapshot.exists()) {
                        // 画像URLと目標金額を取得
                        val imageUrl = snapshot.child("imageUrl").getValue(String::class.java)
                        val targetAmount = snapshot.child("targetAmount").getValue(Int::class.java)

                        // 画像URLが存在する場合、Picassoで画像をImageViewに表示
                        if (imageUrl != null) {
                            Picasso.get().load(imageUrl).into(imageView)
                            loadImageBitmap(imageUrl) // 画像をBitmapとして読み込む
                        } else {
                            Toast.makeText(this, "画像のURLが存在しません", Toast.LENGTH_SHORT).show()
                        }

                        // 目標金額が存在する場合、TextViewに表示
                        if (targetAmount != null) {
                            targetAmountTextView.text = "目標金額: $targetAmount 円"
                        } else {
                            Toast.makeText(this, "目標金額が存在しません", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        // データが存在しない場合のエラーメッセージ
                        Toast.makeText(this, "指定されたPost IDのデータが見つかりません", Toast.LENGTH_SHORT).show()
                    }
                }.addOnFailureListener { exception ->
                    // データ取得に失敗した場合のエラーメッセージ
                    Toast.makeText(this, "データの取得に失敗しました: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
            } else {
                // Post IDが空の場合のエラーメッセージ
                Toast.makeText(this, "Post IDを入力してください", Toast.LENGTH_SHORT).show()
            }
        }

        dotButton.setOnClickListener {
            val drawable = imageView.drawable
            if (drawable is BitmapDrawable) {
                val bitmap = drawable.bitmap
                val uri = saveBitmapToFile(bitmap)  // 画像を一時ファイルに保存

                if (uri != null) {
                    val intent = Intent(this, CheckActivity::class.java)
                    intent.putExtra("imageUri", uri.toString())  // URIをインテントで渡す
                    startActivity(intent)
                } else {
                    Toast.makeText(this, "画像の保存に失敗しました", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "画像が選択されていません", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 選択された画像を処理
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_PICK && resultCode == RESULT_OK) {
            val selectedImageUri = data?.data
            if (selectedImageUri != null) {
                val inputStream = contentResolver.openInputStream(selectedImageUri)
                val selectedImage = BitmapFactory.decodeStream(inputStream)
                imageView.setImageBitmap(selectedImage)
                originalBitmap = selectedImage // 元のビットマップを保持
            } else {
                Toast.makeText(this, "画像の選択に失敗しました", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ドット絵に変換するメソッド
    private fun createDotImage(bitmap: Bitmap, dotSize: Int = 10): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val dotBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val tileList = mutableListOf<Tile>() // タイルを保持するリスト

        for (y in 0 until height step dotSize) {
            for (x in 0 until width step dotSize) {
                // ドットの周囲に黒い枠を作る
                for (dy in 0 until dotSize) {
                    for (dx in 0 until dotSize) {
                        // 画像の範囲内であることを確認する
                        if (x + dx < width && y + dy < height) {
                            if (dy == 0 || dy == dotSize - 1 || dx == 0 || dx == dotSize - 1) {
                                dotBitmap.setPixel(x + dx, y + dy, Color.BLACK) // 枠の色
                            } else {
                                dotBitmap.setPixel(x + dx, y + dy, Color.WHITE) // 中を白くする
                            }
                        }
                    }
                }
                // タイルをリストに追加
                tileList.add(Tile(RectF(x.toFloat(), y.toFloat(), (x + dotSize).toFloat(), (y + dotSize).toFloat())))
            }
        }
        tiles = tileList // タイルのリストを保持
        return dotBitmap
    }

    // Bitmapをファイルに保存するメソッド
    private fun saveBitmapToFile(bitmap: Bitmap): Uri? {
        val file = File(cacheDir, "temp_image.png")
        return try {
            FileOutputStream(file).use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            }
            Uri.fromFile(file) // 変更された部分
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // タッチイベントを処理するメソッド
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val x = event.x
            val y = event.y

            // タイルをタッチした場合の処理
            for (tile in tiles) {
                if (tile.rect.contains(x, y)) {
                    tile.color = selectedColor // 塗りつぶしの色を設定
                    tile.isFilled = true // 塗られたことを記録
                    break
                }
            }

            // 塗りつぶした結果を再描画
            redrawTiles()
            return true
        }
        return super.onTouchEvent(event)
    }

    // タイルを再描画するメソッド
    private fun redrawTiles() {
        if (originalBitmap == null) {
            Toast.makeText(this, "画像が読み込まれていません", Toast.LENGTH_SHORT).show()
            return // originalBitmapがnullの場合は早期リターン
        }

        val canvas = Canvas(originalBitmap!!)
        for (tile in tiles) {
            if (tile.isFilled) {
                canvas.drawRect(tile.rect, paint.apply { color = tile.color }) // タイルを描画
            }
        }
        imageView.setImageBitmap(originalBitmap) // 再描画したBitmapをImageViewにセット
    }


    // 画像をBitmapとして読み込むメソッド
    private fun loadImageBitmap(imageUrl: String) {
        // 画像をダウンロードしてBitmapに変換する処理をここに実装
        // 例: Picassoや他のライブラリを使ってBitmapを取得
        Picasso.get().load(imageUrl).into(imageView) // ここは簡略化
    }
}
