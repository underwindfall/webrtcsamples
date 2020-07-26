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
import com.qifan.webrtc.extensions.common.WeakReferenceProvider
import com.qifan.webrtc.extensions.rtc.* // ktlint-disable no-wildcard-imports
import org.webrtc.* // ktlint-disable no-wildcard-imports
import kotlin.properties.Delegates.notNull

class PeerConnectionClient(context: Context) {
    private var context: Context by WeakReferenceProvider()

    private val rootEglBase: EglBase by lazy { buildRootEglBase() }

    private val stunServer: PeerConnection.IceServer by lazy {
        PeerConnection.IceServer
            .builder("stun:stun1.l.google.com:19302")
            .createIceServer()
    }

    private var localPeerConnection: PeerConnection? = null

    private var localViewRenderer: SurfaceViewRenderer by WeakReferenceProvider()

    private var remoteViewRenderer: SurfaceViewRenderer by WeakReferenceProvider()

    private var mediaConstraints: MediaConstraints = MediaConstraints().apply {
        mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
        mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"))
    }

    private var peerConnectionFactory: PeerConnectionFactory by notNull()
    private val videoCapturer: CameraVideoCapturer by lazy { context.buildVideoCapturer() }
    private val surfaceTextureHelper: SurfaceTextureHelper by lazy {
        createSurfaceTexture(sharedContext = rootEglBase.eglBaseContext)
    }
    private val videoSource: VideoSource by lazy {
        createVideoSource(peerConnectionFactory, videoCapturer.isScreencast)
    }
    private val videoTrack: VideoTrack by lazy {
        createVideoTrack(peerConnectionFactory, videoSource = videoSource)
    }
    private var remoteVideoTrack: VideoTrack? = null
    private val audioSource: AudioSource by lazy {
        createAudioSource(peerConnectionFactory)
    }
    private val audioTrack: AudioTrack by lazy {
        createAudioTrack(peerConnectionFactory, audioSource = audioSource)
    }

    init {
        this.context = context
        peerConnectionFactory = createPeerConnectionFactory()
    }

    internal fun buildRTCPeerClient(
        localViewRenderer: SurfaceViewRenderer,
        remoteViewRenderer: SurfaceViewRenderer
    ) {
        this.localViewRenderer = localViewRenderer
        this.remoteViewRenderer = remoteViewRenderer
    }


    internal fun createLocalPeer(observer: PeerConnection.Observer) {
        PeerConnection.RTCConfiguration(listOf(stunServer)).apply {
            continualGatheringPolicy = PeerConnection.ContinualGatheringPolicy.GATHER_CONTINUALLY
        }.also { config ->
            localPeerConnection = peerConnectionFactory.createPeerConnection(config, observer)
        }
    }

    internal fun createLocalOffer(sdpObserver: SdpObserver) {
        localPeerConnection?.createOffer(sdpObserver, mediaConstraints)
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
}
