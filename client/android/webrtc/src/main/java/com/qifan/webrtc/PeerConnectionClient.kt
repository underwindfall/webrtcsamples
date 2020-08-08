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
package com.qifan.webrtc

import android.content.Context
import com.qifan.webrtc.constants.FPS
import com.qifan.webrtc.constants.LOCAL_STREAM_ID
import com.qifan.webrtc.constants.VIDEO_RESOLUTION_HEIGHT
import com.qifan.webrtc.constants.VIDEO_RESOLUTION_WIDTH
import com.qifan.webrtc.extensions.common.WeakReferenceProvider
import com.qifan.webrtc.extensions.rtc.* // ktlint-disable no-wildcard-imports
import com.qifan.webrtc.model.MediaViewRender
import com.qifan.webrtc.model.RTCConstraints
import com.qifan.webrtc.model.toConstraints
import org.webrtc.* // ktlint-disable no-wildcard-imports

class PeerConnectionClient(context: Context) {
    private var context: Context by WeakReferenceProvider()

    private val rootEglBase: EglBase by lazy { buildRootEglBase() }

    private val defaultStunServer: PeerConnection.IceServer by lazy {
        PeerConnection.IceServer
            .builder("stun:stun.l.google.com:19302")
            .createIceServer()
    }

    private var peerConnection: PeerConnection? = null

    private var localViewRenderer: SurfaceViewRenderer? = null

    private var remoteViewRenderer: SurfaceViewRenderer? = null

    private var mediaConstraints: MediaConstraints = RTCConstraints().toConstraints()

    private var peerConnectionFactory: PeerConnectionFactory? = null
    private val videoCapturer: CameraVideoCapturer by lazy { context.buildVideoCapturer() }

    private val surfaceTextureHelper: SurfaceTextureHelper by lazy {
        createSurfaceTexture(sharedContext = rootEglBase.eglBaseContext)
    }
    private var localVideoSource: VideoSource? = null
    private var localVideoTrack: VideoTrack? = null

    //    private var localVideoTrack: VideoTrack? = null
    private var localAudioSource: AudioSource? = null

    //    private var localAudioTrack: AudioTrack? = null
    private var remoteVideoTrack: VideoTrack? = null
    private var remoteAudioTrack: AudioTrack? = null
    private var remoteMediaStream: MediaStream? = null

    init {
        this.context = context
        peerConnectionFactory = createPeerConnectionFactory()
    }

    private fun createPeerConnectionFactory(): PeerConnectionFactory {
        val options = PeerConnectionFactory.Options()
        val decoderVideoFactory = DefaultVideoDecoderFactory(rootEglBase.eglBaseContext)
        val encoderFactory = DefaultVideoEncoderFactory(rootEglBase.eglBaseContext, true, true)
        val audioDeviceFactory = createJavaAudioDevice(context)
        // load ndk webrtc native library into process
        PeerConnectionFactory.initialize(
            PeerConnectionFactory.InitializationOptions
                .builder(context)
                .setEnableInternalTracer(true)
                .createInitializationOptions()
        )
        // configure connection factory
        return PeerConnectionFactory
            .builder()
            .setOptions(options)
            .setVideoDecoderFactory(decoderVideoFactory)
            .setVideoEncoderFactory(encoderFactory)
            .setAudioDeviceModule(audioDeviceFactory)
            .createPeerConnectionFactory()
    }


    /**
     * method to setup [SurfaceViewRenderer] provided by webrtc libray
     * that does the rendering of webrtc frames for us
     * @param view rendering of webrtc frames
     */
    private fun initSurfaceView(view: SurfaceViewRenderer?) {
        view?.initializeSurfaceView(rootEglBase)
    }

    private fun setSurfaceViewRender(
        localView: SurfaceViewRenderer,
        remoteView: SurfaceViewRenderer
    ) {

        localViewRenderer = localView
        remoteViewRenderer = remoteView
        initSurfaceView(localViewRenderer)
        initSurfaceView(remoteViewRenderer)
    }

    internal fun createLocalPeer(
        observer: PeerConnection.Observer
    ) {
        with(PeerConnection.RTCConfiguration(listOf(defaultStunServer))) {
            peerConnection = peerConnectionFactory?.createPeerConnection(this, observer)
        }
    }

    internal fun setupLocalVideoTrack(mediaViewRender: MediaViewRender) {
        ui {
            setSurfaceViewRender(
                mediaViewRender.localViewRender,
                mediaViewRender.remoteViewRenderer
            )
        }
        localVideoSource = createVideoSource(peerConnectionFactory!!, videoCapturer.isScreencast)
        localVideoTrack =
            createVideoTrack(peerConnectionFactory!!, videoSource = localVideoSource!!)
        videoCapturer.initialize(
            surfaceTextureHelper,
            context,
            localVideoSource?.capturerObserver
        )
        videoCapturer.startCapture(VIDEO_RESOLUTION_WIDTH, VIDEO_RESOLUTION_HEIGHT, FPS)
        localVideoTrack?.addSink(localViewRenderer)
    }

    internal fun setupLocalMediaStream() {
        val localStream = peerConnectionFactory?.createLocalMediaStream(LOCAL_STREAM_ID)
        localAudioSource = createAudioSource(peerConnectionFactory!!)
        val localAudioTrack =
            createAudioTrack(peerConnectionFactory!!, audioSource = localAudioSource!!)
        localStream?.addTrack(localVideoTrack)
        localStream?.addTrack(localAudioTrack)
        peerConnection?.addStream(localStream)
    }

    internal fun setRemoteStream(mediaStream: MediaStream?) {
        this.remoteMediaStream = mediaStream
        remoteVideoTrack = mediaStream?.videoTracks?.firstOrNull()
        remoteAudioTrack = mediaStream?.audioTracks?.firstOrNull()
        remoteVideoTrack?.addSink(remoteViewRenderer)
    }

    internal fun createOffer(sdpObserver: SdpObserver) {
        peerConnection?.createOffer(sdpObserver, mediaConstraints)
    }

    internal fun setLocalSdp(sdpObserver: SdpObserver, sdp: SessionDescription?) {
        peerConnection?.setLocalDescription(sdpObserver, sdp)
    }

    internal fun setRemoteSdp(sdpObserver: SdpObserver, sdp: SessionDescription?) {
        peerConnection?.setRemoteDescription(sdpObserver, sdp)
    }

    internal fun createAnswer(sdpObserver: SdpObserver) {
        peerConnection?.createAnswer(sdpObserver, mediaConstraints)
    }

    internal fun addIceCandidate(iceCandidate: IceCandidate) {
        peerConnection?.addIceCandidate(iceCandidate)
    }

    internal fun dispose() {
        localViewRenderer?.release()
        localViewRenderer = null
        remoteViewRenderer?.release()
        remoteViewRenderer = null
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
        rootEglBase.release()
        peerConnectionFactory?.dispose()
        peerConnectionFactory = null
        PeerConnectionFactory.stopInternalTracingCapture()
        PeerConnectionFactory.shutdownInternalTracer()
    }
}
