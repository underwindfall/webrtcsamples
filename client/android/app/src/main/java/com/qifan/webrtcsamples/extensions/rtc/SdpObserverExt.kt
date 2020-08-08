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
package com.qifan.webrtcsamples.extensions.rtc

import com.qifan.webrtcsamples.extensions.common.warn
import org.webrtc.SdpObserver
import org.webrtc.SessionDescription

typealias DslOnSetFailure = (result: String?) -> Unit
typealias DslOnSetSuccess = () -> Unit
typealias DslOnCreateFailure = (result: String?) -> Unit
typealias DslOnCreateSuccess = (sessionDescription: SessionDescription?) -> Unit

class SimpleObserver(private val source: Source) : SdpObserver {
    private var onSetFailure: DslOnSetFailure? = null
    private var onSetSuccess: DslOnSetSuccess? = null
    private var onCreateFailure: DslOnCreateFailure? = null
    private var onCreateSuccess: DslOnCreateSuccess? = null

    enum class Source(val value: String) {
        CALL_LOCAL("Call Local"),
        CALL_REMOTE("Call Remote"),
        RECEIVER_LOCAL("Receiver Local"),
        RECEIVER_REMOTE("Receiver Remote"),
        REMOTE_ANSWER("Remote Answer"),
        LOCAL_OFFER("Local Offer")
    }

    override fun onSetFailure(p0: String?) {
        onSetFailure?.invoke(p0)
        warn(message = "${source.value}====> onSetFailure  result is $p0")
    }

    fun onSetFailure(func: DslOnCreateFailure) {
        onSetFailure = func
    }

    override fun onSetSuccess() {
        onSetSuccess?.invoke()
        warn(message = "${source.value}====> onSetSuccess")
    }

    fun onSetSuccess(func: DslOnSetSuccess) {
        onSetSuccess = func
    }

    override fun onCreateSuccess(p0: SessionDescription?) {
        onCreateSuccess?.invoke(p0)
        warn(message = "${source.value}====> onCreateSuccess SessionDescription is $p0")
    }

    fun onCreateSuccess(func: DslOnCreateSuccess) {
        onCreateSuccess = func
    }

    override fun onCreateFailure(p0: String?) {
        onCreateFailure?.invoke(p0)
        warn(message = "${source.value}====> onCreateFailure result is $p0")
    }

    fun onCreateFailure(func: DslOnCreateFailure) {
        onCreateFailure = func
    }
}

inline fun sdpObserver(
    source: SimpleObserver.Source,
    observer: SimpleObserver.() -> Unit
): SimpleObserver {
    return SimpleObserver(source).apply(observer)
}
