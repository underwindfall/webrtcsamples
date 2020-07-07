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

import android.os.Build
import android.os.Handler
import android.os.Looper
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

private val executor: ExecutorService by lazy { Executors.newSingleThreadExecutor() }
private val handler: Handler = Handler(Looper.getMainLooper())

fun async(action: () -> Unit) {
    executor.execute { action() }
}

fun ui(action: () -> Unit) {
    if (isUIThread()) {
        action()
    } else {
        handler.post { action() }
    }
}

fun isUIThread(): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        Looper.getMainLooper().isCurrentThread
    } else {
        Thread.currentThread() == Looper.getMainLooper().thread
    }
}
