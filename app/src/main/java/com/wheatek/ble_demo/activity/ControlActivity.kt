package com.wheatek.ble_demo.activity

import android.bluetooth.*
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.wheatek.ble_demo.R
import com.wheatek.ble_demo.activity.base.BaseActivity
import com.wheatek.ble_demo.event.EventBond
import com.wheatek.ble_demo.utils.BtUtil
import com.wheatek.ble_demo.utils.ToastUtil
import kotlinx.android.synthetic.main.activity_control.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import per.wsj.commonlib.utils.LogUtil

class ControlActivity : BaseActivity() {

    private lateinit var mDeviceAddress: String
    private lateinit var device: BluetoothDevice
    var mBluetoothGatt: BluetoothGatt? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_control)

        EventBus.getDefault().register(this)

        initAudio()
        initView()
        initEvent()
    }

    private fun initView() {
        title = intent.getStringExtra(PARAM_NAME)
        mDeviceAddress = intent.getStringExtra(PARAM_ADDR)
        tvAddr.text = mDeviceAddress
        device = intent.getParcelableExtra(PARAM_DEVICE)
    }

    fun initEvent() {
        btnBind.setOnClickListener {
            device.createBond()
        }

        btnUnbind.setOnClickListener {
            Log.e(packageName, "解绑")
            BtUtil.removeBond(device)
        }

//        btnConn.setOnClickListener {
//            val remoteDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(mDeviceAddress)
//            mBluetoothGatt = remoteDevice.connectGatt(this, true, callback)
//        }
    }

    /**
     * 绑定设备事件
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(eventBond: EventBond) {
        when (eventBond.status) {
            BluetoothDevice.BOND_BONDING -> {
//                        tvStatus.text = "正在配对：" + it.name + " -- " + it.address
                ToastUtil.show("正在配对")
            }
            BluetoothDevice.BOND_BONDED -> {
                LogUtil.LOGE("EventBond：配对成功")
                ToastUtil.show("配对成功：" + eventBond.addr)
                startAudio()
            }
            BluetoothDevice.BOND_NONE -> {
//                        tvStatus.text = "配对不成功：" + it.name + " -- " + it.address + "\n"
                ToastUtil.show("失败")
                LogUtil.LOGE("EventBond：配对失败")
            }
        }
    }


    val callback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            if (BluetoothGatt.STATE_CONNECTED == newState) {
                LogUtil.LOGE("连接成功:")
                //必须有，可以让onServicesDiscovered显示所有Services
                gatt?.discoverServices();
                LogUtil.LOGE("连接成功2:")
            } else if (BluetoothGatt.STATE_DISCONNECTED == newState) {
                LogUtil.LOGE("断开连接:")
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(gatt, status)
            val list = mBluetoothGatt!!.services as List<BluetoothGattService>
            for (bluetoothGattService in list) {
                val str = bluetoothGattService.uuid.toString()
                LogUtil.LOGE("onServicesDiscovered:" + str)

                val gattCharacteristics = bluetoothGattService.characteristics
                for (gattCharacteristic in gattCharacteristics) {
                    LogUtil.LOGE("onServicesDiscovered:uuid" + gattCharacteristic.uuid)
                    if ("0000ffe1-0000-1000-8000-00805f9b34fb" == gattCharacteristic.uuid.toString()
                    ) {
//                        linkLossService = bluetoothGattService;
//                        alertLevel = gattCharacteristic;
//                        Log.e("daole", alertLevel.getUuid().toString());
                    }
                }

//                enableNotification(true, gatt, alertLevel);//必须要有，否则接收不到数据
            }
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            super.onCharacteristicWrite(gatt, characteristic, status)
            LogUtil.LOGE("onCharacteristicWrite")
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?
        ) {
            super.onCharacteristicChanged(gatt, characteristic)
            LogUtil.LOGE("onCharacteristicChanged:" + characteristic?.value)
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        EventBus.getDefault().unregister(this)
    }

    companion object {
        const val PARAM_DEVICE = "param_device"
        const val PARAM_NAME = "param_name"
        const val PARAM_ADDR = "param_addr"
        const val LIST_NAME = "NAME"
        const val LIST_UUID = "UUID"

        fun startActivity(
            context: Context,
            device: BluetoothDevice,
            name: String,
            addr: String
        ) {
            val intent = Intent(context, ControlActivity::class.java)
            intent.putExtra(PARAM_DEVICE, device)
            intent.putExtra(PARAM_NAME, name)
            intent.putExtra(PARAM_ADDR, addr)
            context.startActivity(intent)
        }
    }
}