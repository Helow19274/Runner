package com.helow.runner4

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import com.google.android.gms.location.Granularity
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.Priority

fun Context.openAppSettings() {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", packageName, null)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    startActivity(intent)
}

val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 2000L)
    .setGranularity(Granularity.GRANULARITY_FINE)
    .setMinUpdateIntervalMillis(1000L)
    .setMinUpdateDistanceMeters(2f)
    .setWaitForAccurateLocation(true)
    .build()