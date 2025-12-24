package com.helow.runner4

import com.helow.runner4.run.RunUtils
import org.junit.Assert.assertEquals
import org.junit.Test

class RunUtilsTest {

    @Test
    fun testCalculateDurationParts() {
        // 1 hour, 1 minute, 1 second = 3600 + 60 + 1 = 3661
        val (h1, m1, s1) = RunUtils.calculateDurationParts(3661)
        assertEquals(1L, h1)
        assertEquals(1L, m1)
        assertEquals(1L, s1)

        // 59 seconds
        val (h2, m2, s2) = RunUtils.calculateDurationParts(59)
        assertEquals(0L, h2)
        assertEquals(0L, m2)
        assertEquals(59L, s2)

        // 0 seconds
        val (h3, m3, s3) = RunUtils.calculateDurationParts(0)
        assertEquals(0L, h3)
        assertEquals(0L, m3)
        assertEquals(0L, s3)
    }

    @Test
    fun testFormatTimer() {
        assertEquals("01:01:01", RunUtils.formatTimer(3661))
        assertEquals("00:59", RunUtils.formatTimer(59))
        assertEquals("10:00:00", RunUtils.formatTimer(36000))
        assertEquals("00:00", RunUtils.formatTimer(0))
    }

    @Test
    fun testCalculateAverageSpeed() {
        // 10 km in 1 hour = 10000 meters in 3600 seconds = 10 km/h
        assertEquals(10.0, RunUtils.calculateAverageSpeed(10000.0, 3600), 0.01)

        // 5 km in 30 minutes = 5000 meters in 1800 seconds = 10 km/h
        assertEquals(10.0, RunUtils.calculateAverageSpeed(5000.0, 1800), 0.01)

        // 100 meters in 10 seconds = 10 m/s = 36 km/h
        assertEquals(36.0, RunUtils.calculateAverageSpeed(100.0, 10), 0.01)

        // Edge case: 0 seconds
        assertEquals(0.0, RunUtils.calculateAverageSpeed(100.0, 0), 0.01)

        // Rounding test: 1000 meters in 300 seconds (5 min) = 3.333 m/s = 12 km/h
        // (1000 / 300) * 3.6 = 3.333 * 3.6 = 12.0
        assertEquals(12.0, RunUtils.calculateAverageSpeed(1000.0, 300), 0.01)
    }

    @Test
    fun testRoundDistance() {
        assertEquals(123.46, RunUtils.roundDistance(123.4567), 0.001)
        assertEquals(123.45, RunUtils.roundDistance(123.454), 0.001)
        assertEquals(123.0, RunUtils.roundDistance(123.0), 0.001)
    }
}