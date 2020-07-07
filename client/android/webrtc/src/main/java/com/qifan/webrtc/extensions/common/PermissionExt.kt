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
package com.qifan.webrtc.extensions.common

import android.Manifest
import android.content.pm.PackageManager
import androidx.annotation.IntRange
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat

private typealias Permission = String

const val CAMERA_PERMISSION = Manifest.permission.CAMERA
const val AUDIO_PERMISSION = Manifest.permission.RECORD_AUDIO
const val MODIFY_AUDIO_PERMISSION = Manifest.permission.MODIFY_AUDIO_SETTINGS
const val PERMISSION_REQUEST_CODE = 0

fun AppCompatActivity.checkSelfPermissionCompat(vararg permissions: Permission) =
    permissions.map { permission -> ActivityCompat.checkSelfPermission(this, permission) }

fun AppCompatActivity.shouldShowRequestPermissionRationaleCompat(vararg permissions: Permission) =
    permissions.all { permission ->
        ActivityCompat.shouldShowRequestPermissionRationale(this, permission)
    }

fun AppCompatActivity.requestPermissionsCompat(
    permissionsArray: Array<Permission>,
    @IntRange(from = 0) requestCode: Int
) {
    ActivityCompat.requestPermissions(this, permissionsArray, requestCode)
}

fun AppCompatActivity.permissionGranted(vararg permissions: Permission): Boolean {
    return checkSelfPermissionCompat(*permissions).all { it == PackageManager.PERMISSION_GRANTED }
}

fun IntArray.permissionsGranted(): Boolean = all { it == PackageManager.PERMISSION_GRANTED }
