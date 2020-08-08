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
import com.qifan.webrtc.extensions.rtc.async
import org.webrtc.* // ktlint-disable no-wildcard-imports
import kotlin.properties.Delegates.notNull

class RTCManager(private val context: Context) : SignalingSocketIOClient.Listener {
    private var signalClientClient: SignalingSocketIOClient? = null
    private var peerConnectionClient: PeerConnectionClient? = null
    private var url: String by notNull()
    private var roomName: String by notNull()

    private var rtcEvent: RTCEvent = RTCEvent.Idle
        set(value) {
            debug("RTCManager State [$field==========>$value]")
            observeRtcEvents(value)
            field = value
        }

    private fun observeRtcEvents(rtcEvent: RTCEvent) {
        when (rtcEvent) {
            is RTCEvent.Idle -> debug("RTC Manager Reset Initial State")
            is RTCEvent.Connecting -> async { signalClientClient?.connect(url, roomName, this) }
            is RTCEvent.ParticipantEvent.ParticipantConnected -> async { createOffer() }
        }
    }

    fun call(identity: String, roomId: String) {
        url = identity
        roomName = roomId
        signalClientClient = SignalingSocketIOClient()
        rtcEvent = RTCEvent.Connecting
    }

    override fun onRoomConnected() {
        debug("Signaling Socket Connected")
    }

    override fun onParticipantConnected() {
        rtcEvent = RTCEvent.ParticipantEvent.ParticipantConnected
    }

    override fun onParticipantReceiveOffer() {
    }

    override fun onRoomGetAnswer() {
    }

    override fun onExchangeCandidate() {
    }

    override fun onClose() {
    }

    private fun createOffer() {
// todo create offer
    }
}
