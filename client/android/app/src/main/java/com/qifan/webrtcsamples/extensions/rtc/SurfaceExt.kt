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

import org.webrtc.EglBase
import org.webrtc.RendererCommon
import org.webrtc.SurfaceTextureHelper
import org.webrtc.SurfaceViewRenderer

typealias OptionConfigure = () -> Unit

/**
 * create surface texture to attach video capturer with it
 */
fun createSurfaceTexture(
    threadName: String = Thread.currentThread().name,
    sharedContext: EglBase.Context
): SurfaceTextureHelper {
    return SurfaceTextureHelper.create(threadName, sharedContext)
}

/**
 * method to setup [SurfaceViewRenderer] provided by webrtc libray
 * that does the rendering of webrtc frames for us
 * @param rootEglBase  webrtc render open gl engine
 * @param optionalConfigurations optional configuration calling functions
 */
fun SurfaceViewRenderer.initializeSurfaceView(
    rootEglBase: EglBase,
    optionalConfigurations: OptionConfigure? = null
) = apply {
    setMirror(true)
    setEnableHardwareScaler(true)
    setZOrderMediaOverlay(true)
    setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT)
    init(rootEglBase.eglBaseContext, null)
    optionalConfigurations?.invoke()
}
