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

import org.webrtc.SessionDescription

sealed class RTCEvent {
  object Idle : RTCEvent()
  object Connecting : RTCEvent()

  sealed class ParticipantEvent : RTCEvent() {
    object CreateOffer : ParticipantEvent()
    data class SetLocalSdp(val sdp: SessionDescription?) : ParticipantEvent()
    data class SendOfferToParticipant(val sdp: SessionDescription?) : ParticipantEvent()
    data class SetRemoteSdp(val sdp: SessionDescription?) : ParticipantEvent()
    data class SendAnswer(val sdp: SessionDescription?) : ParticipantEvent()
  }

  override fun toString(): String {
    return this.javaClass.simpleName
  }
}
