package com.wheatek.ble_demo.activity

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Bundle
import android.view.animation.LinearInterpolator
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DefaultItemAnimator
import com.wheatek.ble_demo.R
import com.wheatek.ble_demo.activity.base.BaseActivity
import com.wheatek.ble_demo.adapter.DeviceAdapter
import com.wheatek.ble_demo.event.EventFound
import com.wheatek.ble_demo.event.EventScan
import com.wheatek.ble_demo.utils.BtUtil
import com.wheatek.ble_demo.utils.ToastUtil
import kotlinx.android.synthetic.main.activity_ble.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import per.wsj.commonlib.utils.LogUtil
import java.util.*

class Bt3Activity : BaseActivity() {

    private val mData = ArrayList<BluetoothDevice>()
    private val addrs = ArrayList<String>()

    private lateinit var adapter: DeviceAdapter

    private var btAdapt: BluetoothAdapter? = null

    private var animator: ObjectAnimator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ble)
        EventBus.getDefault().register(this)

        initView()
        initData()
    }

    private fun initView() {
        adapter = DeviceAdapter(this, mData)
        rvDevice.itemAnimator = DefaultItemAnimator()
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
            if (!it.isDiscovering) {
                mData.clear()
                it.startDiscovery()
            }
        } ?: let {
            ToastUtil.show("您的机器上没有发现蓝牙适配器，本程序将不能运行!")
            finish()
        }
    }

    /**
     * 扫描到设备
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(eventScan: EventFound) {
        val device = eventScan.device
        if (!device.name.isNullOrEmpty() && !addrs.contains(device.address)) {
            addrs.add(device.address)
            mData.add(device)
            adapter.notifyDataSetChanged()
        }
    }

    /**
     * 开始结束
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(eventScan: EventScan) {
        if (eventScan.isRun) {
            ToastUtil.show("正在扫描")
            startBannerLoadingAnim()
        } else {
            addrs.clear()
            stopBannerLoadingAnim()
            ToastUtil.show("扫描完成")
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
        btAdapt?.let {
            if (it.isDiscovering) {
                it.cancelDiscovery()
            }
        }
    }
}