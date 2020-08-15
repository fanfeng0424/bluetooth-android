package com.wheatek.ble_demo.activity

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.animation.LinearInterpolator
import com.bumptech.glide.Glide
import com.wheatek.ble_demo.R
import com.wheatek.ble_demo.activity.base.BaseActivity
import com.wheatek.ble_demo.event.EventBond
import com.wheatek.ble_demo.event.EventCommand
import com.wheatek.ble_demo.utils.BtUtil
import kotlinx.android.synthetic.main.activity_bond_direct.*
import kotlinx.android.synthetic.main.activity_bond_direct.tvAddr
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class BondDirectActivity : BaseActivity() {

    var curDevice: BluetoothDevice? = null
    lateinit var mAnimation: ObjectAnimator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bond_direct)
        EventBus.getDefault().register(this)
        title = "测试"
        initAudio()

        initView()

        mAnimation = ObjectAnimator.ofFloat(ivPlaying, "rotation", 0f, 360f)
        mAnimation.apply {
            duration = 6000
            interpolator = LinearInterpolator()
            repeatCount = ValueAnimator.INFINITE
        }
    }

    fun initView() {
        tvName.text = "AirPods"
        val address = intent.getStringExtra("address")
        tvAddr.text = address
        curDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(address)
        curDevice?.let {
            if (it.createBond()) {
                tvStatus.text = "准备配对"
            } else {
                tvStatus.text = "无法配对"
            }
        } ?: let {
            tvStatus.text = "地址异常"
        }

        tvSerialNo.text = BtUtil.getSimpleSerialNo() ?: "未获取到序列号"
    }

    /**
     * 绑定设备事件
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(eventBond: EventBond) {
        curDevice?.let {
            if (eventBond.addr == it.address) {
                when (eventBond.status) {
                    BluetoothDevice.BOND_BONDING -> {
                        tvStatus.text = "正在配对"
                        tvStatus.setTextColor(resources.getColor(R.color.blue))
                    }
                    BluetoothDevice.BOND_BONDED -> {
                        tvStatus.text = "配对成功"
                        tvStatus.setTextColor(Color.GREEN)
                        GlobalScope.launch(Dispatchers.Main) {
                            mAnimation.start()
                            sleep(2000)
                            startAudio()
                            tvAction.text = "正在播放音频"
                            sleep(1000)
                            mediaPlayer.pause()
                            sleep(1000)
                            startAudio()
                        }
                    }
                    BluetoothDevice.BOND_NONE -> {
                        tvStatus.text = "配对失败"
                        tvStatus.setTextColor(Color.RED)
                    }
                }
            }
        }
    }

    // 在协程中睡眠指定时间
    private suspend fun sleep(time: Long) {
        withContext(Dispatchers.IO) { Thread.sleep(time) }
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
                tvAction.text = "结束喇叭测试"
                stopAudio()
                mAnimation.pause()
            }
            3 -> {
                tvAction.text = "正在测试麦克风"
                ivPlaying.visibility = View.INVISIBLE
                startMicTest()
                Glide.with(this).load(R.mipmap.recording).into(ivRecording)
                ivRecording.visibility = View.VISIBLE
            }
            4 -> {
                tvAction.text = "已结束麦克风测试"
                ivRecording.visibility = View.GONE
                stopMicTest()
            }
        }
    }

    override fun onBackPressed() {
        // super.onBackPressed()
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
        curDevice?.let {
            BtUtil.removeBond(curDevice)
        }
        mAnimation.cancel()
    }
}