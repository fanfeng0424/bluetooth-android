package com.wheatek.ble_demo.activity

import android.content.Context
import android.media.*
import android.os.Bundle
import android.os.Environment
import androidx.appcompat.app.AppCompatActivity
import com.wheatek.ble_demo.R
import com.wheatek.ble_demo.utils.ToastUtil
import kotlinx.android.synthetic.main.activity_recorder.*
import per.wsj.commonlib.utils.LogUtil
import java.io.File
import java.io.IOException

const val SAMPLE_RATE = 8000
const val BUF_SIZE = 1024

class RecorderActivity : AppCompatActivity() {

    private lateinit var audioManager: AudioManager

    @Volatile
    var mRunning = false

    private var mRecord: AudioRecord? = null
    private var mTrack: AudioTrack? = null

    private lateinit var buffer: ByteArray
    private var bufferSize: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recorder)

        //这两句话的作用是打开设备扬声器
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        // service.isSpeakerphoneOn = true
        audioManager.isBluetoothScoOn = true
        audioManager.startBluetoothSco()

        btnStart.setOnClickListener {
//            startRecord()
            startPlay()
        }

        btnStop.setOnClickListener {
//            stopRecord()
//            ToastUtil.show("关闭")
            stopPlay()
        }

        initRecord()
    }

    fun initRecord() {
        //计算缓冲区尺寸
        bufferSize = AudioRecord.getMinBufferSize(
            SAMPLE_RATE,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        bufferSize = bufferSize.coerceAtLeast(
            AudioTrack.getMinBufferSize(
                SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT
            )
        )
        bufferSize = bufferSize.coerceAtLeast(BUF_SIZE)
        buffer = ByteArray(bufferSize)
    }

    fun startPlay() {
        mRunning = true

        //创建音频采集器，输入源是麦克风
        mRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT, bufferSize
        )

        //创建音频播放设备
        mTrack = AudioTrack(
            AudioManager.STREAM_VOICE_CALL,
            SAMPLE_RATE,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            bufferSize,
            AudioTrack.MODE_STREAM
        )
        mTrack?.playbackRate = SAMPLE_RATE

        //一边采集，一边播放
        mRecord?.startRecording()
        mTrack?.play()

        // 需要停止的时候，把mRunning置为false即可。
        Thread(Runnable {
            while (mRunning) {
                val readSize = mRecord!!.read(buffer, 0, bufferSize)
                if (readSize > 0) {
                    LogUtil.LOGE("readSize:" + readSize)
                    mTrack!!.write(buffer, 0, readSize)
                }
            }
        }).start()
    }

    fun stopPlay() {
        mRunning = false
        mRecord?.stop()
        mRecord?.release()
        mTrack?.stop()
        mTrack?.release()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopPlay()

        audioManager.isBluetoothScoOn = false
        audioManager.stopBluetoothSco()
    }
}