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
package com.qifan.webrtc.extensions.rtc

import android.content.Context
import org.webrtc.Camera1Enumerator
import org.webrtc.Camera2Enumerator
import org.webrtc.CameraEnumerator
import org.webrtc.CameraVideoCapturer

/**
 * Function to get a VideoCapture Interface from WebRTC SDK to get front camera video
 */
fun Context.buildVideoCapturer(): CameraVideoCapturer {
    val canUseCamera2 = Camera2Enumerator.isSupported(this)
    return if (canUseCamera2) {
        createCameraCapturer(
            Camera2Enumerator(
                this
            )
        )
    } else {
        createCameraCapturer(
            Camera1Enumerator(
                true
            )
        )
    }
}

/**
 * Webrtc provides us a very easy way to use Camera and Camera2 API depending on the support.
 * On supported devices we can use either of the APIs
 * @param enumerator
 */
private fun createCameraCapturer(enumerator: CameraEnumerator): CameraVideoCapturer {
    return enumerator.run {
        // find front camera
        deviceNames.find { name -> isFrontFacing(name) }
            ?.let {
                createCapturer(it, null)
            }
            // if can't find any use others
            ?: deviceNames.find { name -> !isFrontFacing(name) }
                ?.let {
                    createCapturer(it, null)
                }
            ?: throw IllegalStateException("Couldn't find available camera")
    }
}
