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

import android.app.Application
import android.content.Context
import com.qifan.webrtc.extensions.common.WeakReferenceProvider
import com.qifan.webrtc.extensions.common.debug
import com.qifan.webrtc.extensions.rtc.async
import org.json.JSONObject
import kotlin.properties.Delegates

class RTCManager(
    context: Context,
    private val url: String,
    private val roomId: String
) : SignalingClient.SignalEventListener {
    private var context: Context by WeakReferenceProvider()

    private var signalingState: SignalingState by Delegates.observable(SignalingState.IDLE) { property, oldValue, newValue ->
        debug(message = "RTC Manager siganling state change $property [$oldValue====>$newValue]")
        updateState(newValue)
    }

    private lateinit var peerConnectionClient: PeerConnectionClient

    private lateinit var signalingClient: SignalingClient

    init {
        this.context = if (context is Application) context else context.applicationContext
    }

    private fun updateState(state: SignalingState) {
        when (state) {
            SignalingState.IDLE -> initSignalingServer()
            SignalingState.INIT_SIGNALING -> initPeerConnection()
//            SignalingState.INIT_PEER_CONNECTION -> createLocalPeer()
        }
    }

    private fun initSignalingServer() {
        signalingClient = SignalingClient()
        signalingClient.initialize(url = url, roomId = roomId)
    }

    private fun initPeerConnection() {
        async {
            peerConnectionClient = PeerConnectionClient(context)
            updateState(SignalingState.INIT_PEER_CONNECTION)
        }
    }

    override fun onConnectedRoom() {
    }

    override fun onRemoteUserJoined() {
    }

    override fun onSendMessage(json: JSONObject) {
    }

    override fun onDisConnectRoom() {
    }

    private enum class SignalingState {
        IDLE,
        INIT_SIGNALING,
        INIT_PEER_CONNECTION
    }
}
