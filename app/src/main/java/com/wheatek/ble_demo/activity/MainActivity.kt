package com.wheatek.ble_demo.activity

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.wheatek.ble_demo.BuildConfig
import com.wheatek.ble_demo.R
import com.wheatek.ble_demo.utils.BtUtil
import kotlinx.android.synthetic.main.activity_main.*
import per.wsj.commonlib.permission.PermissionUtil


/**
 * adb shell am start -a android.intent.action.MAIN -n com.wheatek.ble_demo/com.wheatek.ble_demo.activity.MainActivity --ei uid 111 --ei pid 222
 *
 */
class MainActivity : AppCompatActivity() {

    var mBluetoothAdapter: BluetoothAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        PermissionUtil.with(this)
            .permission(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            .onDenied {
                Toast.makeText(this, it.toString(), Toast.LENGTH_LONG).show()
            }.onGranted {

            }.start()

        checkStatus()

        initView()
        initEvent()

    }

    fun initView() {
        tvVersion.text = "当前版本：" + BuildConfig.VERSION_NAME

        tvDeviceName.text = BtUtil.getSimpleSerialNo() ?: "未获取到序列号"
    }

    fun initEvent() {
        btnCommon.setOnClickListener {
            if (checkStatus()) {
                startActivity(Intent(this, Bt3Activity::class.java))
            }
        }

        btnSendBc.setOnClickListener {
            if (checkStatus()) {
                startActivity(Intent(this, SendBleActivity::class.java))
            }
        }

        btnReceiveBc.setOnClickListener {
            if (checkStatus()) {
                startActivity(Intent(this, ReceiveBleActivity::class.java))
            }
        }
    }

    fun checkStatus(): Boolean {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        mBluetoothAdapter?.let {
            if (!it.isEnabled) {
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(enableBtIntent, 1)
                return false
            }
            tvAddr.text = BtUtil.getBluetoothAddress(it)
        } ?: let {
            return false
        }
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "蓝牙已经开启", Toast.LENGTH_SHORT).show();
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "没有蓝牙权限", Toast.LENGTH_SHORT).show();
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.home_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.itemBleSearch -> {
                if (checkStatus()) {
                    startActivity(Intent(this, BleActivity::class.java))
                }
            }
            R.id.itemBleAutoConn -> {
                if (checkStatus()) {
                    startActivity(Intent(this, AutoConnActivity::class.java))
                }
            }
            R.id.itemBtSearch -> {
                if (checkStatus()) {
                    startActivity(Intent(this, Bt3Activity::class.java))
                }
            }
            R.id.itemBtAutoConn -> {
                if (checkStatus()) {
                    startActivity(Intent(this, Bt3AutoConnActivity::class.java))
                }
            }
            R.id.itemRecorder -> {
                startActivity(Intent(this, RecorderActivity::class.java))
            }
            R.id.itemMusic -> {
                startActivity(Intent(this, PlayMusicActivity::class.java))
            }
        }
        return super.onOptionsItemSelected(item)
    }
}