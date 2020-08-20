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

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.qifan.powerpermission.askPermissions
import com.qifan.powerpermission.data.PermissionResult
import com.qifan.powerpermission.data.hasAllGranted
import com.qifan.powerpermission.data.hasPermanentDenied
import com.qifan.powerpermission.rationale.createDialogRationale
import com.qifan.powerpermission.rationale.delegate.RationaleDelegate
import com.qifan.webrtc.extensions.common.AUDIO_PERMISSION
import com.qifan.webrtc.extensions.common.CAMERA_PERMISSION
import com.qifan.webrtc.extensions.common.MODIFY_AUDIO_PERMISSION
import com.qifan.webrtcsamples.WebRtcActivity.Companion.startWebRtcActivity
import com.qifan.webrtcsamples.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val editRoomIdText get() = binding.editRoomId
    private val editIpText get() = binding.editIpAddr
    private val dialogRationaleDelegate: RationaleDelegate by lazy {
        createDialogRationale(
            dialogTitle = R.string.permission_dialog_title,
            requiredPermissions = listOf(
                CAMERA_PERMISSION,
                AUDIO_PERMISSION,
                MODIFY_AUDIO_PERMISSION
            ),
            message = getString(R.string.permission_dialog_message),
            negativeText = getString(R.string.permission_dialog_deny)
        )
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        editIpText.setText("http://192.168.1.29:8080")
        editRoomIdText.setText("Room1")
        binding.btnConfirm.setOnClickListener {
            if (!editRoomIdText.text.isNullOrEmpty() && !editIpText.text.isNullOrEmpty()) {
                checkPermissions()
            } else {
                Toast.makeText(this, R.string.input_parameter_warning, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun checkPermissions() {
        askPermissions(
            CAMERA_PERMISSION,
            AUDIO_PERMISSION,
            MODIFY_AUDIO_PERMISSION,
            rationaleDelegate = dialogRationaleDelegate
        ) { permissionResult: PermissionResult ->
            when {
                permissionResult.hasAllGranted() -> startWebRtcActivity(
                    roomId = editRoomIdText.text.toString(),
                    ipAddr = editIpText.text.toString()
                )
                permissionResult.hasPermanentDenied() -> onPermissionsDenied()
            }
        }
    }

    private fun onPermissionsDenied() {
        Toast.makeText(this, "WebRTC Permissions Denied", Toast.LENGTH_LONG).show()
    }
}
