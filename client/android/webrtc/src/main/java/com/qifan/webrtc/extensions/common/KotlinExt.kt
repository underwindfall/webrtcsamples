package com.qifan.webrtc.extensions.common

inline fun <reified T> Any.safeCast(action: T.() -> Unit) {
    if (this is T) {
        action()
    }
}

