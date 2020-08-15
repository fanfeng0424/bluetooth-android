package com.wheatek.ble_demo.activity

import android.media.AudioManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.*
import com.wheatek.ble_demo.R
import com.wheatek.ble_demo.activity.base.BaseActivity
import kotlinx.android.synthetic.main.activity_bond_direct.*
import kotlinx.android.synthetic.main.activity_play_music.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import per.wsj.commonlib.utils.LogUtil
import java.io.File

class PlayMusicActivity : BaseActivity() {

    private val uAmpAudioAttributes = AudioAttributes.Builder()
        .setContentType(C.CONTENT_TYPE_MUSIC)
        .setUsage(C.USAGE_MEDIA)
        .build()


    private val exoPlayer: ExoPlayer by lazy {
        SimpleExoPlayer.Builder(this).build().apply {
            setAudioAttributes(uAmpAudioAttributes, true)
            setHandleAudioBecomingNoisy(true)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_play_music)
//        prepareExoPlayerFromFileUri(Uri.fromFile(File(Environment.getExternalStorageDirectory().path + "/music.mp3")))

        initAudio()

        button1.setOnClickListener {
            GlobalScope.launch(Dispatchers.Main) {
                startAudio()
//                withContext(Dispatchers.IO) {
//                    Thread.sleep(1000)
//                }
//                mediaPlayer.pause()
//                withContext(Dispatchers.IO) {
//                    Thread.sleep(1000)
//                }
//                startAudio()
            }
        }

        button2.setOnClickListener {
            mediaPlayer.pause()
        }

    }

    private fun prepareExoPlayerFromFileUri(uri: Uri) {
        val dataSpec = DataSpec(uri)
        val fileDataSource = FileDataSource()
        try {
            fileDataSource.open(dataSpec)
        } catch (e: FileDataSource.FileDataSourceException) {
            e.printStackTrace()
        }
        val factory: DataSource.Factory = DataSource.Factory { fileDataSource }
        val audioSource: MediaSource = ExtractorMediaSource(
            fileDataSource.uri,
            factory, DefaultExtractorsFactory(), null, null
        )
        exoPlayer.prepare(audioSource)
        exoPlayer.playWhenReady = true
    }

    override fun onDestroy() {
        super.onDestroy()
        exoPlayer.stop()
        exoPlayer.release()
    }
}