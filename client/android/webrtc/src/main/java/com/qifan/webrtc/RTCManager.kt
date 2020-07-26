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
import org.webrtc.*
import kotlin.properties.Delegates
import kotlin.properties.Delegates.notNull

class RTCManager(
    context: Context,
    private val url: String,
    private val roomId: String
) : SignalingClient.SignalEventListener,
    PeerConnection.Observer,
    SdpObserver {
    private var context: Context by WeakReferenceProvider()

    private var signalingState: SignalingState by Delegates.observable(SignalingState.IDLE) { property, oldValue, newValue ->
        debug(message = "RTC Manager siganling state change $property [$oldValue====>$newValue]")
        updateState(newValue)
    }

    private var peerConnectionClient: PeerConnectionClient by notNull()

    private var signalingClient: SignalingClient by notNull()

    private var isInitiator = false

    init {
        this.context = if (context is Application) context else context.applicationContext
    }

    private fun updateState(state: SignalingState) {
        when (state) {
            SignalingState.IDLE -> initSignalingServer()
            SignalingState.INIT_SIGNALING -> initPeerConnection()
            SignalingState.INIT_PEER_CONNECTION -> createLocalPeer()
            SignalingState.CREATE_OFFER -> createLocalOffer()
        }
    }

    private fun initSignalingServer() {
        async {
            signalingClient = SignalingClient()
            signalingClient.initialize(url = url, roomId = roomId, listener = this)
        }
    }

    private fun initPeerConnection() {
        async {
            peerConnectionClient = PeerConnectionClient(context)
            signalingState = SignalingState.INIT_PEER_CONNECTION
        }
    }

    private fun createLocalPeer() {
        async {
            peerConnectionClient.createLocalPeer(this)
        }
    }

    private fun createLocalOffer() {
        async {
            peerConnectionClient.createLocalOffer(this)
        }
    }

    override fun onConnectSignaling() {
        signalingState = SignalingState.INIT_SIGNALING
    }

    override fun onCreatedRoom() {
        isInitiator = true
    }

    override fun onRemoteUserJoined() {

    }

    override fun onSendMessage(json: JSONObject) {

    }

    override fun onDisConnectRoom() {

    }

    override fun onIceCandidate(p0: IceCandidate?) {

    }

    override fun onDataChannel(p0: DataChannel?) {

    }

    override fun onIceConnectionReceivingChange(p0: Boolean) {

    }

    override fun onIceConnectionChange(p0: PeerConnection.IceConnectionState?) {

    }

    override fun onIceGatheringChange(p0: PeerConnection.IceGatheringState?) {

    }

    override fun onAddStream(p0: MediaStream?) {

    }

    override fun onSignalingChange(p0: PeerConnection.SignalingState?) {

    }

    override fun onIceCandidatesRemoved(p0: Array<out IceCandidate>?) {

    }

    override fun onRemoveStream(p0: MediaStream?) {

    }

    override fun onRenegotiationNeeded() {

    }

    override fun onAddTrack(p0: RtpReceiver?, p1: Array<out MediaStream>?) {

    }

    override fun onSetFailure(p0: String?) {

    }

    override fun onSetSuccess() {

    }

    override fun onCreateSuccess(p0: SessionDescription?) {

    }

    override fun onCreateFailure(p0: String?) {

    }

    private enum class SignalingState {
        IDLE,
        INIT_SIGNALING,
        INIT_PEER_CONNECTION,
        CREATE_OFFER
    }
}
