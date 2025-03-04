package com.example.weatherx.utils

import android.Manifest
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun RequestLocationPermission(
    onPermissionGranted: () -> Unit
) {
    val context = LocalContext.current
    val permissionState = rememberPermissionState(
        permission = Manifest.permission.ACCESS_FINE_LOCATION
    )

    LaunchedEffect(Unit) {
        if (permissionState.status.isGranted) {
            // Permission already granted, fetch location
            onPermissionGranted()
        } else {
            // Request permission
            permissionState.launchPermissionRequest()
        }
    }

    when {
        permissionState.status.isGranted -> {
            Text(text = "Permission Granted! Fetching Location...")
            onPermissionGranted()
        }

        permissionState.status.shouldShowRationale -> {
            Text(text = "Location is needed for weather updates. Please grant permission.")
        }

        else -> {
            Text(text = "Permission Denied. Enable from Settings.")
        }
    }
}
