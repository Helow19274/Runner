package com.helow.runner4.run

import kotlin.math.round

object RunUtils {

    fun calculateDurationParts(totalSeconds: Long): Triple<Long, Long, Long> {
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds / 60) % 60
        val seconds = totalSeconds % 60
        return Triple(hours, minutes, seconds)
    }

    fun formatTimer(totalSeconds: Long): String {
        val (hours, minutes, seconds) = calculateDurationParts(totalSeconds)
        return if (hours > 0) {
            "%02d:%02d:%02d".format(hours, minutes, seconds)
        } else {
            "%02d:%02d".format(minutes, seconds)
        }
    }

    fun calculateAverageSpeed(distanceMeters: Double, timeSeconds: Long): Double {
        if (timeSeconds <= 0L) return 0.0
        val speedKmh = (distanceMeters / timeSeconds) * 3.6
        return round(speedKmh * 10) / 10.0
    }

    fun roundDistance(distanceMeters: Double): Double {
        return round(distanceMeters * 100) / 100.0
    }
}