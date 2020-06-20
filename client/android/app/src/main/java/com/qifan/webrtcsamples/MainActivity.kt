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

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.qifan.webrtcsamples.WebRtcActivity.Companion.startWebRtcActivity
import com.qifan.webrtcsamples.databinding.ActivityMainBinding
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.btnConfirm.setOnClickListener {
            val roomId = edit_room_id.text
            val ip = edit_ip_addr.text
            if (!ip.isNullOrEmpty() && !roomId.isNullOrEmpty()) {
                startWebRtcActivity(roomId = roomId.toString(), ipAddr = ip.toString())
            } else {
                Toast.makeText(this, R.string.input_parameter_warning, Toast.LENGTH_LONG).show()
            }
        }
    }
}
