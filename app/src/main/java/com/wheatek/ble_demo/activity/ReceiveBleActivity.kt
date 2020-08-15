package com.wheatek.ble_demo.activity

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.wheatek.ble_demo.R
import per.wsj.commonlib.utils.LogUtil
import java.util.*

class ReceiveBleActivity : AppCompatActivity() {

    val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan_broadcast)

        val serviceUuids: Array<UUID> =
            arrayOf<UUID>(UUID.fromString(ADVERTISER_SERVICE_UUID))

        bluetoothAdapter.startLeScan(callback)
    }

    val callback = object : BluetoothAdapter.LeScanCallback {
        override fun onLeScan(device: BluetoothDevice?, rssi: Int, scanRecord: ByteArray?) {
            LogUtil.LOGE("device:" + device?.address)
            LogUtil.LOGE("data:" + scanRecord.toString())
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        bluetoothAdapter.stopLeScan(callback)
    }
}