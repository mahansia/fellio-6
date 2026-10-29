package com.fellio.app.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

/** Returns a function that opens the system "save file" picker with a suggested name. */
@Composable
fun rememberSaveFileLauncher(mime: String, onTarget: (Any) -> Unit): (String) -> Unit {
    val cb = rememberUpdatedState(onTarget)
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument(mime)) { uri ->
        if (uri != null) cb.value(uri)
    }
    return { name -> launcher.launch(name) }
}

/** Returns a function that opens the system "open file" picker. */
@Composable
fun rememberOpenFileLauncher(onTarget: (Any) -> Unit): () -> Unit {
    val cb = rememberUpdatedState(onTarget)
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) cb.value(uri)
    }
    return { launcher.launch(arrayOf("*/*")) }
}

/** Switch handler that asks for the notification permission on Android 13+. */
@Composable
fun rememberNotificationSwitch(onChange: (Boolean) -> Unit): (Boolean) -> Unit {
    val context = LocalContext.current
    val cb = rememberUpdatedState(onChange)
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) cb.value(true)
    }
    return { on ->
        if (!on) {
            cb.value(false)
        } else if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            cb.value(true)
        }
    }
}
