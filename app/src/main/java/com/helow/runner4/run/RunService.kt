package com.helow.runner4.run

import android.Manifest
import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.Binder
import android.os.Build
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.location.LocationAvailability
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.firestore
import com.google.maps.android.SphericalUtil
import com.helow.runner4.MainActivity
import com.helow.runner4.NOTIFICATION_CHANNEL_ID
import com.helow.runner4.R
import com.helow.runner4.locationRequest
import java.util.Timer
import kotlin.concurrent.timer

class RunService : Service() {
    companion object {
        const val ACTION_START = "com.helow.runner4.action.START_RUN"
        const val ACTION_STOP  = "com.helow.runner4.action.STOP_RUN"

        private const val NOTIF_ID_RUN = 1
        private const val NOTIF_ID_ISSUE = 2
    }

    private val notificationManager by lazy { NotificationManagerCompat.from(this) }
    private val fusedLocationClient by lazy { LocationServices.getFusedLocationProviderClient(this) }
    private lateinit var startTime: Timestamp
    val duration by lazy { MutableLiveData(0) }
    val points by lazy { MutableLiveData(mutableListOf<LatLng>()) }
    private lateinit var timer: Timer

    private val callback = object : LocationCallback() {
        override fun onLocationAvailability(locationAvailability: LocationAvailability) {
            if (!locationAvailability.isLocationAvailable) {
                notificationManager.notify(NOTIF_ID_ISSUE, NotificationCompat.Builder(applicationContext,
                    NOTIFICATION_CHANNEL_ID
                ).apply {
                    setSmallIcon(R.drawable.ic_launcher_foreground)
                    setContentTitle(getString(R.string.settings_problem))
                    setContentText(getString(R.string.location_unavailable))
                    setOnlyAlertOnce(true)
                    setContentIntent(activityIntent())
                }.build())
            } else {
                notificationManager.cancel(NOTIF_ID_ISSUE)
            }
        }

        override fun onLocationResult(locationResult: LocationResult) {
            locationResult.locations.forEach { location ->
                val latLng = LatLng(location.latitude, location.longitude)
                val p = points.value!!
                p.add(latLng)
                points.value = p
            }
        }
    }

    inner class RunBinder : Binder() {
        fun getService(): RunService = this@RunService
    }

    override fun onBind(intent: Intent) = RunBinder()

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                stopRun()
                return START_NOT_STICKY
            }
            ACTION_START -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    startForeground(NOTIF_ID_RUN, createNotification(), ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION)
                }
                else {
                    startForeground(NOTIF_ID_RUN, createNotification())
                }
                startRun()
            }
            else -> {
                return START_NOT_STICKY
            }
        }
        return START_STICKY
    }

    private fun activityIntent() = PendingIntent.getActivity(
        this,
        0,
        Intent(this, MainActivity::class.java),
        PendingIntent.FLAG_IMMUTABLE
    )

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(applicationContext, NOTIFICATION_CHANNEL_ID).apply {
            setSmallIcon(R.drawable.ic_launcher_foreground)
            setContentTitle(getString(R.string.run))
            setOnlyAlertOnce(true)
            setOngoing(true)
            setContentIntent(activityIntent())
            setShowWhen(true)
            setWhen(System.currentTimeMillis())
            setUsesChronometer(true)
        }.build()
    }

    private fun startRun() {
        startTime = Timestamp.now()
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, CancellationTokenSource().token).addOnSuccessListener {
                val p = points.value!!
                p.add(LatLng(it.latitude, it.longitude))
                points.value = p
            }

            fusedLocationClient.requestLocationUpdates(locationRequest, callback, Looper.getMainLooper())
            timer = timer("timeCounter", initialDelay = 1000, period = 1000) {
                val diffTime = duration.value!! + 1
                duration.postValue(diffTime)
            }
        }
    }

    private fun stopRun() {
        timer.cancel()
        fusedLocationClient.removeLocationUpdates(callback)

        Firebase.firestore.collection("users").document(Firebase.auth.uid!!).collection("runs").add(
            Run(
                startTime = startTime,
                finishTime = Timestamp(startTime.seconds + duration.value!!, 0),
                distance = SphericalUtil.computeLength(points.value!!),
                route = points.value!!.map { GeoPoint(it.latitude, it.longitude) }
            )
        )

        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }
}