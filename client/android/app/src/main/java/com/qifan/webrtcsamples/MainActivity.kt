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
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.qifan.webrtc.extensions.common.* // ktlint-disable no-wildcard-imports
import com.qifan.webrtcsamples.WebRtcActivity.Companion.startWebRtcActivity
import com.qifan.webrtcsamples.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val editRoomIdText get() = binding.editRoomId
    private val editIpText get() = binding.editIpAddr

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
        if (permissionGranted(
            CAMERA_PERMISSION,
            AUDIO_PERMISSION,
            MODIFY_AUDIO_PERMISSION
        )
        ) {
            startWebRtcActivity(
                roomId = editRoomIdText.text.toString(),
                ipAddr = editIpText.text.toString()
            )
        } else {
            requirePermissions()
        }
    }

    private fun requirePermissions() {
        if (shouldShowRequestPermissionRationaleCompat(
            CAMERA_PERMISSION,
            AUDIO_PERMISSION,
            MODIFY_AUDIO_PERMISSION
        )
        ) {
            showPermissionRationaleDialog()
        } else {
            requestPermissionNow()
        }
    }

    private fun showPermissionRationaleDialog() {
        AlertDialog.Builder(this)
            .setTitle("WebRtc Permissions  Required")
            .setMessage("This app need WebRtc Permissions to work well")
            .setPositiveButton("Grant") { dialog, _ ->
                dialog.dismiss()
                requestPermissionNow()
            }
            .setNegativeButton("Deny") { dialog, _ ->
                dialog.dismiss()
                onPermissionsDenied()
            }
            .show()
    }

    private fun requestPermissionNow() {
        requestPermissionsCompat(
            arrayOf(
                CAMERA_PERMISSION,
                AUDIO_PERMISSION,
                MODIFY_AUDIO_PERMISSION
            ),
            PERMISSION_REQUEST_CODE
        )
    }

    private fun onPermissionsDenied() {
        Toast.makeText(this, "WebRTC Permissions Denied", Toast.LENGTH_LONG).show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == PERMISSION_REQUEST_CODE && grantResults.permissionsGranted()) {
            startWebRtcActivity(
                roomId = editRoomIdText.text.toString(),
                ipAddr = editIpText.text.toString()
            )
        } else {
            onPermissionsDenied()
        }
    }
}
