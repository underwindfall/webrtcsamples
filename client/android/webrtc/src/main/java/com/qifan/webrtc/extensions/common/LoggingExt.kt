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

import android.util.Log

/**
 * Helper function to do the log debug work
 * @param tag simple java class name
 * @param message basic message
 * @param args additional message
 */
fun Any.debug(
    tag: String = this::class.java.simpleName,
    message: String,
    vararg args: Any?
) {
    try {
        Log.d(tag, message.format(*args))
    } catch (_: Exception) {
    }
}

/**
 * Helper function to do the log warn work
 * @param tag simple java class name
 * @param message basic message
 * @param args additional message
 */
fun Any.warn(
    tag: String = this::class.java.simpleName,
    message: String,
    vararg args: Any?
) {
    try {
        Log.w(tag, message.format(*args))
    } catch (_: Exception) {
    }
}

/**
 * Helper function to do the log error work
 * @param tag simple java class name
 * @param message basic message
 * @param args additional message
 */
fun Any.error(
    tag: String = this::class.java.simpleName,
    message: String,
    vararg args: Any?
) {
    try {
        Log.e(tag, message.format(*args))
    } catch (_: Exception) {
    }
}
