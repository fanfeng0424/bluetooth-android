package com.wheatek.ble_demo.activity

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.graphics.Color
import android.os.Bundle
import android.view.animation.LinearInterpolator
import com.wheatek.ble_demo.R
import com.wheatek.ble_demo.activity.base.BaseActivity
import com.wheatek.ble_demo.adapter.AutoConnAdapter
import com.wheatek.ble_demo.event.EventBond
import com.wheatek.ble_demo.event.EventFound
import com.wheatek.ble_demo.event.EventScan
import com.wheatek.ble_demo.event.EventCommand
import com.wheatek.ble_demo.utils.BtUtil
import com.wheatek.ble_demo.utils.ToastUtil
import kotlinx.android.synthetic.main.activity_ble.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import per.wsj.commonlib.utils.LogUtil
import java.util.*

class Bt3AutoConnActivity : BaseActivity() {

    private val mData = ArrayList<BluetoothDevice>()
    private val addrs = ArrayList<String>()

    private lateinit var adapter: AutoConnAdapter

    private var btAdapt: BluetoothAdapter? = null

    var curDevice: BluetoothDevice? = null

    var isBonded = false

    private var animator: ObjectAnimator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ble)
        EventBus.getDefault().register(this)
        title = "Bt自动连接"

        initAudio()
        initView()
        initData()
    }

    private fun initView() {
        adapter = AutoConnAdapter(this, mData)
        rvDevice.adapter = adapter

        floatingButton.setOnClickListener {
            if (!btAdapt!!.isDiscovering) {
                mData.clear()
                btAdapt?.startDiscovery()
            }
        }
    }

    private fun initData() {
        btAdapt = BluetoothAdapter.getDefaultAdapter()
        btAdapt?.let {
            scan(true)
        } ?: let {
            ToastUtil.show("您的机器上没有发现蓝牙适配器，本程序将不能运行!")
            finish()
        }
    }

    private fun scan(flag: Boolean) {
        addrs.clear()
        if (flag) {
            if (!btAdapt!!.isDiscovering) {
                LogUtil.LOGE("开始扫描：startDiscovery")
                startBannerLoadingAnim()
                btAdapt!!.startDiscovery()
            }
        } else {
            if (btAdapt!!.isDiscovering) {
                btAdapt!!.cancelDiscovery()
            }
        }
    }

    /**
     * 扫描到设备
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(eventScan: EventFound) {
        val device = eventScan.device
        if (!device.name.isNullOrEmpty() && BtUtil.isAirpod(device.bluetoothClass)
            && !addrs.contains(device.address)
        ) {
            LogUtil.LOGE("扫描到设备：" + device.name)
            scan(false)
            curDevice = device
            addrs.clear()
            mData.clear()
            addrs.add(device.address)
            mData.add(device)
            adapter.notifyDataSetChanged()
            bondAction()
        }
    }


    private fun bondAction() {
        if (!isBonded) {
            curDevice?.let {
                stopBannerLoadingAnim()
                if (it.createBond()) {
                    LogUtil.LOGE("bondAction：createBond true")
                } else {
                    LogUtil.LOGE("bondAction：createBond false")
                }
            }
        }
    }

    /**
     * 绑定设备事件
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(eventBond: EventBond) {
        curDevice?.let {
            if (eventBond.addr == it.address) {
                adapter.notifyDataSetChanged()
                when (eventBond.status) {
                    BluetoothDevice.BOND_BONDING -> {
//                        tvStatus.text = "正在配对：" + it.name + " -- " + it.address
                    }
                    BluetoothDevice.BOND_BONDED -> {
                        LogUtil.LOGE("EventBond：配对成功")
                        ToastUtil.show("配对成功：" + it.name + " -- " + it.address)
                        startAudio()
                        isBonded = true
                    }
                    BluetoothDevice.BOND_NONE -> {
//                        tvStatus.text = "配对不成功：" + it.name + " -- " + it.address + "\n"
                        LogUtil.LOGE("EventBond：配对失败")
                        scan(true)
                    }
                }
            }
        }
    }


    /**
     * 开始结束
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(eventScan: EventScan) {
        if (eventScan.isRun) {
            ToastUtil.show("正在扫描")
        } else {
            LogUtil.LOGE("EventScan：扫描结束：" + isBonded)
//            if (isBonded) {
//                btnSearch.text = "扫描设备"
//                btnSearch.setTextColor(Color.WHITE)
//                addrs.clear()
//                ToastUtil.show("扫描完成")
//            } else {
//                scan(true)
//            }
        }
    }

    /**
     * 测试完成
     */
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

    fun startBannerLoadingAnim() {
        floatingButton.setImageResource(R.drawable.ic_loading)
        animator = ObjectAnimator.ofFloat(floatingButton, "rotation", 0f, 360f)
        animator?.apply {
            repeatCount = ValueAnimator.INFINITE
            duration = 800
            interpolator = LinearInterpolator()
            start()
        }
    }

    fun stopBannerLoadingAnim() {
        floatingButton.setImageResource(R.drawable.ic_bluetooth_audio_black_24dp)
        animator?.cancel()
        floatingButton.rotation = 0f
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)

        curDevice?.let {
            BtUtil.removeBond(curDevice)
        }

        btAdapt?.let {
            scan(false)
        }
    }
}