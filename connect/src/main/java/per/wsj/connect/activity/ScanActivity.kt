package per.wsj.connect.activity

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.AdapterView.OnItemClickListener
import android.widget.SimpleAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_scan.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import per.wsj.commonlib.permission.PermissionUtil
import per.wsj.connect.APP
import per.wsj.connect.R
import per.wsj.connect.base.bean.BluRxBean
import per.wsj.connect.base.constant.BltContant
import per.wsj.connect.base.manger.BltManager
import per.wsj.connect.receivers.BlueToothReceiver
import per.wsj.connect.service.BltService
import per.wsj.connect.utils.ToastUtil
import per.wsj.connect.utils.factory.ThreadPoolProxyFactory
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.Map
import kotlin.collections.MutableMap
import kotlin.collections.set

const val connectsuccess = 12 //连接成功

class ScanActivity : AppCompatActivity() {
    private var bluetoothadapter: BluetoothAdapter? = null
    private var adapter: SimpleAdapter? = null
    private var list = ArrayList<Map<String, String>>()
    private var listdevice = ArrayList<BluetoothDevice>()
    private val blueToothReceiver = BlueToothReceiver()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan)

        EventBus.getDefault().register(this)

        registerReceiver(blueToothReceiver, blueToothReceiver.makeFilter())
        BltManager.getInstance().initBltManager(this)
        init()
        initblue()

        btnSearch.setOnClickListener {
            if (!bluetoothadapter!!.isEnabled) {
                val enabler = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(enabler, 1)
            } else {
                startscan()
            }
        }
    }

    /**
     * 初始化蓝牙设备
     */
    private fun initblue() {
        bluetoothadapter = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothadapter == null) {
            Toast.makeText(this, "设备不支持蓝牙", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * 组件初始化
     */
    private fun init() {
        list = java.util.ArrayList()
        listdevice = java.util.ArrayList()
        /**
         * listview监听事件 即配对
         */
        listview.setOnItemClickListener(OnItemClickListener { parent, view, position, id ->
            val map: Map<String, String>
            map = list[position]
            if (map["statue"] == "已配对") {
                ToastUtil.show("正在连接")
                ThreadPoolProxyFactory.getNormalThreadPoolProxy()
                    .execute { connect(listdevice[position]) }
            } else {
                try {
                    // 如果想要取消已经配对的设备，只需要将creatBond改为removeBond
                    val method =
                        BluetoothDevice::class.java.getMethod("createBond")
                    Log.e(packageName, "开始配对")
                    method.invoke(listdevice[position])
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        })
    }

    /**
     * 开始扫描蓝牙
     */
    private fun startscan() {
        list.clear()
        adapter?.let {
            it.notifyDataSetChanged()
            listdevice.clear()
        }
        /**
         * 开启蓝牙服务端
         */
        ThreadPoolProxyFactory.getNormalThreadPoolProxy()
            .execute { BltService.getInstance().startBluService() }
        PermissionUtil.with(this)
            .permission(Manifest.permission.ACCESS_COARSE_LOCATION)
            .onDenied {
                Toast.makeText(
                    this,
                    it.toString(),
                    Toast.LENGTH_LONG
                ).show()
            }.onGranted {
                if (bluetoothadapter!!.isDiscovering) {
                    bluetoothadapter!!.cancelDiscovery()
                }
                bluetoothadapter!!.startDiscovery()
            }.start()
    }

    /**
     * EventBus 异步
     * 1:找到设备
     * 2：扫描完成
     * 3：开始扫描
     * 4.配对成功
     * 11:有设备连接进来
     * 12:连接成功
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(bluRxBean: BluRxBean) {
        var intent: Intent? = null
        when (bluRxBean.getId()) {
            1 -> {
                listdevice.add(bluRxBean.getBluetoothDevice())
                val map: MutableMap<String, String> =
                    HashMap()
                map["deviceName"] =
                    bluRxBean.getBluetoothDevice().name + ":" + bluRxBean.getBluetoothDevice()
                        .address
                if (bluRxBean.getBluetoothDevice().bondState != BluetoothDevice.BOND_BONDED) {
                    map["statue"] = "未配对"
                } else {
                    map["statue"] = "已配对"
                }
                list.add(map)
                adapter = SimpleAdapter(
                    this,
                    list,
                    R.layout.devices,
                    arrayOf("deviceName", "statue"),
                    intArrayOf(R.id.devicename, R.id.statue)
                )
                listview.adapter = adapter
            }
            2 -> {
            }
            3 -> ToastUtil.show("正在扫描")
            11, 12 -> {
                intent = Intent(this, ChatActivity::class.java)
                intent.putExtra("devicename", bluRxBean.getBluetoothDevice().name)
                startActivity(intent)
            }
            else -> {
            }
        }
    }

    /***
     * 蓝牙连接代码,项目中连接会使用封装的工具类，在这里提取重写
     */
    private fun connect(bluetoothDevice: BluetoothDevice) {
        /**
         * 配对成功后的蓝牙套接字
         */
        var mBluetoothSocket: BluetoothSocket? = null
        try {
            mBluetoothSocket =
                bluetoothDevice.createRfcommSocketToServiceRecord(BltContant.SPP_UUID)
            if (mBluetoothSocket != null) {
                APP.bluetoothSocket = mBluetoothSocket
                if (bluetoothadapter!!.isDiscovering) {
                    bluetoothadapter!!.cancelDiscovery()
                }
                if (!mBluetoothSocket!!.isConnected) {
                    mBluetoothSocket!!.connect()
                }
                EventBus.getDefault().post(
                    BluRxBean(
                        connectsuccess,
                        bluetoothDevice
                    )
                )
            }
        } catch (e: IOException) {
            e.printStackTrace()
            try {
                mBluetoothSocket!!.close()
            } catch (e1: IOException) {
                e1.printStackTrace()
            }
        }
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 1) {
                startscan()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(blueToothReceiver)
        EventBus.getDefault().unregister(this)
    }

}