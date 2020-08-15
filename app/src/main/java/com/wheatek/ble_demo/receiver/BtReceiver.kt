package com.wheatek.ble_demo.receiver

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.wheatek.ble_demo.event.EventBond
import com.wheatek.ble_demo.event.EventFound
import com.wheatek.ble_demo.event.EventScan
import org.greenrobot.eventbus.EventBus
import per.wsj.commonlib.utils.LogUtil

class BtReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            BluetoothAdapter.ACTION_STATE_CHANGED -> {
                LogUtil.LOGE("ACTION_STATE_CHANGED")
            }
            BluetoothDevice.ACTION_FOUND -> { //found device
                val device =
                    intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                EventBus.getDefault().post(EventFound(device))
            }
            BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
//                ToastUtil.show("正在扫描")
                EventBus.getDefault().post(EventScan(true))
            }
            BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
//                ToastUtil.show("扫描完成，点击列表中的设备来尝试连接")
                EventBus.getDefault().post(EventScan(false))
            }
            BluetoothDevice.ACTION_BOND_STATE_CHANGED -> {
                val device =
                    intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                when (device.bondState) {
                    BluetoothDevice.BOND_NONE -> {
                        LogUtil.LOGE("配对结果NONE")
                    }
                    BluetoothDevice.BOND_BONDING -> {
                        LogUtil.LOGE("配对中11")
                    }
                    BluetoothDevice.BOND_BONDED -> {
                        LogUtil.LOGE("配对成功11")
                    }
                }
                EventBus.getDefault().post(EventBond(device.address, device.bondState))
            }
        }
    }

    /**
     * 蓝牙广播过滤器
     * 蓝牙状态改变
     * 找到设备
     * 搜索完成
     * 开始扫描
     * 状态改变
     *
     * @return
     */
    fun makeFilter(): IntentFilter? {
        val filter = IntentFilter()
        filter.apply {
            addAction(BluetoothAdapter.ACTION_STATE_CHANGED) //蓝牙状态改变的广播
            addAction(BluetoothDevice.ACTION_FOUND) //找到设备的广播
            addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED) //搜索完成的广播
            addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED) //开始扫描的广播
            addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED) //状态改变
        }
        return filter
    }
}
