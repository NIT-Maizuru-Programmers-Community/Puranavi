package com.example.helloworld

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent

class SecondActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.start_screen) // 次の画面のレイアウトを設定

        val start = findViewById<Button>(R.id.change_btn)
        start.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java) // MainActivityのインスタンスを作成する必要はない
            startActivity(intent)
        }
    }
}
