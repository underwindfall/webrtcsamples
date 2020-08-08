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
import com.qifan.webrtc.extensions.common.debug
import com.qifan.webrtc.extensions.common.warn
import com.qifan.webrtc.extensions.rtc.SimpleObserver
import com.qifan.webrtc.extensions.rtc.async
import com.qifan.webrtc.extensions.rtc.sdpObserver
import com.qifan.webrtc.model.MediaViewRender
import org.webrtc.* // ktlint-disable no-wildcard-imports
import kotlin.properties.Delegates.notNull

class RTCManager(private val context: Context) :
    SignalingSocketIOClient.Listener,
    PeerConnection.Observer {
    private var signalClientClient: SignalingSocketIOClient? = null
    private var peerConnectionClient: PeerConnectionClient? = null
    private var managerListener: Listener? = null
    private var isInitiator = false
    private var url: String by notNull()
    private var roomName: String by notNull()

    private var rtcEvent: RTCEvent = RTCEvent.Idle
        set(value) {
            debug("RTCManager State [$field==========>$value]")
            observeRtcEvents(value)
            field = value
        }

    private fun observeRtcEvents(rtcEvent: RTCEvent) {
        async {
            when (rtcEvent) {
                is RTCEvent.Idle -> debug("RTC Manager Reset Initial State")
                is RTCEvent.Connecting -> initialize()
                is RTCEvent.ParticipantEvent.CreateOffer -> createLocalOffer()
                is RTCEvent.ParticipantEvent.SendOfferToParticipant -> signalClientClient?.sendOffer(
                    rtcEvent.sdp
                )
                is RTCEvent.ParticipantEvent.SetRemoteSdp -> createRemoteAnswer(rtcEvent.sdp)
                is RTCEvent.ParticipantEvent.SendAnswer -> signalClientClient?.sendAnswer(rtcEvent.sdp)
                is RTCEvent.ParticipantEvent.SetLocalSdp -> setLocalSdp(rtcEvent.sdp)
            }
        }
    }

    fun call(identity: String, roomId: String, listener: Listener) {
        url = identity
        roomName = roomId
        signalClientClient = SignalingSocketIOClient()
        managerListener = listener
        rtcEvent = RTCEvent.Connecting
    }

    fun hangup() {
        async {
            signalClientClient?.disconnect()
            onClose()
        }
    }


    override fun onRoomCreated() {
        debug("Signaling Socket Created")
        isInitiator = true
    }

    override fun onParticipantConnected() {
        rtcEvent = RTCEvent.ParticipantEvent.CreateOffer
    }

    override fun onParticipantReceiveOffer(sdp: SessionDescription) {
        rtcEvent = RTCEvent.ParticipantEvent.SetRemoteSdp(sdp)
    }

    override fun onRoomReceiveAnswer(sdp: SessionDescription) {
        rtcEvent = RTCEvent.ParticipantEvent.SetLocalSdp(sdp)
    }

    override fun onExchangeCandidate(iceCandidate: IceCandidate) {
        async {
            peerConnectionClient?.addIceCandidate(iceCandidate)
        }
    }

    override fun onClose() {
        signalClientClient = null
        peerConnectionClient?.dispose()
        peerConnectionClient = null
        managerListener?.hangup()
        managerListener = null
    }

    override fun onIceCandidate(iceCandidate: IceCandidate) {
        async {
            signalClientClient?.sendIceCandidate(iceCandidate)
        }
    }

    override fun onDataChannel(p0: DataChannel?) {
        warn("onDataChannel")
    }

    override fun onIceConnectionReceivingChange(p0: Boolean) {
        warn("onIceConnectionReceivingChange")
    }

    override fun onIceConnectionChange(p0: PeerConnection.IceConnectionState?) {
        warn("onIceConnectionChange $p0")
    }

    override fun onIceGatheringChange(p0: PeerConnection.IceGatheringState?) {
        warn("onIceGatheringChange")
    }

    override fun onAddStream(mediaStream: MediaStream?) {
        debug("onAddStream mediaStream size is ${mediaStream?.videoTracks?.size}")
//        async {
        peerConnectionClient?.setRemoteStream(mediaStream)
//        }
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

    private fun initialize() {
//        async {
        signalClientClient?.connect(url, roomName, this)
        managerListener?.apply {
            peerConnectionClient = PeerConnectionClient(context)
            peerConnectionClient?.setupLocalVideoTrack(setupLocalMedia())
        }
        createLocalPeer()
        createLocalMediaStream()
//        }
    }

    private fun createLocalPeer() {
        peerConnectionClient?.createLocalPeer(this)
    }

    private fun createLocalMediaStream() {
        peerConnectionClient?.setupLocalMediaStream()
    }

    private fun createLocalOffer() {
//        async {
        peerConnectionClient?.createOffer(sdpObserver(SimpleObserver.Source.LOCAL_OFFER) {
            onCreateSuccess { sdp ->
                peerConnectionClient?.setLocalSdp(
                    SimpleObserver(
                        SimpleObserver.Source.CALL_LOCAL
                    ),
                    sdp
                )
                rtcEvent = RTCEvent.ParticipantEvent.SendOfferToParticipant(sdp)
            }
        })
//        }
    }


    private fun createRemoteAnswer(remoteSdp: SessionDescription?) {
//        async {
        peerConnectionClient?.setRemoteSdp(
            SimpleObserver(
                SimpleObserver.Source.RECEIVER_REMOTE
            ), remoteSdp
        )
        peerConnectionClient?.createAnswer(sdpObserver(
            SimpleObserver.Source.REMOTE_ANSWER
        ) {
            onCreateSuccess { sdp ->
                peerConnectionClient?.setLocalSdp(
                    SimpleObserver(
                        SimpleObserver.Source.CALL_REMOTE
                    ),
                    sdp
                )
                rtcEvent = RTCEvent.ParticipantEvent.SendAnswer(sdp)
            }
        })
//        }
    }


    private fun setLocalSdp(sdp: SessionDescription?) {
//        async {
        peerConnectionClient?.setRemoteSdp(
            SimpleObserver(
                SimpleObserver.Source.CALL_REMOTE
            ), sdp
        )
//        }
    }


    interface Listener {
        fun setupLocalMedia(): MediaViewRender
        fun hangup()
    }
}
