package com.wheatek.ble_demo.activity

import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.os.Bundle
import android.os.ParcelUuid
import androidx.appcompat.app.AppCompatActivity
import com.wheatek.ble_demo.R
import kotlinx.android.synthetic.main.activity_send_ble.*
import per.wsj.commonlib.utils.LogUtil
import java.lang.StringBuilder


const val ADVERTISER_SERVICE_UUID_BASE = "abcd"
const val ADVERTISER_SERVICE_UUID =
    "0001$ADVERTISER_SERVICE_UUID_BASE-0405-0607-0809-0a0b0c0d0e0f"

class SendBleActivity : AppCompatActivity() {
    var isRunning = true
    lateinit var data: AdvertiseData
    lateinit var settings: AdvertiseSettings

    val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    val advertiser = bluetoothAdapter.bluetoothLeAdvertiser
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_send_ble)

        val EDDYSTONE_SERVER_DATAS = byteArrayOf( //      0x02, 0x01, 0x02,
            //      0x1b, 0x16, 0xcd, 0xab, // uuid
            0x17, 0x16, 0xAA.toByte(), 0xFE.toByte(),  // UID type's len is 0x17
            0xff.toByte(), 0xff.toByte() // RFU
        )
        data = AdvertiseData.Builder()
            .addServiceData(ParcelUuid.fromString(ADVERTISER_SERVICE_UUID), EDDYSTONE_SERVER_DATAS)
            .build()

        settings = AdvertiseSettings.Builder().setConnectable(false).build()

        send()
    }

    fun sendBg() {
        Thread(Runnable {
            send()
        }).start()
    }

    fun send() {
        val text = StringBuilder()
        text.append("\r\nstartAdvertising...")
        advertiser.startAdvertising(settings, data, object : AdvertiseCallback() {

            override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
                super.onStartSuccess(settingsInEffect)
                text.append("\r\nonStartSuccess...");
                text.append("\r\ntoString:" + settingsInEffect.toString())
                text.append("\r\ndescribeContents:" + settingsInEffect?.describeContents())
                text.append("\r\ngetMode:" + settingsInEffect?.mode)
                text.append("\r\ngetTimeout:" + settingsInEffect?.timeout)
                text.append("\r\ngetTxPowerLevel:" + settingsInEffect?.txPowerLevel)
                LogUtil.LOGE(text.toString())
                runOnUiThread {
                    tvResult.text = text.toString()
                }
                if (isRunning) {
                    Thread.sleep(2000)
                    sendBg()
                }
            }

            override fun onStartFailure(errorCode: Int) {
                super.onStartFailure(errorCode)
                text.append("\r\nerrorCode:$errorCode")
                LogUtil.LOGE(text.toString())
                runOnUiThread {
                    tvResult.text = text.toString()
                }

                if (isRunning) {
                    Thread.sleep(10000)
                    send()
                }
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
    }
}