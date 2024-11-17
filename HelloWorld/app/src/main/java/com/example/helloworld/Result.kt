package com.example.helloworld

import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent

class Result : AppCompatActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.result_menu)

        // ByteArray から Bitmap に変換
        val byteArray = intent.getByteArrayExtra("bitmap")
        val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray!!.size)

        // ImageView に Bitmap をセット
        val imageView = findViewById<ImageView>(R.id.imageView)
        imageView.setImageBitmap(bitmap)
        val backButton = findViewById<Button>(R.id.button_back_to_title)
        backButton.setOnClickListener {
            // SecondActivityに遷移
            val intent = Intent(this, SecondActivity::class.java)
            startActivity(intent)
        }
    }
}