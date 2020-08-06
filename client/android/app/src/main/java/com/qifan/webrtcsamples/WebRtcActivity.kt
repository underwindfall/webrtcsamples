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
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.qifan.webrtcsamples.constants.FPS
import com.qifan.webrtcsamples.constants.LOCAL_STREAM_ID
import com.qifan.webrtcsamples.constants.VIDEO_RESOLUTION_HEIGHT
import com.qifan.webrtcsamples.constants.VIDEO_RESOLUTION_WIDTH
import com.qifan.webrtcsamples.databinding.ActivityWebRtcBinding
import com.qifan.webrtcsamples.extensions.common.debug
import com.qifan.webrtcsamples.extensions.common.error
import com.qifan.webrtcsamples.extensions.common.warn
import com.qifan.webrtcsamples.extensions.rtc.*// ktlint-disable no-wildcard-imports
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONException
import org.json.JSONObject
import org.webrtc.*// ktlint-disable no-wildcard-imports
import org.webrtc.audio.JavaAudioDeviceModule
import java.net.URISyntaxException
import kotlin.properties.Delegates.notNull

class WebRtcActivity : AppCompatActivity(), PeerConnection.Observer {
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
    private var enabledAudio = true

    private var socket: Socket by notNull()
    private val defaultStunServer: PeerConnection.IceServer by lazy {
        PeerConnection.IceServer
            .builder("stun:stun.l.google.com:19302")
            .createIceServer()
    }

    private val rootEglBase: EglBase by lazy { EglBase.create() }
    private val videoCapturer: CameraVideoCapturer by lazy { buildVideoCapturer() }
    private var peerConnectionFactory: PeerConnectionFactory? = null

    private val mediaConstraints: MediaConstraints by lazy {
        MediaConstraints().apply {
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"))
        }
    }

    //Since we use a real local server to achieve signaling so there is only one peer connection
    private var peerConnection: PeerConnection? = null

    private val surfaceTextureHelper: SurfaceTextureHelper by lazy {
        createSurfaceTexture(sharedContext = rootEglBase.eglBaseContext)
    }
    private var localVideoSource: VideoSource? = null
    private var localVideoTrack: VideoTrack? = null
    private var localAudioSource: AudioSource? = null
    private var localAudioTrack: AudioTrack? = null

    companion object {
        private const val ROOM = "ROOM"
        private const val IPADDRESS = "IPADDRESS"
        private const val TYPE_CREATE_OFFER = "create_offer"
        private const val TYPE_SEND_OFFER = "send_offer"
        private const val TYPE_SEND_ANSWER = "send_answer"
        private const val TYPE_SEND_CANDIDATE = "send_candidate"

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
        handUpBtn.setOnClickListener { cleanupRtc() }
        muteBtn.setOnClickListener {
            enabledAudio = !enabledAudio
            muteBtn.text = if (enabledAudio) "UnMute" else "Mute"
            localAudioTrack?.setEnabled(enabledAudio)
        }
        parseIntents()
        initializeWebRtc()
    }

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
    }

    private fun parseIntents() {
        val room = intent.getStringExtra(ROOM)
        val ip = intent.getStringExtra(IPADDRESS)
        require(room != null && ip != null) { "Must have essential parameter to initialize call" }
        roomId = room
        ipAddr = ip
    }

    private fun initializeWebRtc() {
        setupSignaling()
        setupSurfaceView()
        buildPeerConnectionFactory()
        setupLocalVideoTrack()
        setupPeerConnection()
        setupLocalMediaStream()
    }

    /**
     * This step is to register all events listner to do different solutions
     */
    private fun setupSignaling() {
        val messagePrefix = "connection to signaling server :"
        try {
            socket = IO.socket(ipAddr)
            socket
                .on(Socket.EVENT_CONNECT) {
                    debug("$messagePrefix connect")
                    socket.emit("create or join", roomId)
                }
                .on("ipaddr") {
                    debug("$messagePrefix ipaddr")
                }
                .on("created") {
                    debug("$messagePrefix created")
                    isInitiator = true
                }
                .on("full") {
                    debug("$messagePrefix full")
                }
                .on("join") {
                    debug("$messagePrefix join")
                    debug("$messagePrefix Another peer made a requst to join room")
                    debug("$messagePrefix This peer is the intiator of room")
                    isChannelReady = true
                  maybeStart()
                }
                .on("joined") {
                    debug("$messagePrefix joined")
                    isChannelReady = true
                }
                .on("log") { args ->
                    args.forEach { debug("$messagePrefix $it") }
                }
                .on("message") { args ->
                    debug("$messagePrefix message")
                    try {

                        val message = args.firstOrNull() as JSONObject
                        when {
//                            message.getString("type") == TYPE_CREATE_OFFER -> {
//                                maybeStart()
//                            }
                            message.getString("type") == TYPE_SEND_OFFER -> {
                                debug("$messagePrefix type offer isInitiator======>$isInitiator isStarted ======>$isStarted")
                                if (!isInitiator && !isStarted) {
                                    maybeStart()
                                }
                                peerConnection?.setRemoteDescription(
                                    SimpleObserver(
                                        SimpleObserver.Source.RECEIVER_REMOTE
                                    ),
                                    SessionDescription(
                                        SessionDescription.Type.OFFER,
                                        message.getString("sdp")
                                    )
                                )
                                createAnswer()
                            }
                            message.getString("type") == TYPE_SEND_ANSWER && isStarted -> {
                                debug("$messagePrefix type answer")
                                peerConnection?.setRemoteDescription(
                                    SimpleObserver(
                                        SimpleObserver.Source.CALL_REMOTE
                                    ),
                                    SessionDescription(
                                        SessionDescription.Type.ANSWER,
                                        message.getString("sdp")
                                    )
                                )
                            }
                            message.getString("type") == TYPE_SEND_CANDIDATE && isStarted -> {
                                debug("$messagePrefix receiving candidates")
                                val candidate = IceCandidate(
                                    message.getString("id"),
                                    message.getInt("label"),
                                    message.getString(
                                        "candidate"
                                    )
                                )
                                peerConnection?.addIceCandidate(candidate)
                            }
                        }
                    } catch (e: JSONException) {

                    }
                }
                .on("close") {
                    Log.d("Qifan", "NEVER")
                    cleanupRtc()
                }
                .on(Socket.EVENT_DISCONNECT) {
                    debug("$messagePrefix disconnect")
                }
            socket.connect()
        } catch (e: URISyntaxException) {
            e.printStackTrace()
        }
    }

    private fun maybeStart() {
        debug("maybeStart======> isStarted=====$isStarted  isChannelReady=====$isChannelReady")
        if (!isStarted && isChannelReady) {
            isStarted = true
            if (isInitiator) {
                createOffer()
            }
        }
    }

    private fun createOffer() {
        peerConnection?.createOffer(
            sdpObserver(
                SimpleObserver.Source.LOCAL_OFFER
            ) {
                onCreateSuccess { sdp ->
                    debug(
                        "${SimpleObserver.Source.LOCAL_OFFER.value} ====> onCreateSuccess $sdp"
                    )
                    peerConnection?.setLocalDescription(
                        SimpleObserver(
                            SimpleObserver.Source.CALL_LOCAL
                        ),
                        sdp
                    )
                    with(JSONObject()) {
                        put("type", TYPE_SEND_OFFER)
                        put("sdp", sdp?.description)
                        sendMessage(this)
                    }
                }
            }, mediaConstraints
        )
    }

    private fun createAnswer() {
        debug("createAnswer")
        peerConnection?.createAnswer(
            sdpObserver(
                SimpleObserver.Source.REMOTE_ANSWER
            ) {
                onCreateSuccess { sdp ->
                    peerConnection?.setLocalDescription(
                        SimpleObserver(
                            SimpleObserver.Source.CALL_REMOTE
                        ),
                        sdp
                    )
                    debug(

                        "${SimpleObserver.Source.REMOTE_ANSWER.value}====> onCreateSuccess $sdp"
                    )
                    with(JSONObject()) {
                        put("type", TYPE_SEND_ANSWER)
                        put("sdp", sdp?.description)
                    }.apply {
                        sendMessage(this)
                    }
                }
            }, mediaConstraints
        )
    }

    private fun setupSurfaceView() {
        initSurfaceView(localViewRender)
        initSurfaceView(remoteViewRender)
    }

    private fun setupLocalVideoTrack() {
        createVideoSourceAndTrackAttachToView(videoCapturer)
    }

    /**
     * This function is try to initialize [PeerConnection]
     * We pass in a PeerConnection#Observer instance in the factory method,
     * which is notified when a ICE candidate is generated (we need to send that to the other peer).
     * The observer is also notified when the remote MediaStream is available.
     * we will attach a renderer to it just like the local MediaStream
     */
    private fun setupPeerConnection() {
        // This time we will add a real ice server to build connection between two peers
        with(PeerConnection.RTCConfiguration(listOf(defaultStunServer))) {
            peerConnection =
                peerConnectionFactory?.createPeerConnection(
                    this,
                    this@WebRtcActivity
                )
        }
    }

    /**
     *  use localVideoTrack && localAudioTrack to attach local stream
     */
    private fun setupLocalMediaStream() {
        val localStream = peerConnectionFactory?.createLocalMediaStream(LOCAL_STREAM_ID)
        localAudioSource = createAudioSource(peerConnectionFactory!!)
        localAudioTrack =
            createAudioTrack(peerConnectionFactory!!, audioSource = localAudioSource!!)
        localStream?.addTrack(localVideoTrack)
        localStream?.addTrack(localAudioTrack)
        peerConnection?.addStream(localStream)
    }

    /**
     * create a VideoSource from the PeerConnectionFactory and VideoTrack then
     * attach our SurfaceViewRenderer to the VideoTrack
     * @param videoCapturer camera capture we got before
     */
    private fun createVideoSourceAndTrackAttachToView(videoCapturer: CameraVideoCapturer) {
        // use CameraVideoCapturer as local video source
        localVideoSource = createVideoSource(peerConnectionFactory!!, videoCapturer.isScreencast)
        localVideoTrack =
            createVideoTrack(peerConnectionFactory!!, videoSource = localVideoSource!!)
        videoCapturer.initialize(
            surfaceTextureHelper,
            applicationContext,
            localVideoSource?.capturerObserver
        )
        videoCapturer.startCapture(VIDEO_RESOLUTION_WIDTH, VIDEO_RESOLUTION_HEIGHT, FPS)
        localVideoTrack?.addSink(localViewRender)
    }

    /**
     * method to setup [SurfaceViewRenderer] provided by webrtc libray
     * that does the rendering of webrtc frames for us
     * @param view rendering of webrtc frames
     */
    private fun initSurfaceView(view: SurfaceViewRenderer) {
        view.initializeSurfaceView(rootEglBase)
    }


    /**
     * PeerConnectionFactory is used to create PeerConnection, MediaStream and
     * MediaStreamTrack objects
     */
    private fun buildPeerConnectionFactory() {

        val options = PeerConnectionFactory.Options()
        val decoderVideoFactory = DefaultVideoDecoderFactory(rootEglBase.eglBaseContext)
        val encoderFactory = DefaultVideoEncoderFactory(rootEglBase.eglBaseContext, true, true)
        val audioDeviceModule = JavaAudioDeviceModule.builder(applicationContext)
            .setUseHardwareAcousticEchoCanceler(true)
            .setUseHardwareNoiseSuppressor(true)
            .createAudioDeviceModule()
        //load ndk webrtc native library into process
        PeerConnectionFactory.initialize(
            PeerConnectionFactory.InitializationOptions
                .builder(applicationContext)
                .setEnableInternalTracer(false)
                .createInitializationOptions()
        )
        //configure connection factory
        peerConnectionFactory = PeerConnectionFactory
            .builder()
            .setOptions(options)
            .setAudioDeviceModule(audioDeviceModule)
            .setVideoDecoderFactory(decoderVideoFactory)
            .setVideoEncoderFactory(encoderFactory)
            .createPeerConnectionFactory()
    }


    override fun onIceCandidate(iceCandidate: IceCandidate) {
        debug("onIceCandidate $iceCandidate")

        with(JSONObject()) {
            put("type", TYPE_SEND_CANDIDATE)
            put("label", iceCandidate.sdpMLineIndex)
            put("id", iceCandidate.sdpMid)
            put("candidate", iceCandidate.sdp)
        }.apply {
            debug("onIceCandidate: sending candidate $this")
            sendMessage(this)
        }
    }

    override fun onDataChannel(p0: DataChannel?) {
        warn("onDataChannel")
    }

    override fun onIceConnectionReceivingChange(p0: Boolean) {
        warn("onIceConnectionReceivingChange")
    }

    override fun onIceConnectionChange(p0: PeerConnection.IceConnectionState?) {
        warn("onIceConnectionChange")
    }

    override fun onIceGatheringChange(p0: PeerConnection.IceGatheringState?) {
        warn("onIceGatheringChange")
    }

    override fun onAddStream(mediaStream: MediaStream?) {
        debug("onAddStream mediaStream size is ${mediaStream?.videoTracks?.size}")
        val remoteVideoTrack = mediaStream?.videoTracks?.firstOrNull()
        remoteVideoTrack?.addSink(remoteViewRender)
    }

    override fun onSignalingChange(p0: PeerConnection.SignalingState?) {
        warn("onSignalingChange")
    }

    override fun onIceCandidatesRemoved(p0: Array<out IceCandidate>?) {
        warn("onIceCandidatesRemoved")
    }

    override fun onRemoveStream(p0: MediaStream?) {
        warn("onRemoveStream")
    }

    override fun onRenegotiationNeeded() {
        warn("onRenegotiationNeeded")
    }

    override fun onAddTrack(p0: RtpReceiver?, p1: Array<out MediaStream>?) {
        warn("onAddTrack")
    }


    /**
     * benefit of socket io to deal with socket
     */
    private fun sendMessage(message: Any) {
        socket.emit("message", message)
    }


    /**
     * release relevant sources to avoid memory leak
     */
    private fun cleanupRtc() {
        socket.emit("leave")
        socket.off()
        socket.disconnect()
        localViewRender.release()
        remoteViewRender.release()


        try {
            videoCapturer.stopCapture()
        } catch (e: InterruptedException) {
            error("error stop video capturer")
        }
        videoCapturer.dispose()
        surfaceTextureHelper.dispose()

        localAudioSource?.dispose()
        localAudioSource = null

        localVideoSource?.dispose()
        localVideoSource = null

        peerConnection?.dispose()
        peerConnection = null
        localAudioTrack = null
        localVideoTrack = null
        peerConnection = null
        rootEglBase.release()

        peerConnectionFactory?.dispose()
        peerConnectionFactory = null
        PeerConnectionFactory.stopInternalTracingCapture()
        PeerConnectionFactory.shutdownInternalTracer()

        finish()
    }

}
