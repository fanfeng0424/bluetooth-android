package com.wheatek.ble_demo.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.wheatek.ble_demo.event.EventCommand
import org.greenrobot.eventbus.EventBus
import per.wsj.commonlib.utils.LogUtil

class StopReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val command = intent.getIntExtra("command", 0)
        LogUtil.LOGE("command:$command")
        if (command != 0) {
            EventBus.getDefault().post(EventCommand(command))
        }
    }
}
