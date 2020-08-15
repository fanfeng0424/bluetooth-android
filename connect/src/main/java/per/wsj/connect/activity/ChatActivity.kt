package per.wsj.connect.activity

import android.os.Bundle
import android.os.Environment
import android.text.TextUtils
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_chat.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import per.wsj.connect.R
import per.wsj.connect.base.bean.MessageBean
import per.wsj.connect.base.constant.BltContant
import per.wsj.connect.service.ReceiveSocketService
import per.wsj.connect.service.SendSocketService
import per.wsj.connect.utils.ToastUtil
import per.wsj.connect.utils.factory.ThreadPoolProxyFactory

class ChatActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        EventBus.getDefault().register(this)
        title = intent.getStringExtra("devicename")

        //开启消息接收端

        //开启消息接收端
        ThreadPoolProxyFactory.getNormalThreadPoolProxy()
            .execute { ReceiveSocketService().receiveMessage() }

        initEvent()
    }

    private fun initEvent() {
        btnSendTxt.setOnClickListener {
            //发送文字消息
            //发送文字消息
            if (TextUtils.isEmpty(etChat.text.toString())) {
                ToastUtil.shortShow("请先输入信息")
            } else {
                SendSocketService.sendMessage(etChat.text.toString())
            }
        }

        btnSendFile.setOnClickListener {
            SendSocketService.sendMessageByFile(
                Environment.getExternalStorageDirectory().toString() + "/test.png"
            )
        }
    }

    /**
     * RECEIVER_MESSAGE:21 收到消息
     * BltContant.SEND_TEXT_SUCCESS:发送消息成功
     * BltContant.SEND_FILE_NOTEXIT:文件不存在
     * BltContant.SEND_FILE_IS_FOLDER:不能发送文件夹
     * @param messageBean
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(messageBean: MessageBean) {
        when (messageBean.id) {
            21 -> {
                Log.d("收到消息", messageBean.content)
                text.append(
                    """
                        收到消息:${messageBean.content}
                        
                        """.trimIndent()
                )
            }
            BltContant.SEND_TEXT_SUCCESS -> {
                text.append(
                    """
                        我:${etChat.text}
                        """.trimIndent()
                )
                etChat.setText("")
            }
            BltContant.SEND_FILE_NOTEXIT ->
                ToastUtil.shortShow("发送的文件不存在，内存根目录下的test.png")
            BltContant.SEND_FILE_IS_FOLDER ->
                ToastUtil.shortShow("不能传送一个文件夹")
            else -> {
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }
}