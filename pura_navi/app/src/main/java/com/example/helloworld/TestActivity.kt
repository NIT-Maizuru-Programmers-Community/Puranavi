package com.example.helloworld

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket

class TestActivity : AppCompatActivity() {
    private lateinit var receivedDataTextView: TextView
    private lateinit var socket: Socket
    private lateinit var output: PrintWriter
    private lateinit var input: BufferedReader
    private val serverIp = "172.20.10.13" // ArduinoのIPアドレス
    private val serverPort = 80 // Arduinoのポート番号
    private var accumulatedSum = 0 // 累積する変数

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)

        receivedDataTextView = findViewById(R.id.receivedDataTextView)

        Thread {
            try {
                // サーバーに接続
                socket = Socket(serverIp, serverPort)
                output = PrintWriter(socket.getOutputStream(), true)
                input = BufferedReader(InputStreamReader(socket.getInputStream()))

                while (true) {
                    // Arduinoからデータを受信
                    val receivedData = input.readLine()

                    // 受信データを整数に変換し、累積に加算
                    receivedData?.toIntOrNull()?.let { value ->
                        accumulatedSum += value
                    }

                    runOnUiThread {
                        // UIスレッドで累積した値を表示
                        receivedDataTextView.text = accumulatedSum.toString()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
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
}
