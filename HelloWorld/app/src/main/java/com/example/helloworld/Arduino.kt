package com.example.helloworld

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.BluetoothLeScanner
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class Arduino :AppCompatActivity(){
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var bluetoothLeScanner: BluetoothLeScanner
    private lateinit var textView: TextView

    private var bluetoothGatt: BluetoothGatt? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.arduino_test)

        textView = findViewById(R.id.textView)

        // Bluetoothアダプターを取得
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        bluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner

        // パーミッションの確認
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
            != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN), 1)
        } else {
            startScan()
        }
    }

    // BLEデバイスのスキャンを開始
    private fun startScan() {
        bluetoothLeScanner.startScan(leScanCallback)
    }

    // スキャン結果のコールバック
    private val leScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            result?.let {
                val device: BluetoothDevice = it.device
                val deviceName = device.name

                // ArduinoR4WiFiという名前のデバイスを見つけたら接続
                if (deviceName == "ArduinoR4WiFi") {
                    runOnUiThread {
                        textView.text = "デバイス発見: $deviceName"
                    }
                    bluetoothLeScanner.stopScan(this)

                    // GATT接続を開始
                    bluetoothGatt = device.connectGatt(this@Arduino, false, gattCallback)
                }
            }
        }
    }

    // GATT接続のコールバック
    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            if (newState == BluetoothGatt.STATE_CONNECTED) {
                runOnUiThread {
                    textView.text = "接続完了"
                }
                gatt?.discoverServices()  // サービスの発見
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(gatt, status)

            if (status == BluetoothGatt.GATT_SUCCESS) {
                // サービスが発見された後、キャラクタリスティックを読み取る
                val service = gatt?.getService(java.util.UUID.fromString("180C"))
                val characteristic = service?.getCharacteristic(java.util.UUID.fromString("2A56"))
                if (characteristic != null) {
                    gatt.readCharacteristic(characteristic)
                }
            }
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            super.onCharacteristicRead(gatt, characteristic, status)
            if (status == BluetoothGatt.GATT_SUCCESS) {
                // キャラクタリスティックの値を取得
                val value = characteristic?.getStringValue(0)
                runOnUiThread {
                    textView.text = "受信データ: $value"
                }
            }
        }
    }
}
