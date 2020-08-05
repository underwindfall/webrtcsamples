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
import com.qifan.webrtc.extensions.rtc.async
import org.json.JSONObject
import org.webrtc.*
import kotlin.properties.Delegates.notNull

class RTCManager(
    private val context: Context,
    private val url: String,
    private val roomId: String
) : SignalingClient.SignalEventListener,
    PeerConnection.Observer,
    SdpObserver {
    private var peerConnectionClient: PeerConnectionClient by notNull()

    private var signalingClient: SignalingClient by notNull()

    private var localSdp: SessionDescription by notNull()

    private var isInitiator = false
    private var isChannelReady = false


    internal fun initializeRTC() {
        initSignalingServer()
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
            peerConnectionClient.createLocalPeer(this)
        }
    }

    private fun createLocalOffer() {
        async {
            peerConnectionClient.createLocalOffer(this)
        }
    }

    override fun onConnectSignaling() {
        initPeerConnection()
    }

    override fun onCreatedRoom() {
        isInitiator = true
    }

    override fun onRemoteUserJoined() {
        isChannelReady = true
        if (isInitiator) {
            peerConnectionClient.createLocalOffer(this)
        }
    }

    override fun onReceiveMessage(json: JSONObject) {
        when (json.getString("type")) {
            "send_offer" -> {
                peerConnectionClient.setRemoteSdp(
                    this,
                    SessionDescription(SessionDescription.Type.OFFER, json.getString("sdp"))
                )
            }
        }

    }

    override fun onDisConnectRoom() {

    }

    //PeerConnection Observer
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

    //SDP Observer
    override fun onSetFailure(p0: String) {

    }

    override fun onSetSuccess() {
        if (isInitiator) {
            JSONObject().apply {
                put("type", "send_offer")
                put("sdp", localSdp)
            }.also {
                signalingClient.sendMessage(it)
            }
        } else {
            peerConnectionClient.createAnswer(this)
        }

    }

    override fun onCreateSuccess(sdp: SessionDescription) {
        localSdp = sdp
        if (isInitiator) {
            peerConnectionClient.setLocalSdp(this, localSdp)
        } else {
            peerConnectionClient.
        }
    }

    override fun onCreateFailure(p0: String?) {

    }

}
