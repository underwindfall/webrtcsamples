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
package com.qifan.webrtc.model

import org.webrtc.MediaConstraints

data class RTCConstraints(
    val audio: MediaConstraints.KeyValuePair = MediaConstraints.KeyValuePair(
        "OfferToReceiveAudio",
        "true"
    ),
    val video: MediaConstraints.KeyValuePair = MediaConstraints.KeyValuePair(
        "OfferToReceiveVideo",
        "true"
    )
)

internal fun RTCConstraints.toConstraints(): MediaConstraints {
    return MediaConstraints().apply {
        mandatory.add(audio)
        mandatory.add(video)
    }
}

internal fun MediaConstraints.addIceRestart(): MediaConstraints {
    return apply { mandatory.add(MediaConstraints.KeyValuePair("IceRestart", "true")) }
}
