/**
 * Copyright (C) 2020 by Qifan YANG (@underwindfall)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qifan.webrtcsamples

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.qifan.webrtcsamples.databinding.ActivityWebRtcBinding
import io.socket.client.Socket

class WebRtcActivity : AppCompatActivity() {
    private lateinit var webRtcBinding: ActivityWebRtcBinding
    private val handUpBtn get() = webRtcBinding.btnHangUp
    private val muteBtn get() = webRtcBinding.btnMute
    private val localViewRender get() = webRtcBinding.rtcViewLocal
    private val remoteViewRender get() = webRtcBinding.rtcViewRemote

    private lateinit var roomId: String
    private lateinit var ipAddr: String
    private var isInitiator = false
    private var isChannelReady = false
    private var isStarted = false
    private lateinit var socket: Socket
    private var enabledAudio = true

//    private val rootEglBase: EglBase by lazy { EglBase.create() }
//    private val videoCapturer: CameraVideoCapturer by lazy { buildVideoCapturer() }
//    private var peerConnectionFactory: PeerConnectionFactory by Delegates.notNull()

    companion object {
        private const val ROOM = "ROOM"
        private const val IPADDRESS = "IPADDRESS"

        @JvmStatic
        fun Activity.startWebRtcActivity(roomId: String, ipAddr: String) {
            startActivity(
                Intent(this, WebRtcActivity::class.java)
                    .putExtra(ROOM, roomId)
                    .putExtra(IPADDRESS, ipAddr)
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        webRtcBinding = ActivityWebRtcBinding.inflate(layoutInflater)
        val view = webRtcBinding.root
        setContentView(view)
        parseIntents()
//        initializeWebRtc()
    }
/*
    override fun onResume() {
        super.onResume()
        videoCapturer.startCapture(VIDEO_RESOLUTION_WIDTH, VIDEO_RESOLUTION_HEIGHT, FPS)
    }

    override fun onPause() {
        super.onPause()
        try {
            videoCapturer.stopCapture()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }*/

    private fun parseIntents() {
        val room = intent.getStringExtra(ROOM)
        val ip = intent.getStringExtra(IPADDRESS)
        require(room != null && ip != null) { "Must have essential parameter to initialize call" }
        roomId = room
        ipAddr = ip
    }

    /*private fun initializeWebRtc() {
        initializeSurfaceView()
        initializeSignaling()
        initializePeerConnectionFactory()
        initializePeerConnectionFactory()
    }

    private fun initializeSignaling() {
        try {
            IO.socket(ipAddr)
                .also { socket = it }
                .apply {
                    connect()
                    on(Socket.EVENT_CONNECT) {
                        socket.emit("create or join", roomId)
                    }
                    on("ipaddr") {
                        debug(message = "webrtc socket signaling local ipaddr is ${it.firstOrNull()} ")
                    }
                    on("created") {
                        isInitiator = true
                    }
                    on("join") {
                        debug(message = "someone start join the room")
                    }
                    on("joined") {
                        isChannelReady = true
                    }
                    on("log") { args ->
                        args.forEach { debug(message = "webrtc socket signaling debuging $it") }
                    }
                    on("message") { args ->
                        debug(message = "webrtc socket siganling send message")
                        try {
                            val message = args.firstOrNull()
                            check(message is JSONObject)
                            //TODO
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    }
                    on("close") {
                        //TODO
                        socket.close()
                    }
                }

        } catch (e: URISyntaxException) {
            error(e)
        }
    }

    private fun initializeSurfaceView() {
        localViewRender.initializeSurfaceView(rootEglBase)
        remoteViewRender.initializeSurfaceView(rootEglBase)
    }

    private fun initializePeerConnectionFactory() {
        val options = PeerConnectionFactory.Options()
        val decoderVideoFactory = DefaultVideoDecoderFactory(rootEglBase.eglBaseContext)
        val encoderFactory = DefaultVideoEncoderFactory(rootEglBase.eglBaseContext, true, true)
        val audioDeviceFactory = createJavaAudioDevice(applicationContext)
        //load ndk webrtc native library into process
        PeerConnectionFactory.initialize(
            PeerConnectionFactory.InitializationOptions
                .builder(applicationContext)
                .setEnableInternalTracer(true)
                .createInitializationOptions()
        )
        //configure connection factory
        peerConnectionFactory = PeerConnectionFactory
            .builder()
            .setOptions(options)
            .setVideoDecoderFactory(decoderVideoFactory)
            .setVideoEncoderFactory(encoderFactory)
            .setAudioDeviceModule(audioDeviceFactory)
            .createPeerConnectionFactory()
    }

    *//**
     * Create Java audio device
     *
     * @param context context
     * @return well configured audio device
     *//*
    private fun createJavaAudioDevice(context: Context): AudioDeviceModule {
        // Set audio record error callbacks
        val audioRecordErrorCallback = object : JavaAudioDeviceModule.AudioRecordErrorCallback {
            override fun onWebRtcAudioRecordInitError(p0: String?) {
                error(message = "onWebRtcAudioRecordInitError $p0")
            }

            override fun onWebRtcAudioRecordError(p0: String?) {
                error(message = "onWebRtcAudioRecordError $p0")
            }

            override fun onWebRtcAudioRecordStartError(
                p0: JavaAudioDeviceModule.AudioRecordStartErrorCode?,
                p1: String?
            ) {
                error(message = "onWebRtcAudioRecordStartError code => $p0  message=> $p1 ")
            }
        }
        // Set audio track error callbacks
        val audioTrackErrorCallback = object : JavaAudioDeviceModule.AudioTrackErrorCallback {
            override fun onWebRtcAudioTrackError(p0: String?) {
                error(message = "onWebRtcAudioTrackError $p0")
            }

            override fun onWebRtcAudioTrackStartError(
                p0: JavaAudioDeviceModule.AudioTrackStartErrorCode?,
                p1: String?
            ) {
                error(message = "onWebRtcAudioTrackStartError code => $p0 message=> $p1")
            }

            override fun onWebRtcAudioTrackInitError(p0: String?) {
                error(message = "onWebRtcAudioTrackInitError  $p0")
            }
        }
        return JavaAudioDeviceModule.builder(context)
            .setUseHardwareAcousticEchoCanceler(true)
            .setUseHardwareNoiseSuppressor(true)
            .setAudioRecordErrorCallback(audioRecordErrorCallback)
            .setAudioTrackErrorCallback(audioTrackErrorCallback)
            .createAudioDeviceModule()
    }

    private fun SurfaceViewRenderer.initializeSurfaceView(rootEglBase: EglBase) = apply {
        setMirror(true)
        setEnableHardwareScaler(true)
        setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT)
        init(rootEglBase.eglBaseContext, null)
    }

    private fun buildVideoCapturer(): CameraVideoCapturer {
        val canUseCamera2 = Camera2Enumerator.isSupported(this)
        return if (canUseCamera2) {
            createCameraCapturer(Camera2Enumerator(this))
        } else {
            createCameraCapturer(Camera1Enumerator(true))
        }
    }

    *//**
     * Webrtc provides us a very easy way to use Camera and Camera2 API depending on the support.
     * On supported devices we can use either of the APIs
     * @param enumerator
     *//*
    private fun createCameraCapturer(enumerator: CameraEnumerator): CameraVideoCapturer {
        return enumerator.run {
            //find front camera
            deviceNames.find { name -> isFrontFacing(name) }
                ?.let {
                    createCapturer(it, null)
                }
            //if can't find any use others
                ?: deviceNames.find { name -> !isFrontFacing(name) }
                    ?.let {
                        createCapturer(it, null)
                    }
                ?: throw IllegalStateException("Couldn't find available camera")
        }
    }*/
}
