package com.qifan.webrtcsamples;

import android.content.Context;

import org.webrtc.audio.AudioDeviceModule;
import org.webrtc.audio.JavaAudioDeviceModule;

public class AudioFactory {
    /**
     * Create Java audio device
     *
     * @param context context
     * @return well configured audio device
     */
    public static AudioDeviceModule createJavaAudioDevice(Context context) {

        // Enable/disable OpenSL ES playback.

        // Set audio record error callbacks.
        JavaAudioDeviceModule.AudioRecordErrorCallback audioRecordErrorCallback
                = new JavaAudioDeviceModule.AudioRecordErrorCallback() {
            @Override
            public void onWebRtcAudioRecordInitError(String errorMessage) {
            }

            @Override
            public void onWebRtcAudioRecordStartError(
                    JavaAudioDeviceModule.AudioRecordStartErrorCode errorCode,
                    String errorMessage) {
            }

            @Override
            public void onWebRtcAudioRecordError(String errorMessage) {
            }
        };

        JavaAudioDeviceModule.AudioTrackErrorCallback audioTrackErrorCallback
                = new JavaAudioDeviceModule.AudioTrackErrorCallback() {
            @Override
            public void onWebRtcAudioTrackInitError(String errorMessage) {
            }

            @Override
            public void onWebRtcAudioTrackStartError(
                    JavaAudioDeviceModule.AudioTrackStartErrorCode errorCode, String errorMessage) {
            }

            @Override
            public void onWebRtcAudioTrackError(String errorMessage) {
            }
        };

        return JavaAudioDeviceModule.builder(context)
                .setUseHardwareAcousticEchoCanceler(true)
                .setUseHardwareNoiseSuppressor(true)
                .setAudioRecordErrorCallback(audioRecordErrorCallback)
                .setAudioTrackErrorCallback(audioTrackErrorCallback)
                .createAudioDeviceModule();
    }
}
