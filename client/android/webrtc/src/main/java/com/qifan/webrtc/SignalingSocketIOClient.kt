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

import com.qifan.webrtc.extensions.common.debug
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONObject
import java.net.URISyntaxException
import kotlin.properties.Delegates.notNull

private const val EVENT_CREATED = "created"
private const val EVENT_FULL = "full"
private const val EVENT_JOIN = "join"
private const val EVENT_JOINED = "joined"
private const val EVENT_LOG = "log"
private const val EVENT_MESSAGE = "message"
private const val EVENT_CLOSE = "close"

private const val TYPE_SEND_OFFER = "send_offer"
private const val TYPE_SEND_ANSWER = "send_answer"
private const val TYPE_SEND_CANDIDATE = "send_candidate"

internal class SignalingSocketIOClient {
    private var socket: Socket by notNull()
    private var listener: Listener? = null

    internal fun connect(identity: String, roomName: String, listener: Listener) {
        try {
            this.listener = listener
            socket = IO.socket(identity)
                .apply {
                    attachSocketListeners(roomName)
                }
        } catch (e: URISyntaxException) {
            e.printStackTrace()
        }
    }

    private fun attachSocketListeners(roomName: String) {
        socket
            .on(Socket.EVENT_CONNECT) {
                socket.emit("create or join", roomName)
            }
            .on(EVENT_CREATED) {
                listener?.onRoomConnected()
            }
            .on(EVENT_JOIN) {
                listener?.onParticipantConnected()
            }
            .on(EVENT_MESSAGE) { args ->
                val message = args.firstOrNull() as JSONObject
                when (message.getString("type")) {
                    TYPE_SEND_OFFER -> {
                        listener?.onParticipantReceiveOffer()
                    }
                    TYPE_SEND_ANSWER -> {
                        listener?.onRoomGetAnswer()
                    }
                    TYPE_SEND_CANDIDATE -> {
                        listener?.onExchangeCandidate()
                    }
                }
            }
            .on(EVENT_CLOSE) {
                listener?.onClose()
            }
            .on(EVENT_LOG) { args ->
                args.forEach { debug("Socket client server observe $it") }
            }
            .on(Socket.EVENT_DISCONNECT) {
                debug("Signal socket disconnect")
            }
    }

    internal fun disconnect() {
        socket.emit("leave")
        socket.off()
        socket.disconnect()
    }

    internal interface Listener {
        fun onRoomConnected()
        fun onParticipantConnected()
        fun onParticipantReceiveOffer()
        fun onRoomGetAnswer()
        fun onExchangeCandidate()
        fun onClose()
    }
}
