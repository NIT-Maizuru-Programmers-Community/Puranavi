package com.example.helloworld

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class CheckActivity :AppCompatActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_check) // activity_next.xmlをレイアウトに設定
    }
}