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

import com.qifan.webrtc.constants.LOCAL_VIDEO_TRACK_ID
import org.webrtc.PeerConnectionFactory
import org.webrtc.VideoSource
import org.webrtc.VideoTrack

/**
 * create video source
 */
fun createVideoSource(
  peerConnectionFactory: PeerConnectionFactory,
  isScreenCast: Boolean
): VideoSource {
  return peerConnectionFactory.createVideoSource(isScreenCast)
}

/**
 * attach video source to video track
 */
fun createVideoTrack(
  peerConnectionFactory: PeerConnectionFactory,
  id: String = LOCAL_VIDEO_TRACK_ID,
  videoSource: VideoSource
): VideoTrack {
  return peerConnectionFactory.createVideoTrack(id, videoSource)
}
