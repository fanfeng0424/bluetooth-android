package com.wheatek.ble_demo.activity

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.os.Bundle
import com.wheatek.ble_demo.R
import com.wheatek.ble_demo.activity.base.BaseActivity
import com.wheatek.ble_demo.event.EventBond
import com.wheatek.ble_demo.event.EventCommand
import com.wheatek.ble_demo.utils.BtUtil
import kotlinx.android.synthetic.main.activity_auto_conn.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import per.wsj.commonlib.utils.LogUtil


class AutoConnActivity : BaseActivity() {

    lateinit var mBluetoothLeScanner: BluetoothLeScanner
    var curDevice: BluetoothDevice? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auto_conn)
        EventBus.getDefault().register(this)
        title = "Ble自动连接"

        initAudio()
        val mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        mBluetoothLeScanner = mBluetoothAdapter.bluetoothLeScanner
        scan(true)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(eventBond: EventBond) {
        curDevice?.let {
            if (eventBond.addr == it.address) {
                when (eventBond.status) {
                    BluetoothDevice.BOND_NONE -> {
                        tvStatus.text = "配对不成功：" + it.name + " -- " + it.address + "\n开始需要继续扫描"
                        scan(true)
                    }
                    BluetoothDevice.BOND_BONDING -> {
                        tvStatus.text = "正在配对：" + it.name + " -- " + it.address
                    }
                    BluetoothDevice.BOND_BONDED -> {
                        tvStatus.text = "配对成功：" + it.name + " -- " + it.address
                        startAudio()
                    }
                }
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(command: EventCommand) {
        when (command.command) {
            1 -> {
                finish()
            }
            2 -> {
                stopAudio()
                startMicTest()
            }
        }
    }

    /**
     *
     */
    fun scan(enable: Boolean) {
        if (enable) {
            tvStatus.text = "扫描中"
            mBluetoothLeScanner.startScan(callback)
        } else {
            mBluetoothLeScanner.stopScan(callback)
        }
    }

    val callback = object : ScanCallback() {
        override fun onBatchScanResults(results: MutableList<ScanResult>?) {
            super.onBatchScanResults(results)
            for (result in results!!) {
                LogUtil.LOGE("onBatchScanResults:" + result.device.name)
            }
        }

        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)
            result?.let {
                if (!it.device.name.isNullOrEmpty()) {
                    LogUtil.LOGE("扫描:" + it.device.name)
                    if (it.device.createBond()) {
                        // 如果启动绑定则保存当前设备对象，并停止扫描
                        curDevice = it.device
                        tvStatus.text = "准备配对：" + it.device.name + " -- " + it.device.address
                        scan(false)
                    }
                }
            }
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            LogUtil.LOGE("onScanFailed:$errorCode")
        }
    }


    override fun onStop() {
        super.onStop()
        scan(false)
    }

    override fun onDestroy() {
        super.onDestroy()
        // 解除绑定
        curDevice?.let {
            BtUtil.removeBond(curDevice)
        }
        EventBus.getDefault().unregister(this)
    }
}