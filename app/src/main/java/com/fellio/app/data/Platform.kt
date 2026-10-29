package com.fellio.app.data

import android.content.Context
import android.net.Uri

/** Small platform layer so the screens and view models can be shared with the desktop app. */
object Platform {
    lateinit var appContext: Context
    const val supportsNotifications = true

    fun writeBytes(target: Any, bytes: ByteArray) {
        val out = appContext.contentResolver.openOutputStream(target as Uri) ?: error("no stream")
        out.use { it.write(bytes) }
    }

    fun readText(target: Any): String {
        val input = appContext.contentResolver.openInputStream(target as Uri) ?: error("no stream")
        return input.bufferedReader().use { it.readText() }
    }

    fun setNotifications(on: Boolean) {
        if (on) Notifier.schedule(appContext) else Notifier.cancel(appContext)
    }
}
