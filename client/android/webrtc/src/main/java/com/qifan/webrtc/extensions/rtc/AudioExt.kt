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
import com.qifan.webrtc.constants.LOCAL_AUDIO_TRACK_ID
import org.webrtc.AudioSource
import org.webrtc.AudioTrack
import org.webrtc.MediaConstraints
import org.webrtc.PeerConnectionFactory
import org.webrtc.audio.AudioDeviceModule
import org.webrtc.audio.JavaAudioDeviceModule

private const val AUDIO_ECHO_CANCELLATION_CONSTRAINT = "googEchoCancellation"
private const val AUDIO_AUTO_GAIN_CONTROL_CONSTRAINT = "googAutoGainControl"
private const val AUDIO_HIGH_PASS_FILTER_CONSTRAINT = "googHighpassFilter"
private const val AUDIO_NOISE_SUPPRESSION_CONSTRAINT = "googNoiseSuppression"
private const val AUDIO_LEVEL_CONTROL_CONSTRAINT = "levelControl"

/**
 * create audio source
 * @param constraints of audio source
 */
fun createAudioSource(
  peerConnectionFactory: PeerConnectionFactory,
  constraints: MediaConstraints = buildMediaConstraints()
): AudioSource {
  return peerConnectionFactory.createAudioSource(constraints)
}

private fun buildMediaConstraints(): MediaConstraints {
  return MediaConstraints().apply {
    mandatory.add(MediaConstraints.KeyValuePair(AUDIO_ECHO_CANCELLATION_CONSTRAINT, "true"))
    mandatory.add(MediaConstraints.KeyValuePair(AUDIO_AUTO_GAIN_CONTROL_CONSTRAINT, "true"))
    mandatory.add(MediaConstraints.KeyValuePair(AUDIO_HIGH_PASS_FILTER_CONSTRAINT, "true"))
    mandatory.add(MediaConstraints.KeyValuePair(AUDIO_NOISE_SUPPRESSION_CONSTRAINT, "true"))
    mandatory.add(MediaConstraints.KeyValuePair(AUDIO_LEVEL_CONTROL_CONSTRAINT, "true"))
  }
}

/**
 * attach audio source to audio track
 * @param id identifier of AudioTrack
 * @param audioSource source of audio
 */
fun createAudioTrack(
  peerConnectionFactory: PeerConnectionFactory,
  id: String = LOCAL_AUDIO_TRACK_ID,
  audioSource: AudioSource
): AudioTrack {
  return peerConnectionFactory.createAudioTrack(id, audioSource)
}

/**
 * Create Java audio device
 *
 * @param context context
 * @return well configured audio device
 */
fun createJavaAudioDevice(context: Context): AudioDeviceModule {
  // Set audio record error callbacks
  val audioRecordErrorCallback = object : JavaAudioDeviceModule.AudioRecordErrorCallback {
    override fun onWebRtcAudioRecordInitError(p0: String?) {
      error(message = "onWebRtcAudioRecordInitError $p0")
    }

    override fun onWebRtcAudioRecordError(p0: String?) {
      error(message = "onWebRtcAudioRecordError $p0")
    }

    override fun onWebRtcAudioRecordStartError(
      p0: JavaAudioDeviceModule.AudioRecordStartErrorCode?,
      p1: String?
    ) {
      error(message = "onWebRtcAudioRecordStartError code => $p0  message=> $p1 ")
    }
  }
  // Set audio track error callbacks
  val audioTrackErrorCallback = object : JavaAudioDeviceModule.AudioTrackErrorCallback {
    override fun onWebRtcAudioTrackError(p0: String?) {
      error(message = "onWebRtcAudioTrackError $p0")
    }

    override fun onWebRtcAudioTrackStartError(
      p0: JavaAudioDeviceModule.AudioTrackStartErrorCode?,
      p1: String?
    ) {
      error(message = "onWebRtcAudioTrackStartError code => $p0 message=> $p1")
    }

    override fun onWebRtcAudioTrackInitError(p0: String?) {
      error(message = "onWebRtcAudioTrackInitError  $p0")
    }
  }
  return JavaAudioDeviceModule.builder(context)
    .setUseHardwareAcousticEchoCanceler(true)
    .setUseHardwareNoiseSuppressor(true)
    .setAudioRecordErrorCallback(audioRecordErrorCallback)
    .setAudioTrackErrorCallback(audioTrackErrorCallback)
    .createAudioDeviceModule()
}
