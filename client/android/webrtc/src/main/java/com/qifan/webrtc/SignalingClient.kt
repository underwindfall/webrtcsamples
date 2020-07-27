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
import com.qifan.webrtc.extensions.common.error
import com.qifan.webrtc.extensions.common.warn
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONException
import org.json.JSONObject
import java.net.URISyntaxException
import kotlin.properties.Delegates

private const val EVENT_CREATED = "created"
private const val EVENT_FULL = "full"
private const val EVENT_JOIN = "join"
private const val EVENT_JOINED = "joined"
private const val EVENT_LOG = "log"
private const val EVENT_MESSAGE = "message"
private const val EVENT_CLOSE = "close"

class SignalingClient {
    private var socket: Socket by Delegates.notNull()
    private var eventListener: SignalEventListener? = null

    internal fun initialize(url: String, roomId: String, listener: SignalEventListener) {
        try {
            socket = IO.socket(url)
            socket.connect()
            eventListener = listener
            attachSignalEvents(roomId)
        } catch (e: URISyntaxException) {
            // TODO need use a error manager to return error message
            e.printStackTrace()
        }
    }

    internal fun sendMessage(message: JSONObject) {
        socket.emit("message", message)
    }

    private fun attachSignalEvents(roomId: String) {
        socket
            .on(Socket.EVENT_CONNECT) {
                debug("Signaling socket connected")
                socket.emit("create or join", roomId)
                eventListener?.onConnectSignaling()
            }
            .on(EVENT_CREATED) {
                debug("Signaling socket created")
                eventListener?.onCreatedRoom()
            }
            .on(EVENT_FULL) {
                warn("Signaling Socket Room is Full")
            }
            .on(EVENT_JOIN) {
                debug("Signaling Socket Another peer made a request to connect")
                eventListener?.onRemoteUserJoined()
            }
            .on(EVENT_JOINED) {
                debug("Signaling Socket joined")
            }
            .on(EVENT_LOG) { msg ->
                msg.forEach { debug("Server Signaling send====> $it") }
            }
            .on(EVENT_MESSAGE) { msg ->
                debug("Socket Signaling message")
                try {
                    val message = msg.firstOrNull()
                    check(message is JSONObject) {
                        // TODO error manager
                        error("Receive Socket message have problem")
                    }
                    eventListener?.onReceiveMessage(message)
                } catch (e: JSONException) {
                    // TODO error manager
                    e.printStackTrace()
                }
            }
            .on(EVENT_CLOSE) {
                debug("Socket Signaling Close")
                eventListener?.onDisConnectRoom()
                socket.close()
                eventListener = null
            }
            .on(Socket.EVENT_DISCONNECT) {
                debug("Signal Socket disconnect")
            }
    }

    internal interface SignalEventListener {
        fun onConnectSignaling()
        fun onCreatedRoom()
        fun onRemoteUserJoined()
        fun onReceiveMessage(json: JSONObject)
        fun onDisConnectRoom()
    }
}
