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
import com.qifan.webrtcsamples.databinding.ActivityWebRtcBinding

class WebRtcActivity : AppCompatActivity() {
    private lateinit var webRtcBinding: ActivityWebRtcBinding
    private lateinit var roomId: String
    private lateinit var ipAddr: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        webRtcBinding = ActivityWebRtcBinding.inflate(layoutInflater)
        val view = webRtcBinding.root
        setContentView(view)
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
    }

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
}
