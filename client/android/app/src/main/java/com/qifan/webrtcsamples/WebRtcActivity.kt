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
package com.qifan.webrtcsamples

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.qifan.webrtc.RTCManager
import com.qifan.webrtc.model.CallSource
import com.qifan.webrtc.model.MediaViewRender
import com.qifan.webrtc.model.RTCConstraints
import com.qifan.webrtcsamples.databinding.ActivityWebRtcBinding
import kotlin.properties.Delegates

class WebRtcActivity : AppCompatActivity(), RTCManager.Listener {
    private lateinit var webRtcBinding: ActivityWebRtcBinding
    private val handUpBtn get() = webRtcBinding.btnHangUp
    private val muteMicroBtn get() = webRtcBinding.btnMuteMicro
    private val muteSpeakerBtn get() = webRtcBinding.btnMuteSpeaker
    private val switchCameraBtn get() = webRtcBinding.btnSwitchCamera
    private val localViewRender get() = webRtcBinding.rtcViewLocal
    private val remoteViewRender get() = webRtcBinding.rtcViewRemote
    private var rtcManager: RTCManager by Delegates.notNull()
    private lateinit var roomId: String
    private lateinit var ipAddr: String

    companion object {
        private const val ROOM = "ROOM"
        private const val IPADDRESS = "IPADDRESS"

        @JvmStatic
        fun Activity.startWebRtcActivity(roomId: String, ipAddr: String) {
            startActivity(
                Intent(this, WebRtcActivity::class.java)
                    .putExtra(ROOM, roomId)
                    .putExtra(IPADDRESS, ipAddr)
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        webRtcBinding = ActivityWebRtcBinding.inflate(layoutInflater)
        val view = webRtcBinding.root
        setContentView(view)
        rtcManager = RTCManager(applicationContext)
        handUpBtn.setOnClickListener {
            rtcManager.hangup()
        }
        muteMicroBtn.setOnClickListener {
            rtcManager.changeMicro()
        }
        muteSpeakerBtn.setOnClickListener {
            rtcManager.changeSpeaker()
        }
        switchCameraBtn.setOnClickListener {
            rtcManager.switchCam()
        }
        parseIntents()
        initializeWebRtc()
    }

    private fun parseIntents() {
        val room = intent.getStringExtra(ROOM)
        val ip = intent.getStringExtra(IPADDRESS)
        require(room != null && ip != null) { "Must have essential parameter to initialize call" }
        roomId = room
        ipAddr = ip
    }

    private fun initializeWebRtc() {
        val callSource = CallSource(
            activity = this,
            identity = ipAddr,
            roomId = roomId,
            mediaViewRender = MediaViewRender(localViewRender, remoteViewRender),
            rtcConstraints = RTCConstraints()
        )
        rtcManager.call(callSource, this)
    }

    override fun cleanup() {
        finish()
    }

    override fun onLocalAudioChange(enable: Boolean) {
        muteMicroBtn.setText(if (enable) R.string.btn_un_mute_micro else R.string.btn_mute_micro)
    }

    override fun onRemoteAudioChange(enable: Boolean) {
        muteSpeakerBtn.setText(if (enable) R.string.remote_btn_un_mute_spear else R.string.remote_btn_mute_speaker)
    }
}
