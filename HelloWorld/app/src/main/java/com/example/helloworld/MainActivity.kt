package com.example.helloworld

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import android.graphics.Color

class MainActivity : ComponentActivity() {

    companion object {
        const val REQUEST_IMAGE_PICK = 1
        const val EXTRA_DOT_IMAGE = "DOT_IMAGE"
    }

    private lateinit var database: DatabaseReference
    private var targetSavings: Int = 0
    private var currentSavings: Int = 0
    private lateinit var imageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Firebaseから目標金額を取得
        database = FirebaseDatabase.getInstance().getReference("savingsGoal")

        val selectFromGalleryButton = findViewById<Button>(R.id.selectFromGalleryButton)
        val dotButton = findViewById<Button>(R.id.dotButton)
        val paintButton = findViewById<Button>(R.id.paintButton)

        imageView = findViewById(R.id.imageView)

        // Firebaseからデータ取得して目標金額を取得
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                targetSavings = snapshot.getValue(Int::class.java) ?: 0
                Toast.makeText(this@MainActivity, "目標金額: $targetSavings 円", Toast.LENGTH_SHORT).show()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MainActivity, "データの取得に失敗しました", Toast.LENGTH_SHORT).show()
            }
        })

        selectFromGalleryButton.setOnClickListener {
            selectImageFromGallery()
        }

        dotButton.setOnClickListener {
            val drawable = imageView.drawable
            if (drawable is BitmapDrawable) {
                val currentBitmap = drawable.bitmap
                val dotBitmap = createDotImage(currentBitmap)
                imageView.setImageBitmap(dotBitmap)
            } else {
                Toast.makeText(this, "画像が選択されていません", Toast.LENGTH_SHORT).show()
            }
        }

        paintButton.setOnClickListener {
            val drawable = imageView.drawable
            if (drawable is BitmapDrawable) {
                val dotBitmap = createDotImage(drawable.bitmap) // ドット絵を作成
                val intent = Intent(this, PaintingActivity::class.java)
                intent.putExtra(EXTRA_DOT_IMAGE, dotBitmap) // Bitmapを渡す
                startActivity(intent)
            } else {
                Toast.makeText(this, "ドット絵を作成するには画像を選択してください", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ギャラリーから画像を選択
    private fun selectImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_IMAGE_PICK)
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

        for (y in 0 until height step dotSize) {
            for (x in 0 until width step dotSize) {
                // ドットの周囲に黒い枠を作る
                for (dy in 0 until dotSize) {
                    for (dx in 0 until dotSize) {
                        if (dy == 0 || dy == dotSize - 1 || dx == 0 || dx == dotSize - 1) {
                            dotBitmap.setPixel(x + dx, y + dy, Color.BLACK) // 枠の色
                        } else {
                            dotBitmap.setPixel(x + dx, y + dy, Color.WHITE) // 中を白くする
                        }
                    }
                }
            }
        }
        return dotBitmap
    }
}
