package com.wheatek.ble_demo.activity

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.*
import android.os.Bundle
import android.os.ParcelUuid
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DefaultItemAnimator
import com.wheatek.ble_demo.R
import com.wheatek.ble_demo.activity.base.BaseActivity
import com.wheatek.ble_demo.adapter.DeviceAdapter
import com.wheatek.ble_demo.receiver.BtReceiver
import com.wheatek.ble_demo.utils.ToastUtil
import kotlinx.android.synthetic.main.activity_ble.*
import per.wsj.commonlib.utils.LogUtil
import java.util.*
import kotlin.collections.ArrayList


class BleActivity : BaseActivity() {

    var mBluetoothLeScanner: BluetoothLeScanner? = null

    lateinit var adapter: DeviceAdapter
    private val mData = LinkedList<BluetoothDevice>()
    private val addrs = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ble)

        initScanner()

        adapter = DeviceAdapter(this, mData)
        rvDevice.adapter = adapter
        rvDevice.itemAnimator = DefaultItemAnimator()

        floatingButton.setOnClickListener {
            startScan()
        }
    }

    override fun onResume() {
        super.onResume()
        startScan()
    }

    fun initScanner() {
        val mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        mBluetoothAdapter?.let {
            mBluetoothLeScanner = it.bluetoothLeScanner
        } ?: let {
            Toast.makeText(this, "请启动蓝牙", Toast.LENGTH_LONG).show()
        }
    }

    fun startScan() {
        val filters = ArrayList<ScanFilter>()
//            filters.add(ScanFilter.Builder().setServiceUuid(ParcelUuid.fromString(ADVERTISER_SERVICE_UUID)).build())
        mBluetoothLeScanner?.startScan(filters, ScanSettings.Builder().build(), callback)
        ToastUtil.show("扫描中")
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
//                LogUtil.LOGE("onScanResult:" + it.device.name + "--" + it.device.address + "--" + it.device.type)
                if (it.device.name.isNullOrEmpty()) {
                    return
                }
                if (addrs.contains(it.device.address)) {
                    var tmp: BluetoothDevice? = null
                    mData.forEach { device ->
                        if (device.address == it.device.address) {
                            tmp = device
                            return@forEach
                        }
                    }
                    tmp?.let {
                        mData.remove(it)
                    }
                    mData.push(it.device)
                    adapter.notifyDataSetChanged()
                } else {
                    addrs.add(it.device.address)
                    mData.push(it.device)
                    adapter.notifyDataSetChanged()
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
        mBluetoothLeScanner?.stopScan(callback)
    }
}