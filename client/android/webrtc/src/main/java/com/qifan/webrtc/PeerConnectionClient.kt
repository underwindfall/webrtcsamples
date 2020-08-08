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
import com.qifan.webrtc.extensions.rtc.buildRootEglBase
import com.qifan.webrtc.extensions.rtc.buildVideoCapturer
import com.qifan.webrtc.extensions.rtc.createJavaAudioDevice
import com.qifan.webrtc.extensions.rtc.createSurfaceTexture
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

    private var mediaConstraints: MediaConstraints = MediaConstraints().apply {
        mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
        mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"))
    }

    private var peerConnectionFactory: PeerConnectionFactory? = null
    private val videoCapturer: CameraVideoCapturer by lazy { context.buildVideoCapturer() }

    private val surfaceTextureHelper: SurfaceTextureHelper by lazy {
        createSurfaceTexture(sharedContext = rootEglBase.eglBaseContext)
    }
    private val videoSource: VideoSource? = null
    private val videoTrack: VideoTrack? = null
    private val audioSource: AudioSource? = null
    private val audioTrack: AudioTrack? = null

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

    internal fun setSurfaceView(
        localViewRenderer: SurfaceViewRenderer,
        remoteViewRenderer: SurfaceViewRenderer
    ) {
        this.localViewRenderer = localViewRenderer
        this.remoteViewRenderer = remoteViewRenderer
    }
}
